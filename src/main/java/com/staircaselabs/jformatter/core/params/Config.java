package com.staircaselabs.jformatter.core.params;

import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.LogManager;

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

            // set logging levels from properties file
            if( cfg.loggingProps.startsWith( "~" ) ) {
                cfg.loggingProps = System.getProperty( "user.home" ) + cfg.loggingProps.substring( 1 );
            }
            InputStream inStream = Files.newInputStream( Paths.get( cfg.loggingProps ).toRealPath() );
            LogManager.getLogManager().readConfiguration( inStream );

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
                    .oneArrayElementPerLine( cfg.oneArrayElementPerLine )
                    .oneUnionElementPerLine( cfg.oneUnionElementPerLine )
                    .oneMethodArgPerLine( cfg.oneMethodArgumentPerLine )
                    .closingBracesOnNewLine( cfg.closingBracesOnNewLine )
                    .closingParensOnNewLine( cfg.closingParenthesesOnNewLine )
                    .allowLineWrapAtMethodInvocationMemberSelect( cfg.allowLineWrapAtMethodInvocationMemberSelect )
                    .allowLineWrapAtNewClassMemberSelect( cfg.allowLineWrapAtNewClassMemberSelect )
                    .allowLineWrapAtIdentifierMemberSelect( cfg.allowLineWrapAtIdentifierMemberSelect )
                    .arrayLineWrapTabs( cfg.arrayLineWrapTabs )
                    .assignmentLineWrapTabs( cfg.assignmentLineWrapTabs )
                    .extendsLineWrapTabs( cfg.extendsLineWrapTabs )
                    .implementsLineWrapTabs( cfg.implementsLineWrapTabs )
                    .memberSelectLineWrapTabs( cfg.memberSelectLineWrapTabs )
                    .methodArgumentLineWrapTabs( cfg.methodArgumentLineWrapTabs )
                    .ternaryLineWrapTabs( cfg.ternaryLineWrapTabs )
                    .throwsLineWrapTabs( cfg.throwsLineWrapTabs )
                    .unionLineWrapTabs( cfg.unionLineWrapTabs )
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
        public boolean oneArrayElementPerLine = true;
        public boolean oneUnionElementPerLine = true;
        public boolean oneMethodArgumentPerLine = true;
        public boolean closingBracesOnNewLine = true;
        public boolean closingParenthesesOnNewLine = true;
        public boolean allowLineWrapAtMethodInvocationMemberSelect = true;
        public boolean allowLineWrapAtNewClassMemberSelect = false;
        public boolean allowLineWrapAtIdentifierMemberSelect = false;

        // number of tabs to insert when wrapping various tokens
        public int arrayLineWrapTabs = 2;
        public int assignmentLineWrapTabs = 2;
        public int extendsLineWrapTabs = 2;
        public int implementsLineWrapTabs = 2;
        public int memberSelectLineWrapTabs = 2;
        public int methodArgumentLineWrapTabs = 2;
        public int ternaryLineWrapTabs = 2;
        public int throwsLineWrapTabs = 2;
        public int unionLineWrapTabs = 2;
        public int unboundListItemLineWrapTabs = 2;

        // miscellaneous
        public boolean cuddleBraces = true;

        // logging
        public String loggingProps = "~/logging.properties";
    }
}

