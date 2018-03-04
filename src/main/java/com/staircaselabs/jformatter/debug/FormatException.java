package com.staircaselabs.jformatter.debug;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Checked exception class for formatter errors
 */
public final class FormatException extends Exception {

    private static final long serialVersionUID = 1L;

    public FormatException( String msg ) {
        super( msg );
    }

    public FormatException( DiagnosticInfo... infos ) {
        super( "\n  " + Arrays.stream( infos ).map( DiagnosticInfo::toString ).collect( Collectors.joining( "\n  " ) ) );
    }

    public FormatException( Collection<Diagnostic<? extends JavaFileObject>> errs ) {
        super( "\n  " + errs.stream().map( e -> e.toString() ).collect( Collectors.joining( "\n  " ) ) );
    }

}
