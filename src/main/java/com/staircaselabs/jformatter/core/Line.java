package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.core.LineWrapPriority.Strategy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

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
        return segment.canBeSplit();
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
        List<LineSegment> segments = segment.split( newline );

        // replace this line's segment with the first segment
        segment = segments.get( 0 );

        // wrap each remaining segment in a new line
        Deque<Line> extraLines = new ArrayDeque<>();
        int prevIndentLevel = getIndentLevel();
        for( int i = 1; i < segments.size(); i++ ) {
            Line extraLine = new Line( prevIndentLevel, segments.get( i ) );

            if( i == 1 ) {
                // add an indent offset to the first wrapped line
               extraLine.setLineWrapIndentOffset( Config.INSTANCE.lineWrap.numTabsAfterLineBreak );
            } else if( i == segments.size() - 1 ) {
                // if necessary, unindent the last wrapped line
                if( segments.get( i ).getFirstToken().getType() == TextToken.TokenType.RIGHT_PAREN ) {
                    extraLine.setLineWrapIndentOffset( -Config.INSTANCE.lineWrap.numTabsAfterLineBreak );
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

}
