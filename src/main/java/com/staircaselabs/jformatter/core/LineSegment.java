package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.core.TextToken.TokenType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LineSegment {

    private LineSegment parent;
    private final LineBreak type;
    private final Queue<TextToken> head = new LinkedList<>();
    private List<LineSegment> branches = new ArrayList<>();
    private int width = 0;
    private int openParens = 0;

    public LineSegment( LineSegment parent, LineBreak type ) {
        this.parent = parent;
        this.type = type;
    }

    public void addLeafToken( TextToken token ) {
        head.add( token );
        width += token.getWidth();

        // keep track of the number of left/right parentheses that we encounter
        openParens += token.getType() == TokenType.LEFT_PAREN ? 1 : 0;
        openParens += token.getType() == TokenType.RIGHT_PAREN ? -1 : 0;
    }

    public void addSegment( LineSegment segment ) {
        segment.parent = this;
        branches.add( segment );
        width += segment.getWidth();

        // keep track of the number of left/right parentheses that we encounter
        openParens += segment.openParens;
    }

    public int getIndentOffset() {
        return (!head.isEmpty() ? head.peek().getIndentOffset() : branches.get( 0 ).getIndentOffset());
    }

    public void updateIndentOffset( int amount ) {
        if( !head.isEmpty() ) {
            head.peek().updateIndentOffset(amount);
        } else {
            branches.get( 0 ).updateIndentOffset( amount );
        }
    }

    public boolean hasMultipleBranches() {
        return branches.size() > 1;
    }

    public boolean isLeaf() {
        return !head.isEmpty() || branches.isEmpty();
    }

    public TextToken getFirstToken() {
        return !head.isEmpty() ? head.peek() : branches.get( 0 ).getFirstToken();
    }

    public void appendLineBreak( String newline ) {
        if( !branches.isEmpty() ) {
            // insert linebreak after final segment in the tail
            branches.get( branches.size() - 1 ).appendLineBreak( newline );
        } else {
            // insert linebreak after last head token
            head.add( new TextToken( newline, TokenType.NEWLINE, 0, 0 ) );
        }
    }

    public List<LineSegment> split( String newline, int numLineWrapTabs ) {
        // append linebreaks to each segment, except the last, which already has one
        IntStream.range( 0, branches.size() - 1 )
                .mapToObj( branches::get )
                .forEach( s -> s.appendLineBreak( newline ) );

        // indent second segment (which corresponds to what will be the first wrapped line)
        branches.get( 1 ).updateIndentOffset( numLineWrapTabs );

        // if necessary, unindent the final segment (which corresponds to what will be final wrapped line)
        TextToken startOfLastSegment = branches.get( branches.size() - 1 ).getFirstToken();
        if( startOfLastSegment.getLineBreak() == LineBreak.NON_BREAKING ) {
            startOfLastSegment.updateIndentOffset( -numLineWrapTabs );
        }

        return branches;
    }

    public int getWidth() {
        return width;
    }

    public Queue<TextToken> getTokens() {
        List<TextToken> branchTokens = branches.stream()
                .map( LineSegment::getTokens )
                .flatMap( Collection::stream )
                .collect( Collectors.toList() );
        return Stream.of( head, branchTokens )
                .flatMap( Collection::stream )
                .collect( Collectors.toCollection( LinkedList::new ) );
    }

    @Override
    public String toString() {
        return getTokens().stream().map( TextToken::toString ).collect( Collectors.joining() );
    }

    public void loadDotFile( String parentId, DotFile dotfile ) {
        // generate a unique ID for this node and remove any dashes
        String uuid = UUID.randomUUID().toString();

        String label = isLeaf()
                ? head.stream().map( TextToken::toString ).collect( Collectors.joining() )
                : type.toString();

        // strip off any newlines and double-quotes because Graphviz doesn't like them
        label = label.replace( "\"", "" );

        // add a label entry for this node
        dotfile.addNode( uuid, label );

        // add an edge from parent to this node
        if( parentId != null ) {
            dotfile.addEdge( parentId, uuid );
        }

        // process any branches
        branches.forEach( b -> b.loadDotFile( uuid, dotfile ) );
    }

    public static LineSegment create( Queue<TextToken> tokens, LineSegment parent ) {
        LineSegment segment = new LineSegment( parent, LineBreak.NON_BREAKING );
        segment.addLeafToken( tokens.remove() );

        while( !tokens.isEmpty() ) {
            TextToken token = tokens.peek();

            if( token.getLineBreak() == LineBreak.ASSIGNMENT ) {
                segment = createNewParent( segment, LineBreak.ASSIGNMENT );
                segment.addSegment( create( tokens, segment ) );
            } else if( token.getLineBreak() == LineBreak.METHOD_ARG ) {
                if( segment.openParens > 0 ) {
                    if( segment.type == LineBreak.METHOD_ARG ) {
                        segment.addSegment( create( tokens, segment ) );
                    } else {
                        segment = createNewParent( segment, LineBreak.METHOD_ARG );
                        segment.addSegment( create( tokens, segment ) );
                    }
                } else {
                    return segment;
                }
            } else if( token.getLineBreak() == LineBreak.METHOD_INVOCATION ) {
                if( segment.openParens == 0 ) {
                    if( segment.type == LineBreak.METHOD_INVOCATION ) {
                        segment.addSegment( create( tokens, segment ) );
                    } else {
                        if( segment.parent.type == LineBreak.METHOD_INVOCATION ) {
                            return segment;
                        } else {
                            segment = createNewParent( segment, LineBreak.METHOD_INVOCATION );
                            segment.addSegment( create( tokens, segment ) );
                        }
                    }
                } else if( segment.openParens < 0 ) {
                    return segment;
                }
            } else if( token.getLineBreak() == LineBreak.NON_BREAKING ) {
                if( token.getType() == TokenType.RIGHT_PAREN ) {
                    if( segment.openParens == 0 ) {
                        return segment;
                    } else if (segment.openParens > 0 && segment.type != LineBreak.NON_BREAKING) {
                        segment.addSegment(create(tokens, segment));
                    } else {
                        if( segment.isLeaf() ) {
                            segment.addLeafToken( tokens.remove() );
                        } else {
                            segment.addSegment(create(tokens, segment));
                        }
                    }
                } else {
                    segment.addLeafToken( tokens.remove() );
                }
            } else {
                throw new RuntimeException( "Missing case for line break: " + token.getLineBreak().toString() );
            }
        }

        return segment;
    }

    private static LineSegment createNewParent( LineSegment original, LineBreak type ) {
        LineSegment child = original;
        LineSegment parent = new LineSegment( child.parent, type );
        parent.addSegment( child );
        return parent;
    }

}
