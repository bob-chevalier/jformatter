package com.staircaselabs.jformatter.core;

import static com.staircaselabs.jformatter.core.CompilationUnitUtils.getCompilationUnit;
import static com.staircaselabs.jformatter.core.TokenUtils.tokenizeText;

import java.util.List;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

public class ScanningFormatter {

    protected FormatScanner scanner;

    public ScanningFormatter( FormatScanner scanner ) {
        this.scanner = scanner;
    }

    public String format( String text ) throws FormatException {
        try {
            boolean rescanRequired;
            do {
                List<TextToken> tokens = tokenizeText( text );
                JCCompilationUnit unit = getCompilationUnit( text );
                Input input = new Input( tokens, unit.endPositions );

                // scan text to find any remaining blocks that require replacement
                scanner.init();
                scanner.scan( unit, input );

                if( scanner.hasError() ) {
                    throw new FormatException( "Encountered syntax error" );
                }

                // initialize state variables
                StringBuilder sb = new StringBuilder( text );
                int minReplacementPos = Integer.MAX_VALUE;

                // attempt to apply all replacements in a single pass
                rescanRequired = false;
                for( Replacement replacement : scanner.getReplacements().descendingSet() ) {
                    // we must ensure that replacements don't overlap, if we detect an overlapping
                    // replacement, we skip it and re-scan text after applying other replacements
                    if( replacement.getEnd() < minReplacementPos ) {
                        replacement.apply( sb );
                        minReplacementPos = replacement.getStart();
                    } else {
                        rescanRequired = true;
                    }
                }

                text = sb.toString();
            } while( rescanRequired );

            return text;
        } catch( Throwable throwable ) {
            //TODO include stacktrace or diagnostic info?
            throwable.printStackTrace();
            throw new FormatException( throwable.getMessage() );
        }
    }

}
