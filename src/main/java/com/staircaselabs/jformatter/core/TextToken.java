package com.staircaselabs.jformatter.core;

public class TextToken {

    private final String originalText;
    private String text;
    private final TokenType type;
    private int start;
    private int end;

    public TextToken( String originalText, TokenType type, int start, int end ) {
        this.text = this.originalText = originalText;
        this.type = type;
        this.start = start;
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public String getOriginalText() {
        return originalText;
    }

    public TokenType getType() {
        return type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public enum TokenType {
        IMPORT,
        WHITESPACE,
        COMMENT_LINE,
        COMMENT_BLOCK,
        COMMENT_JAVADOC,
        NEWLINE,
        OTHER
    }

}
