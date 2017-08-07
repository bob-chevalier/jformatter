package com.staircaselabs.jformatter.formatters;

import static com.staircaselabs.jformatter.core.TokenUtils.findNext;
import static com.staircaselabs.jformatter.core.TokenUtils.findNextByExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.findPrevByExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.getLinebreak;
import static com.staircaselabs.jformatter.core.TokenUtils.isComment;
import static com.staircaselabs.jformatter.core.TokenUtils.tokenizeText;
import static com.staircaselabs.jformatter.core.TokenUtils.stringifyTokens;

import java.util.List;
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.TreeSet;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;

public class ImportsSorter {

    private static final TokenType[] WS_OR_NEWLINE = {
            TokenType.WHITESPACE,
            TokenType.NEWLINE
    };

    private static final TokenType[] IMPORT_END = {
            TokenType.SEMICOLON, // guard against consecutive semicolons
            TokenType.WHITESPACE,
            TokenType.COMMENT_BLOCK,
            TokenType.COMMENT_JAVADOC,
            TokenType.COMMENT_LINE
    };

    private static final TokenType[] BLOCK_END = {
            TokenType.WHITESPACE,
            TokenType.SEMICOLON, // guard against consecutive semicolons
            TokenType.NEWLINE
    };

    public static String format( String text ) throws FormatException {
        List<TextToken> tokens = tokenizeText( text );
        SortedSet<Import> imports = new TreeSet<>();

        OptionalInt importStart = findNext( tokens, 0, TokenType.IMPORT );
        if( !importStart.isPresent() ) {
            // there are no imports so there's nothing to do
            return text;
        }

        // find start of text block to be replaced (include leading whitespace and newlines)
        int blockStart, afterImport;
        blockStart = afterImport = getImportsBlockStart( tokens, importStart.getAsInt() );

        while( importStart.isPresent() ) {
            int semicolonPos = getNextSemicolon( tokens, importStart );
            OptionalInt staticPos =
                    findNext( tokens, importStart.getAsInt(), semicolonPos, TokenType.STATIC );
            int beforeNamePos = staticPos.orElse( importStart.getAsInt() ) + 1;

            int nameStart = getImportNameStart( tokens, beforeNamePos, semicolonPos );
            String name = stringifyTokens( tokens, nameStart, semicolonPos );

            // find first token past import's trailing characters (likely a newline)
            afterImport = findNextByExclusion( tokens, semicolonPos + 1, IMPORT_END )
                    .orElseThrow( () -> new FormatException(
                            "Unexpected text: ["
                            + stringifyTokens( tokens, semicolonPos + 1 )
                            + "]" )
                    );

            // filter everything except whitespace and comments out of trailing characters
            String trailingChars = getTrailingChars( tokens, (semicolonPos + 1), afterImport );

            imports.add( new Import( name, trailingChars, staticPos.isPresent() ) );

            // find the start of the next import
            importStart = findNext( tokens, semicolonPos, TokenType.IMPORT );
        }

        // find end of text block to be replaced (consume trailing whitespace and newlines)
        int blockEnd = getImportsBlockEnd( tokens, afterImport );

        // determine format of linebreaks in file
        String newline = getLinebreak( tokens );

        // re-build file
        StringBuilder sb = new StringBuilder();
        if( blockStart > 0 ) {
            // add text before imports
            sb.append( stringifyTokens( tokens, 0, blockStart ) );
            sb.append( newline );
            sb.append( newline );
        }

        // add new imports block
        sb.append( insertNewlines( imports, newline ) );

        // add text after imports
//        sb.append( newline );
        sb.append( stringifyTokens( tokens, blockEnd ) );

        return sb.toString();
    }

    private static int getImportsBlockStart( List<TextToken> tokens, int firstImportPos ) {
        // consume leading whitespace and newlines
        return findPrevByExclusion( tokens, 0, firstImportPos, WS_OR_NEWLINE )
                .orElse( -1 ) + 1;
    }

    private static int getImportsBlockEnd( List<TextToken> tokens, int afterLastImport )
            throws FormatException {
        // find next non-whitespace, non-newline token
        return findNextByExclusion( tokens, afterLastImport, BLOCK_END )
                .orElseThrow( () -> new FormatException(
                        "Couldn't find anything after imports: ["
                        + stringifyTokens( tokens, afterLastImport )
                        + "]" )
                );
    }

    private static int getNextSemicolon( List<TextToken> tokens, OptionalInt importPos )
            throws FormatException {
        return findNext( tokens, importPos.getAsInt() + 1, TokenType.SEMICOLON )
                .orElseThrow( () -> new FormatException(
                        "Missing closing semicolon: ["
                        + stringifyTokens( tokens, importPos.getAsInt() + 1 )
                        + "]" )
                );
    }

    private static int getImportNameStart( List<TextToken> tokens, int fromPos, int toPos )
            throws FormatException {
        return findNextByExclusion( tokens, fromPos, toPos, TokenType.WHITESPACE )
                .orElseThrow( () -> new FormatException(
                        "No import name in: ["
                        + stringifyTokens( tokens, fromPos, toPos )
                        + "]" )
                );
    }

    private static String getTrailingChars( List<TextToken> tokens, int fromPos, int toPos )
            throws FormatException {
        StringBuilder sb = new StringBuilder();
        for( int idx = fromPos; idx < toPos; idx++ ) {
            TextToken token = tokens.get( idx );
            if( token.type == TokenType.WHITESPACE || isComment( token ) ) {
                sb.append( token.getText() );
            }
        }
        return sb.toString();
    }

    private static String insertNewlines( SortedSet<Import> imports, String newline ) {
        StringBuilder sb = new StringBuilder();
        boolean isPreviousStatic = false;
        for( Import importLine : imports ) {
            if( isPreviousStatic && !importLine.isStatic ) {
                sb.append( newline );
            }
            sb.append( importLine );
            sb.append( newline );
            isPreviousStatic = importLine.isStatic;
        }
        sb.append( newline );
        return sb.toString();
    }

    private static class Import implements Comparable<Import> {
        public final String name;
        public final String trailingChars;
        public final boolean isStatic;

        public Import( String name, String trailingChars, boolean isStatic ) {
          this.name = name;
          this.isStatic = isStatic;

          // remove trailing whitespace characters
          this.trailingChars = trailingChars.replaceFirst( "\\s++$", "" );
        }

        @Override
        public int compareTo( Import other ) {
            if( isStatic != other.isStatic) {
                return isStatic ? -1 : 1;
            }
            return name.compareTo( other.name );
        }

        @Override
        public String toString() {
          return "import " + (isStatic ? "static " : "") + name + ";" + trailingChars;
        }
    }

}
