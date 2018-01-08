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

public class BranchLineSegment extends LineSegment {

    private final BreakType type;
    private List<LineSegment> branches = new ArrayList<>();

    public BranchLineSegment( LineSegment parent, BreakType type ) {
        super( parent );
        this.type = type;
    }

    @Override
    public void add( TextToken token ) {
        throw new UnsupportedOperationException( "Adding a TextToken is not a valid BranchLineSegment operation" );
    }

    @Override
    public void add( LineSegment segment ) {
        segment.parent = this;
        branches.add( segment );
        width += segment.getWidth();

        // keep track of the number of left/right parentheses that we encounter
        openParens += segment.openParens;
    }

    @Override
    public BreakType getType() {
        return type;
    }

    @Override
    public TextToken getFirstToken() {
        return branches.get( 0 ).getFirstToken();
    }

    @Override
    public Queue<TextToken> getTokens() {
        return branches.stream()
                .map( LineSegment::getTokens )
                .flatMap( Collection::stream )
                .collect( Collectors.toCollection( LinkedList::new ) );
    }

    @Override
    public void appendLineBreak( String newline ) {
        branches.get( branches.size() - 1 ).appendLineBreak( newline );
    }

    @Override
    public int getIndentOffset() {
        return branches.get( 0 ).getIndentOffset();
    }

    @Override
    public void updateIndentOffset( int amount ) {
        branches.get( 0 ).updateIndentOffset( amount );
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public boolean canBeSplit() {
        return branches.size() > 1;
    }

    @Override
    public List<LineSegment> split(String newline, int numLineWrapTabs ) {
        // append linebreaks to each segment, except the last, which already has one
        IntStream.range( 0, branches.size() - 1 )
                .mapToObj( branches::get )
                .forEach( s -> s.appendLineBreak( newline ) );

        // indent second segment (which corresponds to what will be the first wrapped line)
        branches.get( 1 ).updateIndentOffset( numLineWrapTabs );

        // if necessary, unindent the final segment (which corresponds to what will be final wrapped line)
        TextToken startOfLastSegment = branches.get( branches.size() - 1 ).getFirstToken();
        if( startOfLastSegment.getLineBreakType() == BreakType.NON_BREAKING ) {
            startOfLastSegment.updateIndentOffset( -numLineWrapTabs );
        }

        return branches;
    }

    @Override
    public void loadDotFile( String parentId, DotFile dotfile ) {
        // generate a unique ID for this node and remove any dashes
        String uuid = UUID.randomUUID().toString();

        // use the linebreak type as this node's label
        dotfile.addNode( uuid, type.toString() );

        // add an edge from parent to this node
        if( parentId != null ) {
            dotfile.addEdge( parentId, uuid );
        }

        // process any branches
        branches.forEach( b -> b.loadDotFile( uuid, dotfile ) );
    }

}
