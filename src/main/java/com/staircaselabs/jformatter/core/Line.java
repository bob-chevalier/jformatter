package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.core.LineWrapPriority.Strategy;
import com.staircaselabs.jformatter.core.TextToken.TokenType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Line {

    private int parentIndentLevel;
    private int lineWrapIndentOffset;
    private LineSegment segment;

    public Line( int parentIndentLevel, LineSegment segment ) {
        this.parentIndentLevel = parentIndentLevel;
        this.lineWrapIndentOffset = 0;
        this.segment = segment;
    }

    public Line( int parentIndentLevel, List<TextToken> tokens, Strategy strategy ) {
        this( parentIndentLevel, LineSegment.create( tokens, null, strategy ) );
    }

    public void printMarkup() {
        System.out.printf( segment.getTokens().stream().map( TextToken::toMarkupString ).collect( Collectors.joining() ) );
    }

    public int getParentIndentLevel() {
        return parentIndentLevel;
    }

    public void setLineWrapIndentOffset( int offset ) {
        lineWrapIndentOffset = offset;
    }

    public int getIndentLevel() {
        return parentIndentLevel + lineWrapIndentOffset + segment.getIndentOffset();
    }

    public List<TextToken> getTokens() {
        return segment.getTokens();
    }

    public boolean canBeSplit() {
        return !segment.isLeaf();
    }

    @Override
    public String toString() {
        return getWidth() == 0
                ? ""
                : Config.INSTANCE.indent.getText( getIndentLevel() ) + segment.toString();
    }

    public int getWidth() {
        int segmentWidth = segment.getWidth();
        return segmentWidth == 0 ? 0 : segmentWidth + Config.INSTANCE.indent.getWidth(getIndentLevel());
    }

    public Deque<Line> wrap(String newline ) {
        LineWrap wrapType = segment.getType();

        // replace this line's segment with the first child segment
        List<LineSegment> branches = segment.getChildren();
        segment = branches.remove( 0 );

        // append a new line-break to the first child, unless there's only one child, in which case it already has one
        if( !branches.isEmpty() ) {
            segment.appendLineBreak(newline);
        }

        if( wrapType == LineWrap.METHOD_ARG && !Config.INSTANCE.lineWrap.oneMethodArgPerLine ) {
            branches = flattenSegments( branches, wrapType );
        }

        // append linebreaks to each remaining segment, except the last, which already has one
        IntStream.range( 0, branches.size() - 1 )
                .mapToObj( branches::get )
                .forEach( s -> s.appendLineBreak( newline ) );

        // wrap each remaining segment in a new line
        Deque<Line> extraLines = new ArrayDeque<>();
        int prevIndentLevel = getIndentLevel();
        for( int i = 0; i < branches.size(); i++ ) {
            Line extraLine = new Line( prevIndentLevel, branches.get( i ) );

            if( i == 0 ) {
                // add an indent offset to the first wrapped line
               extraLine.setLineWrapIndentOffset( Config.INSTANCE.lineWrap.tabsToInsert( wrapType ) );
            } else if( i == branches.size() - 1 ) {
                // if necessary, unindent the last wrapped line
                boolean isGroupClosingSymbol = branches.get( i ).getFirstToken()
                                .getLineWrapTag()
                                .map( LineWrapTag::isGroupClosingSymbol )
                                .orElse( false );
                if( isGroupClosingSymbol ) {
                    extraLine.setLineWrapIndentOffset( -Config.INSTANCE.lineWrap.tabsToInsert( wrapType ) );
                }
            }

            prevIndentLevel = extraLine.getIndentLevel();
            extraLines.addLast( extraLine );
        }

        return extraLines;
    }

    public void writeDotFile( String path ) {
        DotFile dotfile = new DotFile();
        segment.loadDotFile( null, dotfile );
        dotfile.write( path );
    }

    private List<LineSegment> flattenSegments( List<LineSegment> segments, LineWrap wrapType ) {
        List<LineSegment> flattenedSegments = new ArrayList<>();

        int indentWidth = Config.INSTANCE.indent.getWidth( getIndentLevel() )
                + Config.INSTANCE.lineWrap.tabsToInsert( wrapType );
        LineSegment leaf = new LeafLineSegment( null );

        // if the last segment is a closing parenthesis, then we don't want to flatten it
        boolean hasGroupClosingSymbol = segments.get( segments.size() - 1 ).getFirstToken()
                .getLineWrapTag()
                .map( LineWrapTag::isGroupClosingSymbol )
                .orElse( false );

        int stopPos = hasGroupClosingSymbol ? segments.size() - 1 : segments.size();

        for( int i = 0; i < stopPos; i++ ) {
            LineSegment segment = segments.get( i );

            if( indentWidth + leaf.getWidth() + segment.getWidth() > Config.INSTANCE.lineWrap.maxLineWidth ) {
                // adding this segment will push us over the max line width
                if( !leaf.getTokens().isEmpty() ) {
                    // we've already added some segments to the leaf so add it to list and create a new leaf
                    flattenedSegments.add( leaf );
                    leaf = new LeafLineSegment( null );
                }

                // now that we've potentially created a new leaf, check whether this segment will fit again
                if( indentWidth + leaf.getWidth() + segment.getWidth() <= Config.INSTANCE.lineWrap.maxLineWidth ) {
                    // it fits so flatten it and add it to the leaf
                    segment.getTokens().forEach( leaf::add );
                } else {
                    // it still doesn't fit so just add it as a separate segment
                    flattenedSegments.add( segment );
                }
            } else {
                // there's room for this segment so flatten it and add it to the leaf
                segment.getTokens().forEach( leaf::add );
            }
        }

        // add the final leaf node, unless it's empty
        if( !leaf.getTokens().isEmpty() ) {
            flattenedSegments.add( leaf );
        }

        // add closing symbol segment (i.e. right-paren or right-brace), if it exists
        if( hasGroupClosingSymbol ) {
            flattenedSegments.add( segments.get( segments.size() - 1 ) );
        }

        return flattenedSegments;
    }

}
