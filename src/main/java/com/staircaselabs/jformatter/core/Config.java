package com.staircaselabs.jformatter.core;

import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum Config {
    INSTANCE;

    public IndentInfo indent;
    public PaddingInfo padding;
    public LineWrapInfo lineWrap;
    public boolean cuddleBraces;

    public void load( String configPath ) {
        try {
            Path fullPath = Paths.get(configPath).toRealPath();
            YamlReader reader = new YamlReader(new FileReader(fullPath.toString()));
            ConfigFile cfg = reader.read(ConfigFile.class);

            indent = cfg.convertTabsToSpaces ? IndentInfo.spaces( cfg.tabWidth  ) : IndentInfo.tabs( cfg.tabWidth );

            padding = new PaddingInfo.Builder()
                    .methodArg( cfg.methodArguments )
                    .parenGrouping( cfg.groupingParentheses )
                    .typeCast( cfg.typeCasts )
                    .typeParam( cfg.typeParameters )
                    .array( cfg.arrays )
                    .methodName( cfg.trailingMethodNames )
                    .build();

            lineWrap = new LineWrapInfo.Builder()
                    .maxLineWidth( cfg.maxLineWidth )
                    .numTabsAfterLineBreak( cfg.numTabsAfterLineWrap )
                    .methodArgsOnNewLine( cfg.methodArgumentsOnNewLine )
                    .closingParensOnNewLine( cfg.closingParenthesesOnNewLine )
                    .build();

            cuddleBraces = cfg.cuddleBraces;
        } catch( IOException ex ) {
            throw new RuntimeException( "Unable to parse config: " + configPath, ex );
        }
    }

    public static class ConfigFile {

        // indentation parameters
        public boolean convertTabsToSpaces = true;
        public int tabWidth = 4;

        // padding parameters
        public int methodArguments = 1;
        public int groupingParentheses = 1;
        public int typeCasts = 1;
        public int typeParameters = 1;
        public int arrays = 1;
        public int trailingMethodNames = 1;

        // line-wrapping
        public int maxLineWidth = 120;
        public int numTabsAfterLineWrap = 2;
        public boolean methodArgumentsOnNewLine = true;
        public boolean closingParenthesesOnNewLine = true;

        // miscellaneous
        public boolean cuddleBraces = true;
    }
}

