package com.staircaselabs.jformatter;

import com.staircaselabs.jformatter.debug.FormatException;
import com.staircaselabs.jformatter.formatters.Formatters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FileFormatter implements Callable<Boolean> {
    private static final Logger log = Logger.getLogger( FileFormatter.class.getName() );
    private final Path path;
    private String originalText;
    private String newText;
    private Formatters formatters = new Formatters();

    public FileFormatter( Path path ) {
        this.path = path;
    }

    @Override
    //  public String call() throws FormatterException {
    public Boolean call() throws FormatException {
        try {
            originalText = readFileToString( path );
            newText = formatters.format( originalText );

            log.config( "============" );
            log.config( newText );
            log.config( "============" );
        } catch ( FormatException ex ) {
            // add filename info to the exception
            throw new FormatException( ex.getMessage() + " in file: " + path.toString() );
        }

        return !newText.equals( originalText );
    }

    protected String readFileToString( Path path ) throws FormatException {
        try {
            return new String( Files.readAllBytes( path ), UTF_8 );
        } catch( IOException e ) {
            throw new FormatException( path + " could not be read. "  + e.getMessage() );
        }
    }

//    protected void updateFile( String text ) throws FormatException {
//        if( !text.equals( originalText ) ) {
//            try {
//                Files.write( path, text.getBytes( UTF_8 ) );
//            } catch( IOException e ) {
//                throw new FormatException( path + " could not be written. "  + e.getMessage() );
//            }
//        }
//    }

}
