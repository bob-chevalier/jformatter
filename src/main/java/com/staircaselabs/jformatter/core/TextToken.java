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

    public TokenType getType() {
        return type;
    }

    public enum TokenType {
        AT,
        CATCH,
        COMMENT_LINE,
        COMMENT_BLOCK,
        COMMENT_JAVADOC,
        ELSE,
        EOF,
        FINALLY,
        GREATER_THAN,
        IMPORT,
        LEFT_BRACE,
        LEFT_BRACKET,
        LEFT_PAREN,
        LESS_THAN,
        NEWLINE,
        OTHER,
        RIGHT_BRACE,
        RIGHT_BRACKET,
        RIGHT_PAREN,
        SEMICOLON,
        STATIC,
        WHILE,
        WHITESPACE
    }

}
