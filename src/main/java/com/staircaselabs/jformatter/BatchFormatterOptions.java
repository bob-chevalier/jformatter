package com.staircaselabs.jformatter;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public final class BatchFormatterOptions {

    @Parameter(
            names = { "-f", "--files" },
            variableArity = true,
            description = "Space delimited files to format",
            required = true
    )
    public List<String> files = new ArrayList<>();

    @Parameter( names = { "-c", "--config" }, description = "Relative config file path" )
    public String config;

    @Parameter( names = { "-h", "--help" }, description = "Print this help message" )
    public boolean help;

    private BatchFormatterOptions() {
    }

    public static BatchFormatterOptions parseArgs( String args[] ) {
        BatchFormatterOptions options = new BatchFormatterOptions();
        JCommander parser = new JCommander( options );

        try {
            parser.parse( args );
            if( options.help ) {
                parser.usage();
                System.exit( 1 );
            }
        } catch( ParameterException e ) {
            System.err.println( e.getMessage() );
            parser.usage();
            System.exit( 1 );
        }

        return options;
    }

}
