package com.staircaselabs.jformatter.core;

import java.util.Collections;

public class Indent {

    private final String indentText;
    private final int tabWidth;
    private final int numTabsAfterLineWrap;

    private Indent( String indentText, int tabWidth, int numTabsAfterLineWrap ) {
        this.indentText = indentText;
        this.tabWidth = tabWidth;
        this.numTabsAfterLineWrap = numTabsAfterLineWrap;
    }

    public static Indent spaces( int tabWidth, int numLineWrapTabs ) {
        return new Indent( String.join( "", Collections.nCopies( tabWidth, " " ) ), tabWidth, numLineWrapTabs );
    }

    public static Indent tabs( int tabWidth, int numLineWrapTabs ) {
        return new Indent( "\t", tabWidth, numLineWrapTabs );
    }

    public String getText( int numIndents ) {
        return String.join( "", Collections.nCopies( numIndents, indentText ) );
    }

    public int getWidth( int numIndents ) {
        return numIndents * tabWidth;
    }

    public int getNumTabsAfterLineWrap() {
        return numTabsAfterLineWrap;
    }

}
