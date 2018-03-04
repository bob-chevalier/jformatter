package com.staircaselabs.jformatter.formatters.linewrap;

import com.staircaselabs.jformatter.config.Config;
import com.staircaselabs.jformatter.core.LineWrap;
import com.staircaselabs.jformatter.core.LineWrapPriority.Strategy;
import com.staircaselabs.jformatter.core.LineWrapTag;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.debug.DotFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class LineSegment {

    protected LineSegment parent;

    protected int offset = 0;

    public LineSegment( LineSegment parent ) {
        this.parent = parent;
    }

    public abstract List<TextToken> getTokens();

    public abstract boolean isLeaf();

    public abstract int getLeafCount();

    public abstract LineSegment getFirstLeaf();

    public abstract LineSegment getLastLeaf();

    public abstract void loadDotFile( String parentId, DotFile dotfile );

    public static LineSegment create( int prevOffset, List<TextToken> tokens, String newline, LineSegment parent, Strategy strategy ) {
        int maxLineWidth = Config.INSTANCE.lineWrap.maxLineWidth;
        int taggedIndent = tokens.get( 0 ).getIndentOffset();
        int indentWidth = Config.INSTANCE.indent.getWidth( prevOffset + taggedIndent );
        int tokensWidth = tokens.stream().mapToInt( TextToken::getWidth ).sum();

        if( indentWidth + tokensWidth <= maxLineWidth ) {
            // we can fit all the tokens on a single line, so just add them all to a leaf node
            return new LeafLineSegment( prevOffset, tokens, parent );
        } else {
            // we can't fit all the tokens on a single line, so find the most desirable place(s) to split them
            Optional<LineWrapTag> maxPriorityTag = Optional.empty();
            List<Integer> splitPositions = new ArrayList<>();

            // skip the first token because if it contains a line-wrap tag, we'll get stuck in an infinite loop
            // (this will be true whenever we call this method recursively on a subset of tokens from the original line)
            for( int idx = 1; idx < tokens.size(); idx++ ) {
                Optional<LineWrapTag> tag = tokens.get( idx ).getLineWrapTag();
                if( tag.isPresent() ) {
                    int tagPriority = tag.map( t -> t.getPriority( strategy ) ).orElse( - 1 );
                    int maxPriority = maxPriorityTag.map( t -> t.getPriority( strategy ) ).orElse( - 1 );
                    if( tagPriority > maxPriority ) {
                        maxPriorityTag = tag;
                        splitPositions.clear();
                        splitPositions.add( idx );
                    } else if( tagPriority == maxPriority
                            && tag.get().getGroupId().equals( maxPriorityTag.get().getGroupId() ) ) {
                        splitPositions.add( idx );
                    }
                }
            }

            if( maxPriorityTag.isPresent() ) {
                // add a split position to represent the last token of the final segment
                splitPositions.add( tokens.size() );

                // create a node to hold all of the splits
                LineWrap wrapType = maxPriorityTag.get().getType();
                BranchLineSegment node = new BranchLineSegment( parent, wrapType );

                // add the first child
                int start = 0;
                int end = splitPositions.get( 0 );
                LineSegment firstChild = create( prevOffset, tokens.subList( start, end ), newline, node, strategy );
                start = end;
                node.add( firstChild );

                // update the base offset for any additional wrapped lines using first child's offset and wrap type
                int wrapOffset = firstChild.offset + Config.INSTANCE.lineWrap.tabsToInsert( wrapType );

                if( (wrapType == LineWrap.METHOD_ARG && !Config.INSTANCE.lineWrap.oneMethodArgPerLine)
                        || (wrapType == LineWrap.UNION && !Config.INSTANCE.lineWrap.oneUnionElementPerLine)
                        || (wrapType == LineWrap.ARRAY && !Config.INSTANCE.lineWrap.oneArrayElementPerLine) ) {
                    // try to fit as many segments as possible on a single line
                    addMinimalNumberOfSegments(
                            start,
                            splitPositions.subList( 1, splitPositions.size() ),
                            tokens,
                            wrapOffset,
                            wrapType,
                            newline,
                            node,
                            strategy
                    );
                } else {
                    // add a child for each remaining split position
                    addSegmentForEverySplit(
                            start,
                            splitPositions.subList( 1, splitPositions.size() ),
                            tokens,
                            wrapOffset,
                            wrapType,
                            newline,
                            node,
                            strategy
                    );
                }

                // insert linebreaks, where appropriate
                node.insertLineBreaks( newline );
                return node;
            } else {
                // we couldn't find a place to split the tokens, so just let this line exceed the max line width
                return new LeafLineSegment( prevOffset, tokens, parent );
            }
        }
    }

    private static void addSegmentForEverySplit(
            int start,
            List<Integer> splits,
            List<TextToken> tokens,
            int wrapOffset,
            LineWrap wrapType,
            String newline,
            BranchLineSegment parent,
            Strategy strategy
    ) {
        for( int end : splits ) {
            if( tokens.get( start ).getLineWrapTag().map( LineWrapTag::closesGroup ).orElse( false ) ) {
                // this is a right paren or right brace that we want to align with the opening segment
                wrapOffset -= Config.INSTANCE.lineWrap.tabsToInsert( wrapType );
            }
            LineSegment child = create( wrapOffset, tokens.subList( start, end ), newline, parent, strategy );
            parent.add( child );
            start = end;
        }
    }

    private static void addMinimalNumberOfSegments(
        int start,
        List<Integer> splits,
        List<TextToken> tokens,
        int wrapOffset,
        LineWrap wrapType,
        String newline,
        BranchLineSegment parent,
        Strategy strategy
    ) {
        LeafLineSegment leaf = new LeafLineSegment( wrapOffset, parent );
        int maxLineWidth = Config.INSTANCE.lineWrap.maxLineWidth;

        for( int end : splits ) {
            List<TextToken> nextSegmentTokens = tokens.subList( start, end );
            int nextSegmentWidth = nextSegmentTokens.stream().mapToInt( TextToken::getWidth ).sum();
            boolean nextSegmentClosesGroup = nextSegmentTokens.get( 0 )
                    .getLineWrapTag()
                    .map( LineWrapTag::closesGroup )
                    .orElse( false );

            if( leaf.getWidth() + nextSegmentWidth > maxLineWidth || nextSegmentClosesGroup ) {
                // next segment won't fit on the current line (or we want it on its own line)
                if( !leaf.isEmpty() ) {
                    // add the existing leaf and create a new leaf
                    parent.add( leaf );
                    leaf = new LeafLineSegment( wrapOffset, parent );
                }

                if( nextSegmentClosesGroup ) {
                    // force this segment to be aligned with the opening segment
                    wrapOffset -= Config.INSTANCE.lineWrap.tabsToInsert( wrapType );
                }

                if( Config.INSTANCE.indent.getWidth( wrapOffset ) + nextSegmentWidth > maxLineWidth
                        || nextSegmentClosesGroup ) {
                    // segment is too big by itself (or it's a closing segment) so either
                    // try to break it up further, or at least force it to be on its own line
                    parent.add( create( wrapOffset, nextSegmentTokens, newline, parent, strategy ) );
                } else {
                    // segment fits by itself, so add it to the new leaf
                    leaf.addTokens( nextSegmentTokens );
                }
            } else {
                // there's room for this segment, so add it to the existing leaf
                leaf.addTokens( nextSegmentTokens );
            }
            start = end;
        }

        if( !leaf.isEmpty() ) {
            parent.add( leaf );
        }
    }

}
