package com.staircaselabs.jformatter.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.parser.JavaTokenizer;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;
import com.sun.tools.javac.parser.Tokens.Token;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.util.Context;
import com.staircaselabs.jformatter.core.TextToken.TokenType;

public final class TokenUtils {

    private static final String DEFAULT_LINEBREAK = "\n";
    private static final Pattern LINEBREAK_PATTERN = Pattern.compile( "\\R" );

    public static boolean containsComments( List<TextToken> tokens, int startInclusive, int endExclusive ) {
        if( startInclusive >= endExclusive ) {
            return false;
        }

        return tokens.subList( startInclusive, endExclusive )
                .stream()
                .anyMatch( TokenUtils::isComment );
    }

    public static boolean contains( List<TextToken> tokens, int startInclusive, int endExclusive, TokenType... types ) {
        if( startInclusive >= endExclusive ) {
            return false;
        }

        List<TokenType> typesList = Arrays.asList( types );
        return tokens.subList( startInclusive, endExclusive )
                .stream()
                .map( TextToken::getType )
                .anyMatch( typesList::contains );
    }

    public static OptionalInt findNext(
            List<TextToken> tokens,
            int startInclusive,
            int endExclusive,
            TokenType... types
    ) {
        if( startInclusive >= endExclusive ) {
            return OptionalInt.empty();
        }

        List<TokenType> included = Arrays.asList( types );
        return IntStream.range( startInclusive, endExclusive )
                .filter( i -> included.contains( tokens.get( i ).getType() ) )
                .findFirst();
    }

    public static OptionalInt findNext(
            List<TextToken> tokens,
            int startInclusive,
            TokenType... types
    ) {
        return findNext( tokens, startInclusive, tokens.size(), types );
    }

    public static OptionalInt findNextByExclusion(
            List<TextToken> tokens,
            int startInclusive,
            int endExclusive,
            TokenType... types
    ) {
        if( startInclusive >= endExclusive ) {
            return OptionalInt.empty();
        }

        List<TokenType> excluded = Arrays.asList( types );
        return IntStream.range( startInclusive, endExclusive )
                .filter( i -> !excluded.contains( tokens.get( i ).getType() ) )
                .findFirst();
    }

    public static OptionalInt findNextByExclusion(
            List<TextToken> tokens,
            int startInclusive,
            TokenType... types
    ) {
        return findNextByExclusion( tokens, startInclusive, tokens.size(), types );
    }

    public static OptionalInt findPrev(
            List<TextToken> tokens,
            int startInclusive,
            int endExclusive,
            TokenType... types
    ) {
        if( startInclusive >= endExclusive ) {
            return OptionalInt.empty();
        }

        List<TokenType> included = Arrays.asList( types );
        return IntStream.range( startInclusive, endExclusive )
                .filter( i -> included.contains( tokens.get( i ).getType() ) )
                .reduce( (a, b) -> b );
    }

    public static OptionalInt findPrev(
            List<TextToken> tokens,
            int endExclusive,
            TokenType... types
    ) {
        return findPrev( tokens, 0, endExclusive, types );
    }

    public static OptionalInt findPrevByExclusion(
            List<TextToken> tokens,
            int startInclusive,
            int endExclusive,
            TokenType... types
    ) {
        if( startInclusive >= endExclusive ) {
            return OptionalInt.empty();
        }

        List<TokenType> excluded = Arrays.asList( types );
        return IntStream.range( startInclusive, endExclusive )
                .filter( i -> !excluded.contains( tokens.get( i ).getType() ) )
                .reduce( (a, b) -> b );
    }

    public static OptionalInt findPrevByExclusion(
            List<TextToken> tokens,
            int endExclusive,
            TokenType... types
    ) {
        return findPrevByExclusion( tokens, 0, endExclusive, types );
    }

    public static String getLinebreak( List<TextToken> tokens ) {
        OptionalInt linebreakIdx = findNext( tokens, 0, TokenType.NEWLINE );
        return linebreakIdx.isPresent()
                ? tokens.get( linebreakIdx.getAsInt() ).toString()
                : DEFAULT_LINEBREAK;
    }

    public static boolean isComment( TextToken token ) {
        return token.getType() == TokenType.COMMENT_BLOCK
                || token.getType() == TokenType.COMMENT_JAVADOC
                || token.getType()  == TokenType.COMMENT_LINE;
    }

    public static boolean isSingleWhitespace( List<TextToken> tokens, int startInclusive, int endExclusive ) {
        if( endExclusive - startInclusive != 1 ) {
            return false;
        } else {
            TextToken token = tokens.get( startInclusive );
            return (token.getType() == TokenType.WHITESPACE
                    && (token.endExclusive - token.beginInclusive) == 1);
        }
    }

