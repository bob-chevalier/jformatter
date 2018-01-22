package com.staircaselabs.jformatter.core;

public class LineWrapPriority {

    public enum Strategy {
        PRIMARY,
        SECONDARY
    }

    /**
     * Defines the priorities of the various line-wrap types when using the primary line-wrap strategy.
     * Priorites correspond to the ordinal value of the type within this enum.
     */
    public enum Primary {
        ASSIGNMENT,
        TERNARY,
        MEMBER_SELECT,
        METHOD_ARG,
        ARRAY,
        EXTENDS,
        IMPLEMENTS,
        THROWS,
        UNBOUND_LIST_ITEM
    }

    /**
     * Defines the priorities of the various line-wrap types when using the secondary line-wrap strategy.
     * Priorites correspond to the ordinal value of the type within this enum.
     */
    public enum Secondary {
        TERNARY,
        MEMBER_SELECT,
        METHOD_ARG,
        ARRAY,
        ASSIGNMENT,
        EXTENDS,
        IMPLEMENTS,
        THROWS,
        UNBOUND_LIST_ITEM
    }

}
