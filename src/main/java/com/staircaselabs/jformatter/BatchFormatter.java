package com.staircaselabs.jformatter;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class BatchFormatter {

    private static final int MAX_THREADS = 20;

    public static boolean formatFiles( Collection<String> files ) throws IOException {
        PrintWriter outWriter = new PrintWriter( new OutputStreamWriter( System.out, UTF_8 ) );
        PrintWriter errWriter = new PrintWriter( new OutputStreamWriter( System.err, UTF_8 ) );

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
                executorService.submit( new FileFormatter( path ) )
            );
        }

        // wait for all threads to complete and check for any errors
        boolean allSucceeded = true;
        for( Map.Entry<Path, Future<Boolean>> entry : results.entrySet() ) {
            try {
                allSucceeded = entry.getValue().get() && allSucceeded;
                outWriter.println( entry.getKey() + " " + (entry.getValue().get() ? "succeeded" : "failed") );
                outWriter.flush();
            } catch( InterruptedException | ExecutionException e ) {
                errWriter.println( entry.getKey() );
                e.printStackTrace( errWriter );
                errWriter.flush();
                allSucceeded = false;
            }
        }

        errWriter.flush();
        outWriter.flush();
        return allSucceeded;
    }

    public static void main( String[] args ) throws Exception {
        BatchFormatterOptions opts = BatchFormatterOptions.parseArgs( args );
        boolean success = BatchFormatter.formatFiles( opts.files );
        System.exit( success ? 0 : 1 );
    }

}
