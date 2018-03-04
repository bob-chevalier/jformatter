package com.staircaselabs.jformatter.formatters.linewrap;

import com.staircaselabs.jformatter.core.LineWrapPriority.Strategy;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.debug.DotFile;

import java.util.List;
import java.util.stream.Collectors;

public class Line {

    private LineSegment segment;

    public Line(int prevLineOffset, List<TextToken> tokens, String newline, Strategy strategy ) {
        segment = LineSegment.create( prevLineOffset, tokens, newline, null, strategy );
    }

    public String getMarkupString() {
        return segment.getTokens().stream().map( TextToken::toMarkupString ).collect( Collectors.joining() );
    }

    public int getIndentLevel() {
        return segment.getFirstLeaf().offset;
    }

    public List<TextToken> getTokens() {
        return segment.getTokens();
    }

    public boolean isWrapped() {
        return !segment.isLeaf();
    }

    public int getLineWrapCount() {
        return segment.getLeafCount();
    }

    @Override
    public String toString() {
        return segment.toString();
    }

    public void writeDotFile( String path ) {
        DotFile dotfile = new DotFile();
        segment.loadDotFile( null, dotfile );
        dotfile.write( path );
    }

}
