package com.staircaselabs.jformatter.formatters.whitespace;

import com.staircaselabs.jformatter.core.TextToken.TokenType;

public class TrailingWhitespaceFormatter extends WhitespaceFormatter {

    private static final TokenType[] EOF = { TokenType.EOF };

    @Override
    protected TokenType[] getLeadingTokensToExclude() {
        return EOF;
    }

}
