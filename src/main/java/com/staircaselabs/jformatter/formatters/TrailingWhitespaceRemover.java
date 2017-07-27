package com.staircaselabs.jformatter.formatters;

import static com.staircaselabs.jformatter.core.TokenUtils.findNextIndexByType;
import static com.staircaselabs.jformatter.core.TokenUtils.tokenizeText;
import static com.staircaselabs.jformatter.core.TokenUtils.stringifyTokens;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TrailingWhitespaceRemover {

    public static String format( String text ) throws FormatException {
        List<TextToken> tokens = tokenizeText( text );
        List<Integer> indexesToRemove = new ArrayList<>();

        // find first whitespace token
        int idx = findNextIndexByType( tokens, 0, TokenType.WHITESPACE ).orElse( -1 );
        while( idx != -1 && idx < (tokens.size() - 1) ) {
            // check whether whitespace token is followed by a newline or EOF
            TokenType nextTokenType = tokens.get( idx + 1 ).type;
            if( nextTokenType == TokenType.NEWLINE || nextTokenType == TokenType.EOF ) {
                indexesToRemove.add( idx );
            }

            // find next whitespace token
            idx = findNextIndexByType( tokens, (idx + 1), TokenType.WHITESPACE ).orElse( -1 );
        }

        // we need to remove the tokens in reverse order to preserve the integrity of the indexes
        indexesToRemove.stream()
                .sorted( Comparator.reverseOrder() )
                .forEach( i -> tokens.remove( i.intValue() ) );

        return stringifyTokens( tokens );
    }

}
