package com.staircaselabs.jformatter.core;

import com.sun.istack.internal.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LineWrapInfo {

    public final int maxLineWidth;
    public final boolean oneMethodArgPerLine;
    public final boolean closingParensOnNewLine;
    private final Map<LineWrap, Integer> numTabs = new HashMap<>();

    private LineWrapInfo(
            @NotNull Integer maxLineWidth,
            @NotNull Boolean oneMethodArgPerLine,
            @NotNull Boolean closingParensOnNewLine,
            @NotNull Integer assignmentLineWrapTabs,
            @NotNull Integer extendsLineWrapTabs,
            @NotNull Integer implementsLineWrapTabs,
            @NotNull Integer memberSelectLineWrapTabs,
            @NotNull Integer methodArgumentLineWrapTabs,
            @NotNull Integer ternaryLineWrapTabs,
            @NotNull Integer throwsLineWrapTabs,
            @NotNull Integer unboundListItemLineWrapTabs
    ) {
        this.maxLineWidth = maxLineWidth;
        this.oneMethodArgPerLine = oneMethodArgPerLine;
        this.closingParensOnNewLine = closingParensOnNewLine;
        numTabs.put( LineWrap.ASSIGNMENT, assignmentLineWrapTabs );
        numTabs.put( LineWrap.EXTENDS, extendsLineWrapTabs );
        numTabs.put( LineWrap.IMPLEMENTS, implementsLineWrapTabs );
        numTabs.put( LineWrap.MEMBER_SELECT, memberSelectLineWrapTabs );
        numTabs.put( LineWrap.METHOD_ARG, methodArgumentLineWrapTabs );
        numTabs.put( LineWrap.TERNARY, ternaryLineWrapTabs );
        numTabs.put( LineWrap.THROWS, throwsLineWrapTabs );
        numTabs.put( LineWrap.UNBOUND_LIST_ITEM, unboundListItemLineWrapTabs );
    }

    public int tabsToInsert( LineWrap type ) {
        return numTabs.get(type);
    }

    public static class Builder {
        private Integer maxLineWidth = null;
        private Boolean oneMethodArgPerLine = null;
        private Boolean closingParensOnNewLine = null;
        private Integer assignmentLineWrapTabs = null;
        private Integer extendsLineWrapTabs = null;
        private Integer implementsLineWrapTabs = null;
        private Integer memberSelectLineWrapTabs = null;
        private Integer methodArgumentLineWrapTabs = null;
        private Integer ternaryLineWrapTabs = null;
        private Integer throwsLineWrapTabs = null;
        private Integer unboundListItemLineWrapTabs = null;

        public Builder maxLineWidth( int maxLineWidth ) {
            this.maxLineWidth = maxLineWidth;
            return this;
        }

        public Builder oneMethodArgPerLine( boolean oneMethodArgPerLine ) {
            this.oneMethodArgPerLine = oneMethodArgPerLine;
            return this;
        }

        public Builder closingParensOnNewLine( boolean closingParensOnNewLine ) {
            this.closingParensOnNewLine = closingParensOnNewLine;
            return this;
        }

        public Builder assignmentLineWrapTabs( int numTabs ) {
            this.assignmentLineWrapTabs = numTabs;
            return this;
        }

        public Builder extendsLineWrapTabs( int numTabs ) {
            this.extendsLineWrapTabs = numTabs;
            return this;
        }

        public Builder implementsLineWrapTabs( int numTabs ) {
            this.implementsLineWrapTabs = numTabs;
            return this;
        }

        public Builder memberSelectLineWrapTabs( int numTabs ) {
            this.memberSelectLineWrapTabs = numTabs;
            return this;
        }

        public Builder methodArgumentLineWrapTabs( int numTabs ) {
            this.methodArgumentLineWrapTabs = numTabs;
            return this;
        }

        public Builder ternaryLineWrapTabs( int numTabs ) {
            this.ternaryLineWrapTabs = numTabs;
            return this;
        }

        public Builder throwsLineWrapTabs( int numTabs ) {
            this.throwsLineWrapTabs = numTabs;
            return this;
        }

        public Builder unboundListItemLineWrapTabs( int numTabs ) {
            this.unboundListItemLineWrapTabs = numTabs;
            return this;
        }

        public LineWrapInfo build() {
            return new LineWrapInfo(
                maxLineWidth,
                oneMethodArgPerLine,
                closingParensOnNewLine,
                assignmentLineWrapTabs,
                extendsLineWrapTabs,
                implementsLineWrapTabs,
                memberSelectLineWrapTabs,
                methodArgumentLineWrapTabs,
                ternaryLineWrapTabs,
                throwsLineWrapTabs,
                unboundListItemLineWrapTabs
            );
        }
    }

}
