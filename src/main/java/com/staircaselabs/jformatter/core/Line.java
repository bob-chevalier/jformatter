package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.staircaselabs.jformatter.core.CompilationUnitUtils.getCompilationUnit;
import static com.staircaselabs.jformatter.core.CompilationUnitUtils.isValid;
import static com.staircaselabs.jformatter.core.TokenUtils.tokenizeText;

public class Line {

    private final Indent indent;
    private int parentIndentLevel;
    private LineSegment segment;

    public Line( Indent indent, int parentIndentLevel, LineSegment segment ) {
        this.indent = indent;
        this.parentIndentLevel = parentIndentLevel;
        this.segment = segment;
    }

    public Line( Indent indent, int parentIndentLevel, Queue<TextToken> tokens ) {
        this( indent, parentIndentLevel, LineSegment.create( tokens, null ) );
    }

    public void printMarkup() {
        System.out.printf( segment.getTokens().stream().map( TextToken::toMarkupString ).collect( Collectors.joining() ) );
    }

    public int getIndentLevel() {
        return parentIndentLevel + segment.getIndentOffset();
    }

    public boolean canBeSplit() {
        return segment.hasMultipleBranches();
    }

    @Override
    public String toString() {
        return getWidth() == 0
                ? ""
                : indent.getText( getIndentLevel() ) + segment.toString();
    }

    public int getWidth() {
        int segmentWidth = segment.getWidth();
        return segmentWidth == 0 ? 0 : segmentWidth + indent.getWidth(getIndentLevel());
    }

    public Deque<Line> wrap(String newline ) {
        List<LineSegment> segments = segment.split( newline, indent.getLineWrapTabs() );

        // replace this line's segment with the first segment
        segment = segments.get( 0 );

        // wrap each remaining segment in a new line
        Deque<Line> extraLines = new ArrayDeque<>();
        int prevIndentLevel = getIndentLevel();
        for( int i = 1; i < segments.size(); i++ ) {
            Line extraLine = new Line( indent, prevIndentLevel, segments.get( i ) );
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
