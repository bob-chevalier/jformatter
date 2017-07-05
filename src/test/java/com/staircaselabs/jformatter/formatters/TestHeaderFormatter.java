package com.staircaselabs.jformatter.formatters;

import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertTrue;
//import org.junit.jupiter.api.Test;
import org.junit.Test;

import com.staircaselabs.jformatter.formatters.HeaderFormatter;

public class TestHeaderFormatter {

//    public JavaFile( Path path ) {
//        originalText = readFileToString( path );
//
//        preambleTokens = tokenizeText( originalText, CLASS_START );
//        
//        for( Token token : preambleTokens ) {
//            if( token.kind == TokenKind.IDENTIFIER ) {
//                System.out.println( "pos: " + token.pos + "-" + token.endPos + ", " + token.name() );
//            }
//        }
//    }

//    protected List<Token> tokenizeText( String text, Set<TokenKind> stopTokens ) {
//        char[] chars = text.toCharArray();
//        ScannerFactory scannerFactory = ScannerFactory.instance( new Context() );
//        CommentTokenizer tokenizer = new CommentTokenizer( scannerFactory, chars, chars.length );
//        Scanner scanner = new PublicScanner( scannerFactory, tokenizer );
//        
//        List<Token> tokens = new ArrayList<>();
//        do {
//            scanner.nextToken();
//            if( stopTokens.contains( scanner.token().kind ) ) {
//                break;
//            }
//            tokens.add( scanner.token() );
//        } while( scanner.token().kind != TokenKind.EOF );
//
//        return tokens;
//    }

//    private String readFileToString( Path path ) {
//        try {
//            return new String( Files.readAllBytes( path ), UTF_8 );
//        } catch( IOException e ) {
//            throw new RuntimeException( path + " could not be read. "  + e.getMessage() );
//        }
//    }
    @Test
    public void shouldReadFileToString() {
        System.out.println( "BFC hello from shouldReadFileToString" );
//        JavaFile javaFile = new JavaFile( Paths.get( "src/test/resources/ImportOrder.java" ) );
        assertTrue( true );
    }

}
