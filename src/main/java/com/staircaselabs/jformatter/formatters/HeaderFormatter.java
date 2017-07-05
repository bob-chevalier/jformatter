package com.staircaselabs.jformatter.formatters;

import java.util.List;
import java.util.OptionalInt;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import static com.staircaselabs.jformatter.formatters.Utils.CLASS_START;
import static com.staircaselabs.jformatter.formatters.Utils.COMMENTS_WHITESPACE_OR_NEWLINE;
import static com.staircaselabs.jformatter.formatters.Utils.DEFAULT_LINEBREAK;
import static com.staircaselabs.jformatter.formatters.Utils.WHITESPACE_OR_NEWLINE;
import static com.staircaselabs.jformatter.formatters.Utils.findIndexByType;
import static com.staircaselabs.jformatter.formatters.Utils.findIndexByTypeExclusion;
import static com.staircaselabs.jformatter.formatters.Utils.findLastIndexByTypeExclusion;
import static com.staircaselabs.jformatter.formatters.Utils.tokensToText;
import static com.staircaselabs.jformatter.formatters.Utils.isComment;
import static com.staircaselabs.jformatter.formatters.Utils.tokenizeText;

public class HeaderFormatter {

    public static String format( String text ) throws FormatException {
        List<TextToken> preambleTokens = tokenizeText( text, CLASS_START );

        // find index of the first token in header, excluding leading whitespace and newlines
        int startIdx = findIndexByTypeExclusion( preambleTokens, 0, WHITESPACE_OR_NEWLINE )
                .orElseThrow( () -> new FormatException( "File does not contain any class declarations" ) );

        // verify that a header exists in this file
        if( !isComment( preambleTokens.get( startIdx ) ) ) {
            //TODO return text from start token to end or potentially insert pre-defined header + newline
        }

        // find index of first token representing actual code
        int codeIdx = findIndexByTypeExclusion( preambleTokens, startIdx, COMMENTS_WHITESPACE_OR_NEWLINE )
                .orElseThrow( () -> new FormatException( "File does not contain any class declarations" ) );

        // find index of last token in header, excluding trailing whitespace and newlines
        int stopIdx = findLastIndexByTypeExclusion( preambleTokens, startIdx, codeIdx - 1, WHITESPACE_OR_NEWLINE )
                .orElseThrow( () -> new FormatException( "File does not contain any class declarations" ) );

        // determine the linebreak format of this file
        OptionalInt linebreakIdx = findIndexByType( preambleTokens, 0, TokenType.NEWLINE );
        String linebreak = linebreakIdx.isPresent()
                ? preambleTokens.get( linebreakIdx.getAsInt() ).getText()
                : DEFAULT_LINEBREAK;

        // rebuild preamble, ensuring that first line of code is preceded by a newline
        String newPreamble = tokensToText( preambleTokens.subList( startIdx, stopIdx + 1 ) )
                + linebreak
                + tokensToText( preambleTokens.subList( codeIdx, preambleTokens.size() ) );

        System.out.println( "BFC start: " + startIdx + ", stop: " + stopIdx + ", codeIdx: " + codeIdx );
        System.out.println( "BFC new preamble:" );
        System.out.println( newPreamble );
        //TODO need to return entire workingText
        return newPreamble;
    }

//    protected boolean isComment( TextToken token ) {
//        return token.getType() == TokenType.COMMENT_BLOCK 
//                || token.getType() == TokenType.COMMENT_JAVADOC
//                || token.getType() == TokenType.COMMENT_LINE;
//    }
//
//    protected List<TextToken> tokenizeText( String text, Set<TokenKind> stopTokens ) {
//        char[] chars = text.toCharArray();
//        ScannerFactory scannerFactory = ScannerFactory.instance( new Context() );
//        CommentTokenizer tokenizer = new CommentTokenizer( scannerFactory, chars, chars.length );
//        Scanner scanner = new PublicScanner( scannerFactory, tokenizer );
//
//        int lastPos = 0;
//        List<TextToken> tokens = new ArrayList<>();
//        do {
//            scanner.nextToken();
//            Token rawToken = scanner.token();
//            //TODO do we really need to support stop tokens?
//            if( stopTokens.contains( rawToken.kind ) ) {
//                break;
//            }
//
//            // check for comments
//            if( rawToken.comments != null ) {
//                // comments are stored in reverse order by tokenizer
//                ListIterator<Comment> iter = rawToken.comments.listIterator( rawToken.comments.size() );
//                while( iter.hasPrevious() ) {
//                    Comment comment = iter.previous();
//
//                    // check for whitespace before comment line
//                    if( comment.getSourcePos( 0 ) > lastPos ) {
//                        // add a whitespace and newline tokens
//                        tokens.addAll(
//                                parseWhitespace( text.substring( lastPos, comment.getSourcePos( 0 ) ), lastPos )
//                        );
//                    }
//
//                    // add comment token
//                    tokens.add(
//                            new TextToken(
//                                    comment.getText(),
//                                    tokenTypeFromCommentStyle( comment.getStyle() ),
//                                    comment.getSourcePos( 0 ),
//                                    comment.getSourcePos( 0 ) + comment.getText().length()
//                            )
//                    );
//                    lastPos = comment.getSourcePos( 0 ) + comment.getText().length();
//                }
//            }
//
//            // check for whitespace before raw token
//            if( rawToken.pos > lastPos ) {
//                // add whitespace and newline tokens
//                tokens.addAll( parseWhitespace( text.substring( lastPos, rawToken.pos ), lastPos ) );
//            }
//
//            tokens.add(
//                    new TextToken(
//                            text.substring( rawToken.pos, rawToken.endPos ),
//                            tokenTypeFromTokenKind( rawToken.kind ),
//                            rawToken.pos,
//                            rawToken.pos + rawToken.endPos
//                    )
//            );
//            lastPos = rawToken.endPos;
//
//        } while( scanner.token().kind != TokenKind.EOF );
//
//        return tokens;
//    }
//
//    protected List<TextToken> parseWhitespace( String text, int startPos ) {
//        List<TextToken> tokens = new ArrayList<>();
//
//        Matcher matcher = LINEBREAK_PATTERN.matcher( text );
//        int offset = 0;
//        while( matcher.find() ) {
//            if( matcher.start() > offset ) {
//                // add token to represent non-linebreak whitespace before first newline
//                tokens.add(
//                        new TextToken(
//                                text.substring( offset, matcher.start() ),
//                                TokenType.WHITESPACE,
//                                startPos + offset,
//                                startPos + (matcher.start() - offset)
//                        )
//                );
//            }
//
//            // add newline token
//            tokens.add(
//                    new TextToken(
//                            matcher.group(),
//                            TokenType.NEWLINE,
//                            startPos + matcher.start(),
//                            startPos + matcher.end()
//                    )
//            );
//
//            offset = matcher.end();
//        }
//
//        // check for trailing non-linebreak whitespace
//        if( offset < text.length() ) {
//            tokens.add(
//                    new TextToken(
//                            text.substring( offset ),
//                            TokenType.WHITESPACE,
//                            startPos + offset,
//                            startPos + text.length()
//                    )
//            );
//        }
//
//        return tokens;
//    }
//
//    protected TokenType tokenTypeFromTokenKind( TokenKind kind ) {
//        switch( kind ) {
//        case IMPORT:
//            return TokenType.IMPORT;
//        default:
//            return TokenType.OTHER;
//        }
//    }
//
//    protected TokenType tokenTypeFromCommentStyle( CommentStyle style ) {
//        switch( style ) {
//        case LINE:
//            return TokenType.COMMENT_LINE;
//        case BLOCK:
//            return TokenType.COMMENT_BLOCK;
//        case JAVADOC:
//            return TokenType.COMMENT_JAVADOC;
//        default:
//            //TODO throw an exception here?
//            return null;
//        }
//    }
//
//    protected Optional<TextToken> findNextTokenByType(
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
//
//    protected OptionalInt findIndexByType(
//            List<TextToken> tokens,
//            int startPos,
//            TokenType... types
//    ) {
//        List<TokenType> included = Arrays.asList( types );
//        return IntStream.range( startPos, tokens.size() )
//                .filter( i -> included.contains( tokens.get( i ).getType() ) )
//                .findFirst();
//    }
//
//    protected OptionalInt findIndexByTypeExclusion(
//            List<TextToken> tokens,
//            int startPos,
//            TokenType... types
//    ) {
//        List<TokenType> excluded = Arrays.asList( types );
//        return IntStream.range( startPos, tokens.size() )
//                .filter( i -> !excluded.contains( tokens.get( i ).getType() ) )
//                .findFirst();
//    }
//
//    protected OptionalInt findLastIndexByTypeExclusion(
//            List<TextToken> tokens,
//            int startPos,
//            int stopPos,
//            TokenType... types
//    ) {
//        List<TokenType> excluded = Arrays.asList( types );
//        return IntStream.range( startPos, stopPos )
//                .filter( i -> !excluded.contains( tokens.get( i ).getType() ) )
//                .reduce( (a, b) -> b );
//    }
//
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
//
//    private String getText( List<TextToken> tokens ) {
//        return tokens.stream().map( TextToken::getText ).reduce( "", String::concat );
//    }
//
//    private String readFileToString( Path path ) {
//        try {
//            return new String( Files.readAllBytes( path ), UTF_8 );
//        } catch( IOException e ) {
//            throw new RuntimeException( path + " could not be read. "  + e.getMessage() );
//        }
//    }
//
//    private static class PublicScanner extends Scanner {
//        protected PublicScanner( ScannerFactory scannerFactory, JavaTokenizer tokenizer ) {
//            super( scannerFactory, tokenizer );
//        }
//    }

}
