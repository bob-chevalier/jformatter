package com.staircaselabs.jformatter.formatters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import com.staircaselabs.jformatter.core.CommentTokenizer;
import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.tools.javac.parser.JavaTokenizer;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;
import com.sun.tools.javac.parser.Tokens.Token;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.util.Context;

public final class Utils {

    public static final String DEFAULT_LINEBREAK = "\n";
    public static final Pattern LINEBREAK_PATTERN = Pattern.compile( "\\R" );

    public static final TokenType[] WHITESPACE_OR_NEWLINE = {
            TokenType.WHITESPACE,
            TokenType.NEWLINE
    };

    public static final TokenType[] COMMENTS_WHITESPACE_OR_NEWLINE = {
            TokenType.COMMENT_BLOCK,
            TokenType.COMMENT_JAVADOC,
            TokenType.COMMENT_LINE,
            TokenType.WHITESPACE,
            TokenType.NEWLINE
    };

    public static OptionalInt findIndexByType(
            List<TextToken> tokens,
            int startPos,
            TokenType... types
    ) {
        List<TokenType> included = Arrays.asList( types );
        return IntStream.range( startPos, tokens.size() )
                .filter( i -> included.contains( tokens.get( i ).type ) )
                .findFirst();
    }

    public static OptionalInt findIndexByTypeExclusion(
            List<TextToken> tokens,
            int startPos,
            TokenType... types
    ) {
        List<TokenType> excluded = Arrays.asList( types );
        return IntStream.range( startPos, tokens.size() )
                .filter( i -> !excluded.contains( tokens.get( i ).type) )
                .findFirst();
    }

    public static OptionalInt findLastIndexByTypeExclusion(
            List<TextToken> tokens,
            int startPos,
            int stopPos,
            TokenType... types
    ) {
        List<TokenType> excluded = Arrays.asList( types );
        return IntStream.range( startPos, stopPos )
                .filter( i -> !excluded.contains( tokens.get( i ).type ) )
                .reduce( (a, b) -> b );
    }

    public static String getLinebreak( List<TextToken> tokens ) {
        OptionalInt linebreakIdx = findIndexByType( tokens, 0, TokenType.NEWLINE );
        return linebreakIdx.isPresent()
                ? tokens.get( linebreakIdx.getAsInt() ).getText()
                : DEFAULT_LINEBREAK;
    }

    public static String tokensToText( List<TextToken> tokens ) {
        return tokens.stream().map( TextToken::getText ).reduce( "", String::concat );
    }

    public static boolean isComment( TextToken token ) {
        return token.type == TokenType.COMMENT_BLOCK 
                || token.type == TokenType.COMMENT_JAVADOC
                || token.type  == TokenType.COMMENT_LINE;
    }

    public static List<TextToken> tokenizeText( String text ) throws FormatException {
        char[] chars = text.toCharArray();
        ScannerFactory scannerFactory = ScannerFactory.instance( new Context() );
        CommentTokenizer tokenizer = new CommentTokenizer( scannerFactory, chars, chars.length );
        Scanner scanner = new PublicScanner( scannerFactory, tokenizer );

        int lastPos = 0;
        List<TextToken> tokens = new ArrayList<>();
        do {
            scanner.nextToken();
            Token rawToken = scanner.token();

            // check for comments
            if( rawToken.comments != null ) {
                // comments are stored in reverse order by tokenizer
                ListIterator<Comment> iter = rawToken.comments.listIterator( rawToken.comments.size() );
                while( iter.hasPrevious() ) {
                    Comment comment = iter.previous();

                    // check for whitespace before comment line
                    if( comment.getSourcePos( 0 ) > lastPos ) {
                        // add a whitespace and newline tokens
                        tokens.addAll(
                                parseWhitespace( text.substring( lastPos, comment.getSourcePos( 0 ) ), lastPos )
                        );
                    }

                    // add comment token
                    tokens.add(
                            new TextToken(
                                    comment.getText(),
                                    tokenTypeFromCommentStyle( comment.getStyle() ),
                                    comment.getSourcePos( 0 ),
                                    comment.getSourcePos( 0 ) + comment.getText().length()
                            )
                    );
                    lastPos = comment.getSourcePos( 0 ) + comment.getText().length();
                }
            }

            // check for whitespace before raw token
            if( rawToken.pos > lastPos ) {
                // add whitespace and newline tokens
                tokens.addAll( parseWhitespace( text.substring( lastPos, rawToken.pos ), lastPos ) );
            }

            tokens.add(
                    new TextToken(
                            text.substring( rawToken.pos, rawToken.endPos ),
                            tokenTypeFromTokenKind( rawToken.kind ),
                            rawToken.pos,
                            rawToken.pos + rawToken.endPos
                    )
            );
            lastPos = rawToken.endPos;
        } while( scanner.token().kind != TokenKind.EOF );

        // check for trailing newline after end-of-file token
        if( lastPos < text.length() ) {
            tokens.add(
                    new TextToken(
                            text.substring( lastPos, text.length() ),
                            TokenType.NEWLINE,
                            lastPos,
                            text.length()
                    )
            );
        }

        return tokens;
    }

    protected static List<TextToken> parseWhitespace( String text, int startPos ) {
        List<TextToken> tokens = new ArrayList<>();

        Matcher matcher = LINEBREAK_PATTERN.matcher( text );
        int offset = 0;
        while( matcher.find() ) {
            if( matcher.start() > offset ) {
                // add token to represent non-linebreak whitespace before first newline
                tokens.add(
                        new TextToken(
                                text.substring( offset, matcher.start() ),
                                TokenType.WHITESPACE,
                                startPos + offset,
                                startPos + (matcher.start() - offset)
                        )
                );
            }

            // add newline token
            tokens.add(
                    new TextToken(
                            matcher.group(),
                            TokenType.NEWLINE,
                            startPos + matcher.start(),
                            startPos + matcher.end()
                    )
            );

            offset = matcher.end();
        }

        // check for trailing non-linebreak whitespace
        if( offset < text.length() ) {
            tokens.add(
                    new TextToken(
                            text.substring( offset ),
                            TokenType.WHITESPACE,
                            startPos + offset,
                            startPos + text.length()
                    )
            );
        }

        return tokens;
    }

    protected static TokenType tokenTypeFromTokenKind( TokenKind kind ) {
        switch( kind ) {
        case IMPORT:
            return TokenType.IMPORT;
        case EOF:
            return TokenType.EOF;
        default:
            return TokenType.OTHER;
        }
    }

    protected static TokenType tokenTypeFromCommentStyle( CommentStyle style )
            throws FormatException {
        switch( style ) {
        case LINE:
            return TokenType.COMMENT_LINE;
        case BLOCK:
            return TokenType.COMMENT_BLOCK;
        case JAVADOC:
            return TokenType.COMMENT_JAVADOC;
        default:
            throw new FormatException( "Unexpected CommentStyle: " + style );
        }
    }

    private static class PublicScanner extends Scanner {
        protected PublicScanner( ScannerFactory scannerFactory, JavaTokenizer tokenizer ) {
            super( scannerFactory, tokenizer );
        }
    }

}
