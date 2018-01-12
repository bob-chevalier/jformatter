package com.staircaselabs.jformatter.core;

import java.text.DecimalFormat;
import java.util.Optional;

public class TextToken {

    private final DecimalFormat decimalFormat = new DecimalFormat( "+#;-#" );
    public final int beginInclusive;
    public final int endExclusive;
    private final String text;
    private final TokenType type;

    private int indentOffset = 0;
    private Optional<LineWrapTag> lineWrapTag = Optional.empty();

    public TextToken( String text, TokenType type, int beginInclusive, int endExclusive ) {
        this.text = text;
        this.type = type;
        this.beginInclusive = beginInclusive;
        this.endExclusive = endExclusive;
    }

    public TokenType getType() {
        return type;
    }

    public void allowLineWrap( LineWrapTag lineWrapTag ) {
        this.lineWrapTag = Optional.of( lineWrapTag );
    }

    public Optional<LineWrapTag> getLineWrapTag() {
        return lineWrapTag;
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
        String lineWrapLabel = lineWrapTag.map( LineWrapTag::toString ).orElse( "" );
        String markupLabel = (indentOffset == 0 && !lineWrapTag.isPresent())
                ? ""
                : String.format( "[%s%s]", indentLabel,  lineWrapLabel );

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
