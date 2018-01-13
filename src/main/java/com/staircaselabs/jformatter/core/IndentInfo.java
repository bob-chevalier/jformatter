package com.staircaselabs.jformatter.core;

import java.util.Collections;

public class IndentInfo {

    private final String indentText;
    private final int tabWidth;

    private IndentInfo(String indentText, int tabWidth ) {
        this.indentText = indentText;
        this.tabWidth = tabWidth;
    }

    public static IndentInfo spaces(int tabWidth ) {
        return new IndentInfo( String.join( "", Collections.nCopies( tabWidth, " " ) ), tabWidth );
    }

    public static IndentInfo tabs(int tabWidth  ) {
        return new IndentInfo( "\t", tabWidth );
    }

    public String getText( int numIndents ) {
        return String.join( "", Collections.nCopies( numIndents, indentText ) );
    }

    public int getWidth( int numIndents ) {
        return numIndents * tabWidth;
    }

}
