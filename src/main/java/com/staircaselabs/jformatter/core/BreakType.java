package com.staircaselabs.jformatter.core;

public enum BreakType {
    ASSIGNMENT( "A" ),
    EXTENDS( "E" ),
    IMPLEMENTS( "I" ),
    METHOD_ARG( "MA" ),
    METHOD_INVOCATION( "MI" ),
    NON_BREAKING( "N" ),
    TERNARY( "T" ),
    THROWS( "TH" ),
    UNBOUND_LIST_ITEM( "UL" );

    private final String shortName;

    private BreakType(String shortName ) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        return shortName;
    }
}

