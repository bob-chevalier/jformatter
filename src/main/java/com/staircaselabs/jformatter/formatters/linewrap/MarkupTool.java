package com.staircaselabs.jformatter.formatters.linewrap;

import com.staircaselabs.jformatter.config.Config;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.LineWrap;
import com.staircaselabs.jformatter.core.LineWrapTag;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.Tree;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

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
    public Optional<String> tagLineWrapGroup(LineWrap wrapType, List<? extends Tree> list, String source ) {
        if( list != null && !list.isEmpty() ) {
            // create a tag with a new group ID for the first tree in the list
            String groupId = tagLineWrap( wrapType, list.get( 0 ), source );

            // tag all remaining trees, using the group ID from the first tag
            for( int i = 1; i < list.size(); i++ ) {
                tagTree(new LineWrapTag(groupId, wrapType, source), list.get(i));
            }

            return Optional.of( groupId );
        }

        return Optional.empty();
    }

    public void tagLineWrapGroupWithClosingBrace( LineWrap wrapType, List<? extends Tree> list, String source ) {
        Optional<String> groupId = tagLineWrapGroup( wrapType, list, source );
        if( groupId.isPresent() && Config.INSTANCE.lineWrap.closingBracesOnNewLine ) {
            LineWrapTag tag = new LineWrapTag( groupId.get(), wrapType, source );
            tag.closeGroup();
            tagToken( tag, TokenType.RIGHT_BRACE );
        }
    }

    public void tagLineWrapGroupWithClosingParen( LineWrap wrapType, List<? extends Tree> list, String source ) {
        Optional<String> groupId = tagLineWrapGroup( wrapType, list, source );
        if( groupId.isPresent() && Config.INSTANCE.lineWrap.closingParensOnNewLine ) {
            LineWrapTag tag = new LineWrapTag( groupId.get(), wrapType, source );
            tag.closeGroup();
            tagToken( tag, TokenType.RIGHT_PAREN );
        }
    }

    public void tagLineWrapGroupWithClosingParen( LineWrap wrapType, TokenType type, String source ) {
        // tag first instance of token in the group
        String groupId = tagLineWrap( wrapType, type, source );

        // tag additional instances of token
        LineWrapTag tag = null;
        do {
            tag = new LineWrapTag( groupId, wrapType, source );
        } while( tagToken( tag, type ) );

        // tag closing parenthesis, if appropriate
        if( Config.INSTANCE.lineWrap.closingParensOnNewLine ) {
            tag = new LineWrapTag( groupId, wrapType, source );
            tag.closeGroup();
            tagToken( tag, TokenType.RIGHT_PAREN );
        }
    }

    public String tagLineWrap( LineWrap wrapType, Tree tree, String source ) {
        LineWrapTag tag = new LineWrapTag( wrapType, source );
        tagTree( tag, tree );
        return tag.getGroupId();
    }

    public String tagLineWrap( LineWrap wrapType, TokenType tokenType, String source ) {
        LineWrapTag tag = new LineWrapTag( wrapType, source );
        tagToken( tag, tokenType );
        return tag.getGroupId();
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

    private boolean tagToken( LineWrapTag tag, TokenType tokenType ) {
        OptionalInt pos = input.findNext( currentInclusive, endExclusive, tokenType );
        if( pos.isPresent() ) {
            input.tokens.get( pos.getAsInt() ).allowLineWrap( tag );
            currentInclusive = pos.getAsInt() + 1;
            return true;
        }

        return false;
    }

}
