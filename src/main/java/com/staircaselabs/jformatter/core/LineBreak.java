package com.staircaselabs.jformatter.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public enum LineBreak {
    INDEPENDENT("I", 2),
    UNIFIED_FIRST("UF", 2),
    UNIFIED("U", 0),
    UNIFIED_LAST("UL", 0),
    UNIFIED_LAST_UNJUSTIFIED("ULU", -2),
    NON_BREAKING("NB", 0);

    private static Map<LineBreak, Predicate<LineBreak>> validBreakPredicates = new HashMap<>();
    private final String shortName;
    private final int indentOffset;

    private LineBreak(String shortName, int indentOffset) {
        this.shortName = shortName;
        this.indentOffset = indentOffset;
    }

    public int getIndentOffset() {
        return indentOffset;
    }

    public static Predicate<LineBreak> getValidBreakPredicate(LineBreak lineStartBreakType) {
        if (!validBreakPredicates.containsKey(lineStartBreakType)) {
            switch (lineStartBreakType) {
                case INDEPENDENT:
                    validBreakPredicates.put(lineStartBreakType, t -> t != LineBreak.NON_BREAKING);
                    break;
                case UNIFIED_FIRST:
                    validBreakPredicates.put(
                            lineStartBreakType,
                            t -> t == LineBreak.UNIFIED
                                    || t == LineBreak.UNIFIED_LAST
                                    || t == LineBreak.UNIFIED_LAST_UNJUSTIFIED
                    );
                    break;
                case UNIFIED:
                    validBreakPredicates.put(
                            lineStartBreakType,
                            t -> t == LineBreak.UNIFIED
                                    || t == LineBreak.UNIFIED_LAST
                                    || t == LineBreak.UNIFIED_LAST_UNJUSTIFIED
                    );
                    break;
                case UNIFIED_LAST:
                    validBreakPredicates.put(lineStartBreakType, t -> t != LineBreak.NON_BREAKING);
                    break;
                case UNIFIED_LAST_UNJUSTIFIED:
                    validBreakPredicates.put(lineStartBreakType, t -> t != LineBreak.NON_BREAKING);
                    break;
                case NON_BREAKING:
                    validBreakPredicates.put(lineStartBreakType, t -> t != LineBreak.NON_BREAKING);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized BreakType: " + lineStartBreakType.toString());
            }
        }

        return validBreakPredicates.get(lineStartBreakType);
    }

    @Override
    public String toString() {
        return shortName;
    }
}

