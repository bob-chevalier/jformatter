package com.staircaselabs.jformatter.core;

import com.sun.istack.internal.NotNull;

public class LineWrapInfo {

    public final int maxLineWidth;
    public final int numTabsAfterLineBreak;
    public final boolean methodArgsOnNewLine;
    public final boolean closingParensOnNewLine;
    public final int assignmentLineWrapTabs;
    public final int extendsLineWrapTabs;
    public final int implementsLineWrapTabs;
    public final int memberSelectLineWrapTabs;
    public final int methodArgumentLineWrapTabs;
    public final int ternaryLineWrapTabs;
    public final int throwsLineWrapTabs;
    public final int unboundListItemLineWrapTabs;

    private LineWrapInfo(
            @NotNull Integer maxLineWidth,
            @NotNull Integer numTabsAfterLineBreak,
            @NotNull Boolean methodArgsOnNewLine,
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
        this.numTabsAfterLineBreak = numTabsAfterLineBreak;
        this.methodArgsOnNewLine = methodArgsOnNewLine;
        this.closingParensOnNewLine = closingParensOnNewLine;
        this.assignmentLineWrapTabs = assignmentLineWrapTabs;
        this.extendsLineWrapTabs = extendsLineWrapTabs;
        this.implementsLineWrapTabs = implementsLineWrapTabs;
        this.memberSelectLineWrapTabs = memberSelectLineWrapTabs;
        this.methodArgumentLineWrapTabs = methodArgumentLineWrapTabs;
        this.ternaryLineWrapTabs = ternaryLineWrapTabs;
        this.throwsLineWrapTabs = throwsLineWrapTabs;
        this.unboundListItemLineWrapTabs = unboundListItemLineWrapTabs;
    }

    public static class Builder {
        private Integer maxLineWidth = null;
        private Integer numTabsAfterLineBreak = null;
        private Boolean methodArgsOnNewLine = null;
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

        public Builder numTabsAfterLineBreak( int numTabsAfterLineBreak ) {
            this.numTabsAfterLineBreak = numTabsAfterLineBreak;
            return this;
        }

        public Builder methodArgsOnNewLine( boolean methodArgsOnNewLine ) {
            this.methodArgsOnNewLine = methodArgsOnNewLine;
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
                numTabsAfterLineBreak,
                methodArgsOnNewLine,
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
