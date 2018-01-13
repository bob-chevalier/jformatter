package com.staircaselabs.jformatter.core;

public class Config {

    // indentation parameters
    public boolean convertTabsToSpaces = true;
    public int tabWidth = 4;
    public int numTabsAfterLineWrap = 2;

    // padding parameters
    public int methodArguments = 1;
    public int groupingParentheses = 1;
    public int typeCasts = 1;
    public int typeParameters = 1;
    public int arrays = 1;
    public int trailingMethodNames = 1;

    // line-wrapping
    public int maxLineWidth = 120;
    public boolean cuddleBraces = true;
    public boolean closingParenthesesOnNewLine = true;

}

