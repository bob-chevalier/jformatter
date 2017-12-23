package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.core.TextToken.TokenType;

public class TextToken {

    public final TokenType type;
    public final int beginInclusive;
    public final int endExclusive;
    private final String text;

    public TextToken( String text, TokenType type, int beginInclusive, int endExclusive ) {
        this.text = text;
        this.type = type;
        this.beginInclusive = beginInclusive;
        this.endExclusive = endExclusive;
    }

    public String getText() {
        return text;
    }

    public TokenType getType() {
        return type;
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
