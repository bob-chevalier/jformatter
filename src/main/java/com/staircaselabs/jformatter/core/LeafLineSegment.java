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

public class LeafLineSegment extends LineSegment {

    private final Queue<TextToken> tokens = new LinkedList<>();

    public LeafLineSegment( LineSegment parent ) {
        super( parent );
    }

    @Override
    public void add( TextToken token ) {
        tokens.add( token );
        width += token.getWidth();

        // keep track of the number of left/right parentheses that we encounter
        openParens += token.getType() == TokenType.LEFT_PAREN ? 1 : 0;
        openParens += token.getType() == TokenType.RIGHT_PAREN ? -1 : 0;
    }

    @Override
    public void add( LineSegment segment ) {
       throw new UnsupportedOperationException( "Adding a LineSegment is not a valid LeafLineSegment operation" );
    }

    @Override
    public BreakType getType() {
        return BreakType.NON_BREAKING;
    }

    @Override
    public TextToken getFirstToken() {
        return tokens.peek();
    }

    @Override
    public Queue<TextToken> getTokens() {
        return tokens;
    }

    @Override
    public void appendLineBreak( String newline ) {
        tokens.add( new TextToken( newline, TokenType.NEWLINE, 0, 0 ) );
    }

    @Override
    public int getIndentOffset() {
        return tokens.peek().getIndentOffset();
    }

    @Override
    public void updateIndentOffset( int amount ) {
        tokens.peek().updateIndentOffset(amount);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public boolean canBeSplit() {
        return false;
    }

    @Override
    public List<LineSegment> split(String newline, int numLineWrapTabs ) {
        throw new UnsupportedOperationException( "LeafLineSegments cannot be split" );
    }

    @Override
    public void loadDotFile( String parentId, DotFile dotfile ) {
        // generate a unique ID for this node and remove any dashes
        String uuid = UUID.randomUUID().toString();

        // strip off any newlines and double-quotes because Graphviz doesn't like them
        String label = tokens.stream().map( TextToken::toString ).collect( Collectors.joining() );
        label = label.replace( "\"", "" );

        // add a label entry for this node
        dotfile.addNode( uuid, label );

        // add an edge from parent to this node
        if( parentId != null ) {
            dotfile.addEdge( parentId, uuid );
        }
    }

}
