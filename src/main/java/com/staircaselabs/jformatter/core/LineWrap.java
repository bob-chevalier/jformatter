package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.core.LineWrapPriority.Primary;
import com.staircaselabs.jformatter.core.LineWrapPriority.Secondary;

public enum LineWrap {
    ASSIGNMENT( "A", Primary.ASSIGNMENT, Secondary.ASSIGNMENT ),
    EXTENDS( "E", Primary.EXTENDS, Secondary.EXTENDS ),
    IMPLEMENTS( "I", Primary.IMPLEMENTS, Secondary.IMPLEMENTS ),
    MEMBER_SELECT( "MS", Primary.MEMBER_SELECT, Secondary.MEMBER_SELECT),
    METHOD_ARG( "MA", Primary.METHOD_ARG, Secondary.METHOD_ARG ),
    TERNARY( "T", Primary.TERNARY, Secondary.TERNARY ),
    THROWS( "TH", Primary.THROWS, Secondary.THROWS ),
    UNBOUND_LIST_ITEM( "UL", Primary.UNBOUND_LIST_ITEM, Secondary.UNBOUND_LIST_ITEM );

    private final String shortName;

    private final int primaryPriority;
    private final int secondaryPriority;

    private LineWrap(String shortName, Primary primary, Secondary secondary ) {
        this.shortName = shortName;

        // ordinals range from [0-n), so the priority of a value is the inverse of its ordinal
        this.primaryPriority = Primary.values().length - primary.ordinal();
        this.secondaryPriority = Secondary.values().length - secondary.ordinal();
    }

    public int getPrimaryPriority() {
        return primaryPriority;
    }

    public int getSecondaryPriority() {
        return secondaryPriority;
    }

    @Override
    public String toString() {
        return shortName;
    }

}
