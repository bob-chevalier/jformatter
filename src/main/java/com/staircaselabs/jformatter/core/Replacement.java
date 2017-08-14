package com.staircaselabs.jformatter.core;

public class Replacement {

    private int startInclusive;
    private int endExclusive;
    private String newText;

    public Replacement( int startInclusive, int endExclusive, String newText ) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
        this.newText = newText;
    }

    public int getStart() {
        return startInclusive;
    }

    public int getEnd() {
        return endExclusive;
    }

    public String getNewText() {
        return newText;
    }

    public void apply( StringBuilder builder ) throws FormatException {
        if( startInclusive >= endExclusive ) {
            StringBuilder sb = new StringBuilder( "Invalid replacement range: [" );
            sb.append( startInclusive );
            sb.append( ", " );
            sb.append( endExclusive );
            sb.append( ") '" );
            sb.append( newText );
            sb.append( "'" );
            throw new FormatException( sb.toString() );
        }
        builder.replace( startInclusive, endExclusive, newText );
    }

}
