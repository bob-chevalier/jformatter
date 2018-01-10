package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.core.TextToken.TokenType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class LineSegment {

    protected LineSegment parent;
    protected int width = 0;
    protected int openParens = 0;

    public LineSegment( LineSegment parent ) {
        this.parent = parent;
    }

    public abstract void add( TextToken token );

    public abstract void add( LineSegment branch );

    public abstract BreakType getType();

    public abstract TextToken getFirstToken();

    public abstract Queue<TextToken> getTokens();

    public abstract void appendLineBreak( String newline );

    public abstract int getIndentOffset();

    public abstract void updateIndentOffset( int amount );

    public abstract boolean isLeaf();

    public abstract boolean canBeSplit();

    public abstract List<LineSegment> split( String newline, int numLineWrapTabs );

    public abstract void loadDotFile( String parentId, DotFile dotfile );

    public int getWidth() {
        return width;
    }

    @Override
    public String toString() {
        return getTokens().stream().map( TextToken::toString ).collect( Collectors.joining() );
    }

    public static LineSegment create( Queue<TextToken> tokens, LineSegment parent ) {
        LineSegment segment = new LeafLineSegment( parent );
        segment.add( tokens.remove() );

        while( !tokens.isEmpty() ) {
            TextToken token = tokens.peek();

            switch( token.getLineBreakType() ) {
                case ASSIGNMENT:
                    segment = createNewParent( segment, BreakType.ASSIGNMENT );
                    segment.add( create( tokens, segment ) );
                    break;
                case METHOD_ARG:
                    if( segment.openParens > 0 ) {
                        if( segment.getType() == BreakType.METHOD_ARG ) {
                            segment.add( create( tokens, segment ) );
                        } else {
                            segment = createNewParent( segment, BreakType.METHOD_ARG );
                            segment.add( create( tokens, segment ) );
                        }
                    } else {
                        return segment;
                    }
                    break;
                case METHOD_INVOCATION:
                    if( segment.openParens == 0 ) {
                        if( segment.getType() == BreakType.METHOD_INVOCATION ) {
                            segment.add( create( tokens, segment ) );
                        } else {
                            if( segment.parent != null && segment.parent.getType() == BreakType.METHOD_INVOCATION ) {
                                return segment;
                            } else {
                                segment = createNewParent( segment, BreakType.METHOD_INVOCATION );
                                segment.add( create( tokens, segment ) );
                            }
                        }
                    } else if( segment.openParens < 0 ) {
                        return segment;
                    }
                    break;
                case NON_BREAKING:
                    if (token.getType() == TokenType.RIGHT_PAREN) {
                        if (segment.openParens == 0) {
                            return segment;
                        } else if (segment.openParens > 0 && segment.getType() != BreakType.NON_BREAKING) {
                            segment.add(create(tokens, segment));
                        } else {
                            if (segment.isLeaf()) {
                                segment.add(tokens.remove());
                            } else {
                                segment.add(create(tokens, segment));
                            }
                        }
                    } else {
                        segment.add(tokens.remove());
                    }
                    break;
                case TERNARY:
                    if( segment.getType() == BreakType.TERNARY ) {
                        segment.add( create( tokens, segment ) );
                    } else {
                        if( segment.parent != null && segment.parent.getType() == BreakType.TERNARY ) {
                            return segment;
                        } else {
                            segment = createNewParent( segment, BreakType.TERNARY );
                            segment.add( create( tokens, segment ) );
                        }
                    }
                    break;
                default:
                    throw new RuntimeException( "Missing case for line break: " + token.getLineBreakType().name() );
            }
        }

        return segment;
    }

    private static LineSegment createNewParent( LineSegment original, BreakType type ) {
        LineSegment child = original;
        LineSegment parent = new BranchLineSegment( child.parent, type );
        parent.add( child );
        return parent;
    }

}
