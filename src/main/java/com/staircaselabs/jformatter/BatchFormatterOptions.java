package com.staircaselabs.jformatter;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
