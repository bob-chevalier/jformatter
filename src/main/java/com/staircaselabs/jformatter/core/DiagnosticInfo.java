package com.staircaselabs.jformatter.core;

public class DiagnosticInfo {

    private final int line;
    private final int column;
    private final String msg;

    public static DiagnosticInfo create( String msg ) {
        return new DiagnosticInfo( -1, -1, msg );
    }

    public static DiagnosticInfo create( int line, int column, String msg ) {
        if( line < 0 ) {
            throw new IllegalArgumentException( "Expected line to be greater than zero, it was " + line );
        } else if( column < 0 ) {
            throw new IllegalArgumentException( "Expected column to be greater than zero, it was " + column );
        } else if( msg == null ){
            throw new IllegalArgumentException( "Expected non-null msg" );
        }

        return new DiagnosticInfo( line, column, msg );
    }

    private DiagnosticInfo( int line, int column, String msg ) {
        this.line = line;
        this.column = column;
        this.msg = msg;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        if( line >= 0 ) {
            builder.append( line ).append( ":" );
        }
        if( column >= 0 ) {
            // internal column numbers are 0-based, but diagnostics use 1-based indexing by convention
            builder.append( column + 1 ).append( ":" );
        }
        builder.append( msg );

        return builder.toString();
    }

}
