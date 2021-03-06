package com.staircaselabs.jformatter.formatters.whitespace;

import com.staircaselabs.jformatter.core.Formatter;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.staircaselabs.jformatter.debug.FormatException;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.staircaselabs.jformatter.core.TokenUtils.findNext;
import static com.staircaselabs.jformatter.core.TokenUtils.findNextByExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.findPrevByExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.tokenizeText;

public class WhitespaceFormatter implements Formatter {

    private static final TokenType[] WHITESPACE_OR_EOF = { TokenType.WHITESPACE, TokenType.EOF };

    @Override
    public String format( String text ) throws FormatException {
        List<TextToken> tokens = tokenizeText( text );
        List<TextToken> validTokens = new ArrayList<>();

        // skip leading whitespace from document
        OptionalInt lineStart = findNextByExclusion( tokens, 0, TokenType.WHITESPACE, TokenType.NEWLINE, TokenType.EOF );

        while( lineStart.isPresent() ) {
            // find next newline token
            int newline = findNext( tokens, lineStart.getAsInt(), TokenType.NEWLINE, TokenType.EOF )
                    .orElseThrow( () -> new RuntimeException( "Unterminated line." ) );

            // skip any whitespace that preceeds newline
            int lineEnd = findPrevByExclusion( tokens, lineStart.getAsInt(), newline, TokenType.WHITESPACE )
                    .orElse( newline );

            IntStream.rangeClosed( lineStart.getAsInt(), lineEnd ).mapToObj( tokens::get ).forEach( validTokens::add );
            if( lineEnd != newline ) {
                validTokens.add( tokens.get( newline ) );
            }

            lineStart = findNextByExclusion(tokens, newline + 1, getLeadingTokensToExclude());
        }

        return validTokens.stream().map( TextToken::toString ).collect( Collectors.joining() );
    }

    protected TokenType[] getLeadingTokensToExclude() {
        return WHITESPACE_OR_EOF;
    }

}
