package com.staircaselabs.jformatter.core;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Checked exception class for formatter errors
 */
public final class FormatException extends Exception {

    public FormatException( String msg ) {
        this( DiagnosticInfo.create( msg ) );
    }

    public FormatException( DiagnosticInfo... diagnosticInfos ) {
        super( "\n  " + Arrays.stream( diagnosticInfos )
                .map( i -> i.toString() )
                .collect( Collectors.joining( "\n  " ) )
        );
    }

}
