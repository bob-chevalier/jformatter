package com.staircaselabs.jformatter.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BranchLineSegment extends LineSegment {

    private final LineWrap type;
    private List<LineSegment> branches = new ArrayList<>();

    public BranchLineSegment( LineSegment parent, LineWrap type ) {
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
    }

    @Override
    public TextToken getFirstToken() {
        return branches.get( 0 ).getFirstToken();
    }

    @Override
    public List<TextToken> getTokens() {
        return branches.stream()
                .map( LineSegment::getTokens )
                .flatMap( Collection::stream )
                .collect( Collectors.toList() );
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
    public boolean isLeaf() {
        return false;
    }

    @Override
    public LineWrap getType() {
        return type;
    }

    @Override
    public List<LineSegment> split(String newline ) {
        // append linebreaks to each segment, except the last, which already has one
        IntStream.range( 0, branches.size() - 1 )
                .mapToObj( branches::get )
                .forEach( s -> s.appendLineBreak( newline ) );

        return branches;
    }

    @Override
    public List<LineSegment> getChildren() {
        return branches;
    }

    @Override
    public void loadDotFile( String parentId, DotFile dotfile ) {
        // generate a unique ID for this node and remove any dashes
        String uuid = UUID.randomUUID().toString();

        dotfile.addNode( uuid, type.toString() );

        // add an edge from parent to this node
        if( parentId != null ) {
            dotfile.addEdge( parentId, uuid );
        }

        // process any branches
        branches.forEach( b -> b.loadDotFile( uuid, dotfile ) );
    }

}
