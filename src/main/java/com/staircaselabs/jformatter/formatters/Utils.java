package com.staircaselabs.jformatter.formatters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalInt;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import com.sun.tools.javac.parser.JavaTokenizer;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;
import com.sun.tools.javac.parser.Tokens.Token;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.util.Context;

import com.staircaselabs.jformatter.core.CommentTokenizer;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;

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

    public static final Set<TokenKind> CLASS_START = new HashSet<>(
            Arrays.asList(
                    TokenKind.CLASS,
                    TokenKind.INTERFACE,
                    TokenKind.ENUM
            )
    );

    public static OptionalInt findIndexByType(
            List<TextToken> tokens,
            int startPos,
            TokenType... types
    ) {
        List<TokenType> included = Arrays.asList( types );
        return IntStream.range( startPos, tokens.size() )
                .filter( i -> included.contains( tokens.get( i ).getType() ) )
                .findFirst();
    }

    public static OptionalInt findIndexByTypeExclusion(
            List<TextToken> tokens,
            int startPos,
            TokenType... types
    ) {
        List<TokenType> excluded = Arrays.asList( types );
        return IntStream.range( startPos, tokens.size() )
                .filter( i -> !excluded.contains( tokens.get( i ).getType() ) )
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
                .filter( i -> !excluded.contains( tokens.get( i ).getType() ) )
                .reduce( (a, b) -> b );
    }

    public static String tokensToText( List<TextToken> tokens ) {
        return tokens.stream().map( TextToken::getText ).reduce( "", String::concat );
    }

    public static boolean isComment( TextToken token ) {
        return token.getType() == TokenType.COMMENT_BLOCK 
                || token.getType() == TokenType.COMMENT_JAVADOC
                || token.getType() == TokenType.COMMENT_LINE;
    }

    public static List<TextToken> tokenizeText( String text, Set<TokenKind> stopTokens ) {
        char[] chars = text.toCharArray();
        ScannerFactory scannerFactory = ScannerFactory.instance( new Context() );
        CommentTokenizer tokenizer = new CommentTokenizer( scannerFactory, chars, chars.length );
        Scanner scanner = new PublicScanner( scannerFactory, tokenizer );

        int lastPos = 0;
        List<TextToken> tokens = new ArrayList<>();
        do {
            scanner.nextToken();
            Token rawToken = scanner.token();
            if( stopTokens.contains( rawToken.kind ) ) {
                break;
            }

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
        default:
            return TokenType.OTHER;
        }
    }

    protected static TokenType tokenTypeFromCommentStyle( CommentStyle style ) {
        switch( style ) {
        case LINE:
            return TokenType.COMMENT_LINE;
        case BLOCK:
            return TokenType.COMMENT_BLOCK;
        case JAVADOC:
            return TokenType.COMMENT_JAVADOC;
        default:
            //TODO throw an exception here?
            return null;
        }
    }

//    public static Optional<TextToken> findNextTokenByType(
//            List<TextToken> tokens,
//            int startPos,
//            final Set<TokenType> types
//    ) {
//        for( int idx = startPos; idx < tokens.size(); idx++ ) {
//            TextToken token = tokens.get( idx );
//            if( types.contains( token.getType() ) ) {
//                return Optional.of( token );
//            }
//        }
//        return Optional.empty();
//    }


//    protected Optional<TextToken> findPrevTokenByType(
//            List<TextToken> tokens,
//            int startPos,
//            final Set<TokenType> types
//    ) {
//        for( int idx = startPos; idx < tokens.size(); idx++ ) {
//            TextToken token = tokens.get( idx );
//            if( types.contains( token.getType() ) ) {
//                return Optional.of( token );
//            }
//        }
//        return Optional.empty();
//    }

//    private String readFileToString( Path path ) {
//        try {
//            return new String( Files.readAllBytes( path ), UTF_8 );
//        } catch( IOException e ) {
//            throw new RuntimeException( path + " could not be read. "  + e.getMessage() );
//        }
//    }

    private static class PublicScanner extends Scanner {
        protected PublicScanner( ScannerFactory scannerFactory, JavaTokenizer tokenizer ) {
            super( scannerFactory, tokenizer );
        }
    }

}
