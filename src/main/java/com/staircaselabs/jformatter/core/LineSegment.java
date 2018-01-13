package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.core.LineWrapPriority.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class LineSegment {

    protected LineSegment parent;

    protected int width = 0;

    public LineSegment( LineSegment parent ) {
        this.parent = parent;
    }

    public abstract void add( TextToken token );

    public abstract void add( LineSegment branch );

    public abstract TextToken getFirstToken();

    public abstract List<TextToken> getTokens();

    public abstract void appendLineBreak( String newline );

    public abstract int getIndentOffset();

    public abstract boolean isLeaf();

    public abstract LineWrap getType();

    public abstract List<LineSegment> split( String newline );

    public abstract List<LineSegment> getChildren();

    public abstract void loadDotFile( String parentId, DotFile dotfile );

    public int getWidth() {
        return width;
    }

    @Override
    public String toString() {
        return getTokens().stream().map( TextToken::toString ).collect( Collectors.joining() );
    }

    public static LineSegment create( List<TextToken> tokens, LineSegment parent, Strategy strategy ) {
        Optional<LineWrapTag> maxPriorityTag = Optional.empty();
        List<Integer> positions = new ArrayList<>();

        // skip first token because it was the max priority tag of the parent node
        for( int idx = 1; idx < tokens.size(); idx++ ) {
            Optional<LineWrapTag> tag = tokens.get( idx ).getLineWrapTag();
            if( tag.isPresent() ) {
                int tagPriority = tag.map( t -> t.getPriority( strategy ) ).orElse( - 1 );
                int maxPriority = maxPriorityTag.map( t -> t.getPriority( strategy ) ).orElse( - 1 );
                if(  tagPriority > maxPriority ) {
                    maxPriorityTag = tag;
                    positions.clear();
                    positions.add( idx );
                } else if( tagPriority == maxPriority
                        && tag.get().getGroupId().equals( maxPriorityTag.get().getGroupId() ) ) {
                    positions.add( idx );
                }
            }
        }

        LineSegment node = null;

        if( maxPriorityTag.isPresent() ) {
            node = new BranchLineSegment( parent, maxPriorityTag.get().getType() );
            int pos = 0;
            for( int nextSegmentStart : positions ) {
                node.add( create( tokens.subList( pos, nextSegmentStart ), node, strategy ) );
                pos = nextSegmentStart;
            }

            if( pos < tokens.size() ) {
                node.add( create( tokens.subList(pos, tokens.size()), node, strategy ));
            }
        } else {
            node = new LeafLineSegment( parent );
            tokens.forEach( node::add );
        }

        return node;
    }

}
