package com.staircaselabs.jformatter.formatters.linewrap;

import com.staircaselabs.jformatter.core.DotFile;
import com.staircaselabs.jformatter.core.LineWrap;
import com.staircaselabs.jformatter.core.TextToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BranchLineSegment extends LineSegment {

    protected final LineWrap type;

    protected final List<LineSegment> branches = new ArrayList<>();

    public BranchLineSegment( LineSegment parent, LineWrap type ) {
        super( parent );
        this.type = type;
    }

    public void add( LineSegment segment ) {
        segment.parent = this;
        branches.add( segment );

        // we want this node's offset to be equal to the last branch's offset
        offset = segment.offset;
    }

    public void insertLineBreaks( String newline ) {
        // append linebreaks to each segment, except the last
        IntStream.range( 0, branches.size() - 1 )
                .mapToObj( branches::get )
                .map( LineSegment::getLastLeaf )
                .map( LeafLineSegment.class::cast )
                .forEach( s -> s.appendLineBreak( newline ) );
    }

    @Override
    public List<TextToken> getTokens() {
        return branches.stream()
                .map( LineSegment::getTokens )
                .flatMap( Collection::stream )
                .collect( Collectors.toList() );
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public int getLeafCount() {
        return branches.stream().mapToInt( LineSegment::getLeafCount ).sum();
    }

    @Override
    public LineSegment getFirstLeaf() {
        return branches.get( 0 ).getFirstLeaf();
    }

    @Override
    public LineSegment getLastLeaf() {
        return branches.get( branches.size() - 1 ).getLastLeaf();
    }

    @Override
    public void loadDotFile( String parentId, DotFile dotfile ) {
        // generate a unique ID for this node and remove any dashes
        String uuid = UUID.randomUUID().toString();

        dotfile.addNode( uuid, "(" + offset + ")" + type.toString() );

        // add an edge from parent to this node
        if( parentId != null ) {
            dotfile.addEdge( parentId, uuid );
        }

        // process any branches
        branches.forEach( b -> b.loadDotFile( uuid, dotfile ) );
    }

    @Override
    public String toString() {
        return branches.stream().map( LineSegment::toString ).collect( Collectors.joining() );
    }

}
