package com.staircaselabs.jformatter.core;

import java.text.DecimalFormat;

public class TextToken {

    private final DecimalFormat decimalFormat = new DecimalFormat( "+#;-#" );
    public final int beginInclusive;
    public final int endExclusive;
    private final String text;
    private final TokenType type;
    private BreakType lineBreakType = BreakType.NON_BREAKING;
    private String lineBreakSource = "default";
    private int indentOffset = 0;
    private static final boolean VERBOSE_MARKUP = false;

    public TextToken( String text, TokenType type, int beginInclusive, int endExclusive ) {
        this.text = text;
        this.type = type;
        this.beginInclusive = beginInclusive;
        this.endExclusive = endExclusive;
    }

    public TokenType getType() {
        return type;
    }

    public void setLineBreakTag(BreakType breakType, String source ) {
//        if( this.lineBreakType.canBeOverriden( lineBreakType ) ) {
            this.lineBreakType = breakType;
            lineBreakSource = source;
//        }
    }

    public BreakType getLineBreakType() {
        return lineBreakType;
    }

    public void updateIndentOffset( int amount ) {
        indentOffset += amount;
    }

    public int getIndentOffset() {
        return indentOffset;
    }

    public int getWidth() {
        // ignore newlines when calculating the width of a line
        return (type == TokenType.NEWLINE
                ? 0
                : endExclusive - beginInclusive);
    }

    @Override
    public String toString() {
        return text;
    }

    public String toMarkupString() {
        String indentLabel = indentOffset == 0 ? "" : decimalFormat.format( indentOffset ) + ":";
        String srcLabel = VERBOSE_MARKUP ? ":" + lineBreakSource : "";
        String markupLabel = (indentOffset == 0 && lineBreakType == BreakType.NON_BREAKING)
                ? ""
                : String.format( "[%s%s%s]", indentLabel, lineBreakType.toString(), srcLabel );
        return  markupLabel + toString();
    }

    public enum TokenType {
        ABSTRACT,
        AND,
        AND_ASSIGNMENT,
        ARROW,
        AT,
        ASSIGNMENT,
        BREAK,
        CATCH,
        CASE,
        CLASS,
        COLON,
        COMMA,
        COMMENT_LINE,
        COMMENT_BLOCK,
        COMMENT_JAVADOC,
        CONDITIONAL_AND,
        CONDITIONAL_OR,
        CONTINUE,
        DEFAULT,
        DIVIDE,
        DIVIDE_ASSIGNMENT,
        DO,
        DOT,
        ELSE,
        EOF,
        EQUALS,
        EXTENDS,
        FINAL,
        FINALLY,
        FOR,
        GREATER_THAN,
        GREATER_THAN_EQUAL,
        IF,
        IMPLEMENTS,
        IMPORT,
        INSTANCE_OF,
        LEFT_BRACE,
        LEFT_BRACKET,
        LEFT_PAREN,
        LEFT_SHIFT,
        LEFT_SHIFT_ASSIGNMENT,
        LESS_THAN,
        LESS_THAN_EQUAL,
        MINUS,
        MINUS_ASSIGNMENT,
        MOD,
        MOD_ASSIGNMENT,
        MULTIPLY,
        MULTIPLY_ASSIGNMENT,
        NEW,
        NEWLINE,
        NOT_EQUAL,
        OTHER,
        OR, // maybe rename this to PIPE
        OR_ASSIGNMENT,
        PACKAGE,
        PLUS,
        PLUS_ASSIGNMENT,
        PRIVATE,
        PROTECTED,
        PUBLIC,
        QUESTION,
        REFERENCE,
        RETURN,
        RIGHT_BRACE,
        RIGHT_BRACKET,
        RIGHT_PAREN,
        RIGHT_SHIFT,
        RIGHT_SHIFT_ASSIGNMENT,
        SEMICOLON,
        STATIC,
        STRICTFP,
        SWITCH,
        SYNCHRONIZED,
        THROW,
        THROWS,
        TRANSIENT,
        TRY,
        UNSIGNED_RIGHT_SHIFT,
        UNSIGNED_RIGHT_SHIFT_ASSIGNMENT,
        VOLATILE,
        WHILE,
        WHITESPACE,
        XOR,
        XOR_ASSIGNMENT
    }

}