    public static String stringifyTokens( List<TextToken> tokens ) {
        return stringifyTokens( tokens, 0, tokens.size() );
    }

    public static String stringifyTokens( List<TextToken> tokens, int startInclusive ) {
        return stringifyTokens( tokens, startInclusive, tokens.size() );
    }

    public static String stringifyTokens( List<TextToken> tokens, int startInclusive, int endExclusive ) {
        return IntStream.range( startInclusive, endExclusive )
                .mapToObj( tokens::get )
                .map( TextToken::toString )
                .collect( Collectors.joining() );
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
                            rawToken.endPos
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
        case ABSTRACT:
            return TokenType.ABSTRACT;
        case AMP:
            return TokenType.AND;
        case AMPAMP:
            return TokenType.CONDITIONAL_AND;
        case AMPEQ:
            return TokenType.AND_ASSIGNMENT;
        case ARROW:
            return TokenType.ARROW;
        case BANGEQ:
            return TokenType.NOT_EQUAL;
        case BAR:
            return TokenType.OR;
        case BARBAR:
            return TokenType.CONDITIONAL_OR;
        case BAREQ:
            return TokenType.OR_ASSIGNMENT;
        case BREAK:
            return TokenType.BREAK;
        case CARET:
            return TokenType.XOR;
        case CARETEQ:
            return TokenType.XOR_ASSIGNMENT;
        case CATCH:
            return TokenType.CATCH;
        case CASE:
            return TokenType.CASE;
        case CLASS:
            return TokenType.CLASS;
        case COLCOL:
            return TokenType.REFERENCE;
        case COLON:
            return TokenType.COLON;
        case COMMA:
            return TokenType.COMMA;
        case CONTINUE:
            return TokenType.CONTINUE;
        case DEFAULT:
            return TokenType.DEFAULT;
        case DO:
            return TokenType.DO;
        case DOT:
            return TokenType.DOT;
        case ELSE:
            return TokenType.ELSE;
        case EOF:
            return TokenType.EOF;
        case EQ:
            return TokenType.ASSIGNMENT;
        case EQEQ:
            return TokenType.EQUALS;
        case EXTENDS:
            return TokenType.EXTENDS;
        case FINAL:
            return TokenType.FINAL;
        case FINALLY:
            return TokenType.FINALLY;
        case FOR:
            return TokenType.FOR;
        case GT:
            return TokenType.GREATER_THAN;
        case GTEQ:
            return TokenType.GREATER_THAN_EQUAL;
        case GTGT:
            return TokenType.RIGHT_SHIFT;
        case GTGTEQ:
            return TokenType.RIGHT_SHIFT_ASSIGNMENT;
        case GTGTGT:
            return TokenType.UNSIGNED_RIGHT_SHIFT;
        case GTGTGTEQ:
            return TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT;
        case IF:
            return TokenType.IF;
        case IMPLEMENTS:
            return TokenType.IMPLEMENTS;
        case IMPORT:
            return TokenType.IMPORT;
        case INSTANCEOF:
            return TokenType.INSTANCE_OF;
        case LBRACE:
            return TokenType.LEFT_BRACE;
        case LBRACKET:
            return TokenType.LEFT_BRACKET;
        case LT:
            return TokenType.LESS_THAN;
        case LTEQ:
            return TokenType.LESS_THAN_EQUAL;
        case LPAREN:
            return TokenType.LEFT_PAREN;
        case LTLT:
            return TokenType.LEFT_SHIFT;
        case LTLTEQ:
            return TokenType.LEFT_SHIFT_ASSIGNMENT;
        case MONKEYS_AT:
            return TokenType.AT;
        case NEW:
            return TokenType.NEW;
        case PACKAGE:
            return TokenType.PACKAGE;
        case PERCENT:
            return TokenType.MOD;
        case PERCENTEQ:
            return TokenType.MOD_ASSIGNMENT;
        case PLUS:
            return TokenType.PLUS;
        case PLUSEQ:
            return TokenType.PLUS_ASSIGNMENT;
        case PRIVATE:
            return TokenType.PRIVATE;
        case PROTECTED:
            return TokenType.PROTECTED;
        case PUBLIC:
            return TokenType.PUBLIC;
        case QUES:
            return TokenType.QUESTION;
        case RBRACE:
            return TokenType.RIGHT_BRACE;
        case RBRACKET:
            return TokenType.RIGHT_BRACKET;
        case RETURN:
            return TokenType.RETURN;
        case RPAREN:
            return TokenType.RIGHT_PAREN;
        case SEMI:
            return TokenType.SEMICOLON;
        case SLASH:
            return TokenType.DIVIDE;
        case SLASHEQ:
            return TokenType.DIVIDE_ASSIGNMENT;
        case STAR:
            return TokenType.MULTIPLY;
        case STAREQ:
            return TokenType.MULTIPLY_ASSIGNMENT;
        case STATIC:
            return TokenType.STATIC;
        case STRICTFP:
            return TokenType.STRICTFP;
        case SUB:
            return TokenType.MINUS;
        case SUBEQ:
            return TokenType.MINUS_ASSIGNMENT;
        case SWITCH:
            return TokenType.SWITCH;
        case SYNCHRONIZED:
            return TokenType.SYNCHRONIZED;
        case THROW:
            return TokenType.THROW;
        case THROWS:
            return TokenType.THROWS;
        case TRANSIENT:
            return TokenType.TRANSIENT;
        case TRY:
            return TokenType.TRY;
        case VOLATILE:
            return TokenType.VOLATILE;
        case WHILE:
            return TokenType.WHILE;
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

    public static TokenType tokenTypeFromBinaryOperator( Kind kind ) {
        switch( kind ) {
        case AND:
            return TokenType.AND;
        case CONDITIONAL_AND:
            return TokenType.CONDITIONAL_AND;
        case CONDITIONAL_OR:
            return TokenType.CONDITIONAL_OR;
        case DIVIDE:
            return TokenType.DIVIDE;
        case EQUAL_TO:
            return TokenType.EQUALS;
        case GREATER_THAN:
            return TokenType.GREATER_THAN;
        case GREATER_THAN_EQUAL:
            return TokenType.GREATER_THAN_EQUAL;
        case LESS_THAN:
            return TokenType.LESS_THAN;
        case LESS_THAN_EQUAL:
            return TokenType.LESS_THAN_EQUAL;
        case LEFT_SHIFT:
            return TokenType.LEFT_SHIFT;
        case MINUS:
            return TokenType.MINUS;
        case MULTIPLY:
            return TokenType.MULTIPLY;
        case NOT_EQUAL_TO:
            return TokenType.NOT_EQUAL;
        case OR:
            return TokenType.OR;
        case PLUS:
            return TokenType.PLUS;
        case REMAINDER:
            return TokenType.MOD;
        case RIGHT_SHIFT:
            return TokenType.RIGHT_SHIFT;
        case UNSIGNED_RIGHT_SHIFT:
            return TokenType.UNSIGNED_RIGHT_SHIFT;
        case XOR:
            return TokenType.XOR;
        default:
            return TokenType.OTHER;
        }
    }

    public static TokenType tokenTypeFromCompoundOperator( Kind kind ) {
        switch( kind ) {
        case AND_ASSIGNMENT:
            return TokenType.AND_ASSIGNMENT;
        case DIVIDE_ASSIGNMENT:
            return TokenType.DIVIDE_ASSIGNMENT;
        case LEFT_SHIFT_ASSIGNMENT:
            return TokenType.LEFT_SHIFT_ASSIGNMENT;
        case MINUS_ASSIGNMENT:
            return TokenType.MINUS_ASSIGNMENT;
        case MULTIPLY_ASSIGNMENT:
            return TokenType.MULTIPLY_ASSIGNMENT;
        case OR_ASSIGNMENT:
            return TokenType.OR_ASSIGNMENT;
        case PLUS_ASSIGNMENT:
            return TokenType.PLUS_ASSIGNMENT;
        case REMAINDER_ASSIGNMENT:
            return TokenType.MOD_ASSIGNMENT;
        case RIGHT_SHIFT_ASSIGNMENT:
            return TokenType.RIGHT_SHIFT_ASSIGNMENT;
        case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
            return TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT;
        case XOR_ASSIGNMENT:
            return TokenType.XOR_ASSIGNMENT;
        default:
            return TokenType.OTHER;
        }
    }

    public static TokenType tokenTypeFromModifier( String modifier ) {
        switch( modifier ) {
        case "abstract":
            return TokenType.ABSTRACT;
        case "final":
            return TokenType.FINAL;
        case "private":
            return TokenType.PRIVATE;
        case "protected":
            return TokenType.PROTECTED;
        case "public":
            return TokenType.PUBLIC;
        case "static":
            return TokenType.STATIC;
        case "strictfp":
            return TokenType.STRICTFP;
        case "synchronized":
            return TokenType.SYNCHRONIZED;
        case "transient":
            return TokenType.TRANSIENT;
        case "volatile":
            return TokenType.VOLATILE;
        default:
            return TokenType.OTHER;
        }
    }

    private static class PublicScanner extends Scanner {
        protected PublicScanner( ScannerFactory scannerFactory, JavaTokenizer tokenizer ) {
            super( scannerFactory, tokenizer );
        }
    }

}
