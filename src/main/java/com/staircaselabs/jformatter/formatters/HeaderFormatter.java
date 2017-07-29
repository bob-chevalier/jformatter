package com.staircaselabs.jformatter.formatters;

import static com.staircaselabs.jformatter.core.TokenUtils.findNextIndexByTypeExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.findPrevIndexByTypeExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.getLinebreak;
import static com.staircaselabs.jformatter.core.TokenUtils.isComment;
import static com.staircaselabs.jformatter.core.TokenUtils.tokenizeText;
import static com.staircaselabs.jformatter.core.TokenUtils.stringifyTokens;
import java.util.List;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;

public class HeaderFormatter {

    private static final TokenType[] WHITESPACE_OR_NEWLINE = {
            TokenType.WHITESPACE,
            TokenType.NEWLINE
    };

    private static final TokenType[] COMMENTS_WHITESPACE_OR_NEWLINE = {
            TokenType.COMMENT_BLOCK,
            TokenType.COMMENT_JAVADOC,
            TokenType.COMMENT_LINE,
            TokenType.WHITESPACE,
            TokenType.NEWLINE
    };

    public static String format( String text ) throws FormatException {
        List<TextToken> tokens = tokenizeText( text );

        // find index of the first token in header, excluding leading whitespace and newlines
        int startIdx = findNextIndexByTypeExclusion( tokens, 0, WHITESPACE_OR_NEWLINE )
                .orElseThrow( () -> new FormatException( "File does not contain any class declarations" ) );

        // verify that a header exists in this file
        if( isComment( tokens.get( startIdx ) ) ) {
            // find index of first token representing actual code
            int codeIdx = findNextIndexByTypeExclusion( tokens, startIdx, COMMENTS_WHITESPACE_OR_NEWLINE )
                    .orElseThrow( () -> new FormatException( "File does not contain any class declarations" ) );

            // find index of last token in header, excluding trailing whitespace and newlines
            int stopIdx = findPrevIndexByTypeExclusion( tokens, startIdx, codeIdx - 1, WHITESPACE_OR_NEWLINE )
                    .orElseThrow( () -> new FormatException( "File does not contain any class declarations" ) );

            // find an existing linebreak in the file so that inserted newlines have the same format
            String newline = getLinebreak( tokens );

            // rebuild text, ensuring that first line of code is preceded by two newlines
            return stringifyTokens( tokens, startIdx, (stopIdx + 1) )
                    + newline
                    + newline
                    + stringifyTokens( tokens, codeIdx );
        } else {
            // there is no header so just return original text as-is, excluding any leading whitespace and newlines
            return stringifyTokens( tokens, startIdx );

            //TODO potentially allow for the option of inserting a pre-defined header
        }
    }

}
