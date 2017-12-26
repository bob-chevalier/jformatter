package com.staircaselabs.jformatter.core;

import static com.staircaselabs.jformatter.core.CompilationUnitUtils.getCompilationUnit;
import static com.staircaselabs.jformatter.core.TokenUtils.tokenizeText;

import java.util.List;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

public class ScanningFormatter {

    private static final boolean DEBUGGING = true;

    protected ReplacementScanner scanner;
    private ReplacementScanner debugScanner = new ReplacementScanner();

    public ScanningFormatter( ReplacementScanner scanner ) {
        this.scanner = scanner;
    }

    public String format( String text ) throws FormatException {
//        try {
            boolean rescanRequired;
            do {
                List<TextToken> tokens = tokenizeText( text );
                JCCompilationUnit unit = getCompilationUnit( text );
                Input input = new Input( tokens, unit.endPositions );

                // scan text to find any remaining blocks that require replacement
                scanner.init();
                scanner.scan( unit, input );

                if( scanner.hasError() ) {
                    throw new FormatException( getClass().getSimpleName() + " encountered a syntax error" );
                }

                // initialize state variables
                StringBuilder sb = new StringBuilder( text );
                int minReplacementPos = Integer.MAX_VALUE;

                // attempt to apply all replacements in a single pass
                rescanRequired = scanner.isRescanRequired();
                for( Replacement replacement : scanner.getReplacements().descendingSet() ) {
                    // we must ensure that replacements don't overlap, if we detect an overlapping
                    // replacement, we skip it and re-scan text after applying other replacements
                    if( replacement.getStop() < minReplacementPos ) {
                        replacement.apply( sb );
                        minReplacementPos = replacement.getStart();

                        if( DEBUGGING ) {
                            text = sb.toString();
                            tokens = tokenizeText( text );
                            unit = getCompilationUnit( text );
                            input = new Input( tokens, unit.endPositions );

                            // scan text to find any remaining blocks that require replacement
                            debugScanner.init();
                            debugScanner.scan( unit, input );

                            if( debugScanner.hasError() ) {
                                throw new FormatException(
                                        "Syntax error caused by " + replacement.getDebugLabel()
                                        + "\n=========Before=========\n" + replacement.getOldText()
                                        + "\n=========After==========\n" + replacement.getNewText()
                                        + "\n========================"
                                );
                            }
                            sb = new StringBuilder( text );
                        }
                    } else {
                        rescanRequired = true;
                    }
                }

                text = sb.toString();
            } while( rescanRequired );

            return text;
//        } catch( Throwable throwable ) {
//            //TODO include stacktrace or diagnostic info?
//            throwable.printStackTrace();
//            throw new FormatException( throwable.getMessage() );
//        }
    }

}
