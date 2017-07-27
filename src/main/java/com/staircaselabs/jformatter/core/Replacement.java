package com.staircaselabs.jformatter.core;

public class Replacement {

    public int startInclusive;
    public int endExclusive;
    public String newText;

    public Replacement( int startInclusive, int endExclusive, String newText ) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
        this.newText = newText;
    }

    public int getStart() {
        return startInclusive;
    }

    public void apply( StringBuilder builder ) {
        builder.replace( startInclusive, endExclusive, newText );
    }

}
