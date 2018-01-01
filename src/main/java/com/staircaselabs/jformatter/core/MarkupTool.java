package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.core.LineBreak;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
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

    public void tagUnifiedUnjustifiedBreaks( List<? extends Tree> list, TokenType closingTokenType, String source ) {
        if( !list.isEmpty() ) {
            tagTreeLineBreak( LineBreak.UNIFIED_FIRST, list.get( 0 ), source );

            for( int i = 1; i < list.size(); i++ ) {
                tagTreeLineBreak( LineBreak.UNIFIED, list.get( i ), source );
            }

            Optional<TextToken> closingToken = input.findNextToken( currentInclusive, endExclusive, closingTokenType );
            if( closingToken.isPresent() ) {
               closingToken.get().setLineBreakTag( LineBreak.UNIFIED_LAST_UNJUSTIFIED, source );
               currentInclusive = closingToken.get().endExclusive;
            }
        }
    }

    public void tagUnifiedBreaks( List<? extends Tree> list, String source ) {
        if( !list.isEmpty() ) {
            tagTreeLineBreak( LineBreak.UNIFIED_FIRST, list.get( 0 ), source );

            for( int i = 1; i < list.size() - 1; i++ ) {
                tagTreeLineBreak( LineBreak.UNIFIED, list.get( i ), source );
            }

            tagTreeLineBreak( LineBreak.UNIFIED_LAST, list.get( list.size() - 1 ), source );
        }
    }

    public void tagIndependentBreak( Tree tree, String source ) {
        tagTreeLineBreak( LineBreak.INDEPENDENT, tree, source );
    }

    public void tagIndependentBreak( TokenType tokenType, String source ) {
        Optional<TextToken> token = input.findNextToken( currentInclusive, endExclusive, tokenType );
        if( token.isPresent() ) {
            token.get().setLineBreakTag( LineBreak.INDEPENDENT, source );
            currentInclusive = token.get().endExclusive;
        }
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

    private void tagTreeLineBreak( LineBreak breakType, Tree tree, String source ) {
        input.getFirstToken( tree ).setLineBreakTag( breakType, source );
        currentInclusive = input.getLastTokenIndex( tree );
    }

}
