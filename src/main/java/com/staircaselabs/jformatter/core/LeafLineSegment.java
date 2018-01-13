package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.core.TextToken.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LeafLineSegment extends LineSegment {

    private final List<TextToken> tokens = new ArrayList<>();

    public LeafLineSegment( LineSegment parent ) {
        super( parent );
    }

    @Override
    public void add( TextToken token ) {
        tokens.add( token );
        width += token.getWidth();
    }

    @Override
    public void add( LineSegment segment ) {
       throw new UnsupportedOperationException( "Adding a LineSegment is not a valid LeafLineSegment operation" );
    }

    @Override
    public TextToken getFirstToken() {
        return tokens.get( 0 );
    }

    @Override
    public List<TextToken> getTokens() {
        return tokens;
    }

    @Override
    public void appendLineBreak( String newline ) {
        tokens.add( new TextToken( newline, TokenType.NEWLINE, 0, 0 ) );
    }

    @Override
    public int getIndentOffset() {
        return getFirstToken().getIndentOffset();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public LineWrap getType() {
        throw new UnsupportedOperationException( "LeafLineSegments do not have an associated LineWrap type" );
    }

    @Override
    public List<LineSegment> split(String newline ) {
        throw new UnsupportedOperationException( "LeafLineSegments cannot be split" );
    }

    @Override
    public List<LineSegment> getChildren() {
        throw new UnsupportedOperationException( "LeafLineSegments do not have child nodes" );
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
