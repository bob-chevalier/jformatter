package com.staircaselabs.jformatter.formatters;

import static com.staircaselabs.jformatter.formatters.Utils.COMMENTS_WHITESPACE_OR_NEWLINE;
import static com.staircaselabs.jformatter.formatters.Utils.WHITESPACE_OR_NEWLINE;
import static com.staircaselabs.jformatter.formatters.Utils.findIndexByTypeExclusion;
import static com.staircaselabs.jformatter.formatters.Utils.findLastIndexByTypeExclusion;
import static com.staircaselabs.jformatter.formatters.Utils.getLinebreak;
import static com.staircaselabs.jformatter.formatters.Utils.isComment;
import static com.staircaselabs.jformatter.formatters.Utils.tokenizeText;
import static com.staircaselabs.jformatter.formatters.Utils.tokensToText;
import java.util.List;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.TextToken;

public class HeaderFormatter {

    public static String format( String text ) throws FormatException {
        List<TextToken> tokens = tokenizeText( text );

        // find index of the first token in header, excluding leading whitespace and newlines
        int startIdx = findIndexByTypeExclusion( tokens, 0, WHITESPACE_OR_NEWLINE )
                .orElseThrow( () -> new FormatException( "File does not contain any class declarations" ) );

        // verify that a header exists in this file
        if( isComment( tokens.get( startIdx ) ) ) {
            // find index of first token representing actual code
            int codeIdx = findIndexByTypeExclusion( tokens, startIdx, COMMENTS_WHITESPACE_OR_NEWLINE )
                    .orElseThrow( () -> new FormatException( "File does not contain any class declarations" ) );

            // find index of last token in header, excluding trailing whitespace and newlines
            int stopIdx = findLastIndexByTypeExclusion( tokens, startIdx, codeIdx - 1, WHITESPACE_OR_NEWLINE )
                    .orElseThrow( () -> new FormatException( "File does not contain any class declarations" ) );

            // find an existing linebreak in the file so that inserted newlines have the same format
            String newline = getLinebreak( tokens );

            // rebuild text, ensuring that first line of code is preceded by two newlines
            return tokensToText( tokens.subList( startIdx, stopIdx + 1 ) )
                    + newline
                    + newline
                    + tokensToText( tokens.subList( codeIdx, tokens.size() ) );
        } else {
            // there is no header so just return original text as-is, excluding any leading whitespace and newlines
            return tokensToText( tokens.subList( startIdx, tokens.size() ) );

            //TODO potentially allow for the option of inserting a pre-defined header
        }
    }

}
