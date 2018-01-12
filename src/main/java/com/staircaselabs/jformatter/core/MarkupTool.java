package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.Tree;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class MarkupTool {

    private final Input input;
    private int currentInclusive;
    private int endExclusive;

    public MarkupTool( Tree enclosingTree, Input input ) {
        this.input = input;
        this.currentInclusive = input.getFirstTokenIndex( enclosingTree );
        this.endExclusive = input.getLastTokenIndex( enclosingTree );
    }

    //TODO determine if this is ever called
    public Optional<String> tagLineWrapGroup( LineWrap wrapType, List<? extends Tree> list, String source ) {
        if( !list.isEmpty() ) {
            // create a tag with a new group ID for the first tree in the list
            LineWrapTag tag = new LineWrapTag( wrapType, source );
            tagTree( tag, list.get( 0 ) );

            // tag all remaining trees, using the group ID from the first tag
            for( int i = 1; i < list.size(); i++ ) {
               tagTree( new LineWrapTag( tag.getGroupId(), wrapType, source ), list.get( i ) );
            }

            return Optional.of( tag.getGroupId() );
        }

        return Optional.empty();
    }

    public void tagLineWrapGroupWithClosingParen( LineWrap wrapType, List<? extends Tree> list, String source ) {
        Optional<String> groupId = tagLineWrapGroup( wrapType, list, source );
        if( groupId.isPresent() ) {
            tagToken( new LineWrapTag( groupId.get(), wrapType, source ), TokenType.RIGHT_PAREN );
        }
    }

    public void tagLineWrap( LineWrap wrapType, Tree tree, String source ) {
        tagTree( new LineWrapTag( wrapType, source ), tree );
    }

    public void tagLineWrap( LineWrap wrapType, TokenType tokenType, String source ) {
        tagToken( new LineWrapTag( wrapType, source ), tokenType );
    }

    public void indentBracedBlock( Tree tree ) {
        if( tree != null ) {
            int blockStart = input.getFirstTokenIndex( tree );
            int blockEnd = input.getLastTokenIndex( tree );

            // find opening and closing brace positions
            int leftBrace = input.findNext( blockStart, blockEnd, TokenType.LEFT_BRACE )
                    .orElseThrow( () -> new RuntimeException( "Unexpected missing left brace." ) );
            int rightBrace = input.findPrev( blockStart, blockEnd, TokenType.RIGHT_BRACE )
                    .orElseThrow( () -> new RuntimeException( "Unexpected missing right brace." ) );

            if( indentNextLine( leftBrace, rightBrace ) ) {
                input.tokens.get( rightBrace ).updateIndentOffset( -1 );
            }
        }
    }

    public boolean indentNextLine( int startInclusive, int endExclusive ) {
        return adjustIndentAfterNextLine( startInclusive, endExclusive, 1 );
    }

    public boolean unindentNextLine( int startInclusive, int endExclusive ) {
        return adjustIndentAfterNextLine( startInclusive, endExclusive, -1 );
    }

    public boolean adjustIndentAfterNextLine( int startInclusive, int endExclusive, int indentOffset ) {
        // find first newline within range
        OptionalInt firstNewline = input.findNext( startInclusive, endExclusive, TokenType.NEWLINE );
        if( firstNewline.isPresent() ) {

            // find first code or comment after newline
            OptionalInt afterNewline = input.findNextByExclusion(
                    firstNewline.getAsInt(),
                    endExclusive,
                    TokenType.WHITESPACE,
                    TokenType.NEWLINE
            );

            if( afterNewline.isPresent() ) {
                input.tokens.get( afterNewline.getAsInt() ).updateIndentOffset( indentOffset );
                return true;
            }

        }
        return false;
    }

    private void tagTree( LineWrapTag tag, Tree tree ) {
        input.getFirstToken( tree ).allowLineWrap( tag );
        currentInclusive = input.getLastTokenIndex( tree );
    }

    private void tagToken( LineWrapTag tag, TokenType tokenType ) {
        Optional<TextToken> token = input.findNextToken( currentInclusive, endExclusive, tokenType );
        if( token.isPresent() ) {
            token.get().allowLineWrap( tag );
            currentInclusive = token.get().endExclusive;
        }
    }

}
