package com.staircaselabs.jformatter.core;

import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public enum Config {
    INSTANCE;

    public IndentInfo indent;
    public PaddingInfo padding;
    public LineWrapInfo lineWrap;
    public boolean cuddleBraces;

    public void load( Optional<String> configPath ) {
        try {
            ConfigFile cfg;
            if( configPath.isPresent() ) {
                Path fullPath = Paths.get( configPath.get() ).toRealPath();
                YamlReader reader = new YamlReader(new FileReader(fullPath.toString()));
                cfg = reader.read(ConfigFile.class);
            } else {
                cfg = new ConfigFile();
            }

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
                    .oneMethodArgPerLine( cfg.oneMethodArgumentPerLine )
                    .closingParensOnNewLine( cfg.closingParenthesesOnNewLine )
                    .assignmentLineWrapTabs( cfg.assignmentLineWrapTabs )
                    .extendsLineWrapTabs( cfg.extendsLineWrapTabs )
                    .implementsLineWrapTabs( cfg.implementsLineWrapTabs )
                    .memberSelectLineWrapTabs( cfg.memberSelectLineWrapTabs )
                    .methodArgumentLineWrapTabs( cfg.methodArgumentLineWrapTabs )
                    .ternaryLineWrapTabs( cfg.ternaryLineWrapTabs )
                    .throwsLineWrapTabs( cfg.throwsLineWrapTabs )
                    .unboundListItemLineWrapTabs( cfg.unboundListItemLineWrapTabs )
                    .build();

            cuddleBraces = cfg.cuddleBraces;
        } catch( IOException ex ) {
            throw new RuntimeException( "Unable to parse config: " + configPath, ex );
        }
    }

    /**
     * Object that YAML configuration files are parsed into.
     * If no YAML file is provided, the defaults provided in this object will be used.
     */
    public static class ConfigFile {

        // indentation parameters
        public boolean convertTabsToSpaces = true;
        public int tabWidth = 4;

        // padding parameters
        public int methodArguments = 1;
        public int groupingParentheses = 0;
        public int typeCasts = 0;
        public int typeParameters = 0;
        public int arrays = 1;
        public int trailingMethodNames = 0;

        // line-wrapping
        public int maxLineWidth = 120;
        public boolean oneMethodArgumentPerLine = true;
        public boolean closingParenthesesOnNewLine = true;

        // number of tabs to insert when wrapping various tokens
        public int assignmentLineWrapTabs = 2;
        public int extendsLineWrapTabs = 2;
        public int implementsLineWrapTabs = 2;
        public int memberSelectLineWrapTabs = 2;
        public int methodArgumentLineWrapTabs = 2;
        public int ternaryLineWrapTabs = 2;
        public int throwsLineWrapTabs = 2;
        public int unboundListItemLineWrapTabs = 2;

        // miscellaneous
        public boolean cuddleBraces = true;
    }
}

