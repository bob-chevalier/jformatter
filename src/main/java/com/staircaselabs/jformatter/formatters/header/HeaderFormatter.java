package com.staircaselabs.jformatter.formatters.header;

import static com.staircaselabs.jformatter.core.TokenUtils.findNextByExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.findPrevByExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.getLinebreak;
import static com.staircaselabs.jformatter.core.TokenUtils.isComment;
import static com.staircaselabs.jformatter.core.TokenUtils.tokenizeText;
import static com.staircaselabs.jformatter.core.TokenUtils.stringifyTokens;
import java.util.List;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.Formatter;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;

public class HeaderFormatter implements Formatter {

    private static final TokenType[] WS_NEWLINE_OR_COMMENT = {
            TokenType.COMMENT_BLOCK,
            TokenType.COMMENT_JAVADOC,
            TokenType.COMMENT_LINE,
            TokenType.WHITESPACE,
            TokenType.NEWLINE
    };

    @Override
    public String format( String text ) throws FormatException {
        List<TextToken> tokens = tokenizeText( text );

        // find first non-whitespace, non-newline token
        int headerStart = findNextByExclusion( tokens, 0, TokenType.WHITESPACE, TokenType.NEWLINE )
                .orElseThrow( () -> new RuntimeException( "Found empty file." ) );

        // verify that a header exists in this file
        if( isComment( tokens.get( headerStart ) ) ) {
            // find first token that represents source code
            int codeStart = findNextByExclusion( tokens, headerStart, WS_NEWLINE_OR_COMMENT )
                    .orElseThrow( () -> new RuntimeException( "Found file with no source code." ) );

            // find the end of the header
            int headerStop = findPrevByExclusion( tokens, headerStart, codeStart, TokenType.WHITESPACE, TokenType.NEWLINE )
                    .orElseThrow( () -> new FormatException( "Unexpected missing token." ) );

            // find an existing linebreak in the file so that inserted newlines have the same format
            String newline = getLinebreak( tokens );

            // rebuild text, ensuring that first line of code is preceded by two newlines
            return stringifyTokens( tokens, headerStart, (headerStop + 1) )
                    + newline
                    + stringifyTokens( tokens, codeStart );
        } else {
            // there is no header so just return original text as-is, excluding any leading whitespace and newlines
            return stringifyTokens( tokens, headerStart );
        }
    }

}
