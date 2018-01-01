package com.staircaselabs.jformatter.core;

import java.util.Collections;

public class Indent {

    private final String indentText;
    private final int tabWidth;

    private Indent( String indentText, int tabWidth ) {
        this.indentText = indentText;
        this.tabWidth = tabWidth;
    }

    public static Indent spaces( int tabWidth ) {
        return new Indent( String.join( "", Collections.nCopies( tabWidth, " " ) ), tabWidth );
    }

    public static Indent tabs( int tabWidth ) {
        return new Indent( "\t", tabWidth );
    }

    public String getText( int numIndents ) {
        return String.join( "", Collections.nCopies( numIndents, indentText ) );
    }

    public int getWidth( int numIndents ) {
        return numIndents * tabWidth;
    }

}
