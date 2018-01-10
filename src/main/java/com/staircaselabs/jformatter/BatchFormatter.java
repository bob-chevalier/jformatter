package com.staircaselabs.jformatter;

import com.staircaselabs.jformatter.core.Config;
import com.staircaselabs.jformatter.core.ConfigLoader;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class BatchFormatter {

    private static final int MAX_THREADS = 20;

    public static boolean formatFiles( Collection<String> files, Optional<String> configPath ) throws IOException {
        PrintWriter outWriter = new PrintWriter( new OutputStreamWriter( System.out, UTF_8 ) );
        PrintWriter errWriter = new PrintWriter( new OutputStreamWriter( System.err, UTF_8 ) );

        // parse config file if one was provided, otherwise just create a default configuration object
        Config config = configPath.map( ConfigLoader::parse ).orElse( new Config() );

        // convert filenames to paths and filter out non-java files and duplicates
        Set<Path> uniquePaths = new HashSet<>();
        for( String filename : files ) {
            if( filename.endsWith( ".java" ) ) {
                Path path = Paths.get( filename ).toRealPath();
                uniquePaths.add( path );
            } else {
                errWriter.println( "Skipping non-Java file: " + filename );
                errWriter.flush();
            }
        }

        // create a pool of worker threads
        int numThreads = Math.min( MAX_THREADS, uniquePaths.size() );
        if( numThreads <= 0 ) {
            errWriter.println( "No valid files to process" );
            return false;
        }
        ExecutorService executorService = Executors.newFixedThreadPool( numThreads );

        // start formatting the files asynchronously
        Map<Path, Future<Boolean>> results = new LinkedHashMap<>();
        for( Path path : uniquePaths ) {
            results.put(
                path,
                executorService.submit( new FileFormatter( path, config ) )
            );
        }

        // wait for all threads to complete and check for any errors
        List<String> failedFiles = new ArrayList<>();
        List<String> modifiedFiles = new ArrayList<>();
        for( Map.Entry<Path, Future<Boolean>> entry : results.entrySet() ) {
            try {
                if( entry.getValue().get() ) {
                    modifiedFiles.add( entry.getKey().toString() );
                }
                outWriter.println( entry.getKey() + " " + (entry.getValue().get() ? "succeeded" : "failed") );
                outWriter.flush();
            } catch( InterruptedException | ExecutionException e ) {
                //TODO send stacktrace info to logfile not console
                errWriter.println( entry.getKey() );
                e.printStackTrace( errWriter );
                errWriter.flush();
                failedFiles.add( entry.getKey().toString() );
            }
        }

        // print results
        outWriter.println( createMsg( results.size(), "processed" ) );
        outWriter.println( createMsg( modifiedFiles.size(), "modified" ) );
        outWriter.println( createMsg( failedFiles.size(), "failed" ) );
        failedFiles.stream().forEach( outWriter::println );

        errWriter.flush();
        outWriter.flush();
        return failedFiles.isEmpty();
    }

    private static String createMsg( int numFiles, String verb ) {
        StringBuilder sb = new StringBuilder();
        sb.append( numFiles );
        sb.append( " file" );
        if( numFiles != 1 ) {
            sb.append( "s" );
        }
        sb.append( " " );
        sb.append( verb );
        return sb.toString();
    }

    public static void main( String[] args ) throws Exception {
        BatchFormatterOptions opts = BatchFormatterOptions.parseArgs( args );
        Optional<String> configPath = opts.config == null ? Optional.empty() : Optional.of( opts.config );
        boolean success = BatchFormatter.formatFiles( opts.files, configPath );
        System.exit( success ? 0 : 1 );
    }

}
