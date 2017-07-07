package com.staircaselabs.jformatter.core;

public class TextToken {

    public final TokenType type;
    public final int start;
    public final int end;
    private final String text;

    public TextToken( String text, TokenType type, int start, int end ) {
        this.text = text;
        this.type = type;
        this.start = start;
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public enum TokenType {
        COMMENT_LINE,
        COMMENT_BLOCK,
        COMMENT_JAVADOC,
        EOF,
        IMPORT,
        NEWLINE,
        OTHER,
        WHITESPACE
    }

}
