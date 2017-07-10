package com.staircaselabs.jformatter.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Checked exception class for formatter errors
 */
public final class FormatException extends Exception {

    public FormatException( String msg ) {
        this( DiagnosticInfo.create( msg ) );
    }

    public FormatException( DiagnosticInfo... infos ) {
        super( "\n  " + Arrays.stream( infos )
                .map( i -> i.toString() )
                .collect( Collectors.joining( "\n  " ) )
        );
    }

    public FormatException( Collection<Diagnostic<? extends JavaFileObject>> errs ) {
        super( "\n  " + errs.stream()
                .map( e -> e.toString() )
                .collect( Collectors.joining( "\n  " ) )
        );
    }

}
