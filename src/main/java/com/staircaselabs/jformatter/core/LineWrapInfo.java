package com.staircaselabs.jformatter.core;

import java.util.Collections;

public class LineWrapInfo {

    public final int maxLineWidth;
    public final int numTabsAfterLineBreak;
    public final boolean methodArgsOnNewLine;
    public final boolean closingParensOnNewLine;

    private LineWrapInfo(
            int maxLineWidth,
            int numTabsAfterLineBreak,
            boolean methodArgsOnNewLine,
            boolean closingParensOnNewLine
    ) {
        this.maxLineWidth = maxLineWidth;
        this.numTabsAfterLineBreak = numTabsAfterLineBreak;
        this.methodArgsOnNewLine = methodArgsOnNewLine;
        this.closingParensOnNewLine = closingParensOnNewLine;
    }

    public static class Builder {
        private int maxLineWidth = 120;
        private int numTabsAfterLineBreak = 2;
        private boolean methodArgsOnNewLine = false;
        private boolean closingParensOnNewLine = true;

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

        public LineWrapInfo build() {
            return new LineWrapInfo(
                maxLineWidth,
                numTabsAfterLineBreak,
                methodArgsOnNewLine,
                closingParensOnNewLine
            );
        }
    }

}
