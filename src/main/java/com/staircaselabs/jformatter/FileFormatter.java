package com.staircaselabs.jformatter;

import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.formatters.BraceInserter;
import com.staircaselabs.jformatter.formatters.LeftBraceCuddler;
import com.staircaselabs.jformatter.formatters.HeaderFormatter;
import com.staircaselabs.jformatter.formatters.ImportsSorter;
import com.staircaselabs.jformatter.formatters.RightBraceCuddler;
import com.staircaselabs.jformatter.formatters.TrailingWhitespaceRemover;
import com.staircaselabs.jformatter.formatters.UnusedImportsRemover;

public class FileFormatter implements Callable<Boolean> {
    private final Path path;
    private String originalText;
    private String workingText;

    public FileFormatter( Path path ) {
        this.path = path;
    }

    @Override
    //  public String call() throws FormatterException {
    public Boolean call() throws InterruptedException, FormatException {
        originalText = workingText = readFileToString( path );

//        workingText = HeaderFormatter.format( workingText );
//        workingText = TrailingWhitespaceRemover.format( workingText );
//        workingText = UnusedImportsRemover.format( workingText );
//        workingText = ImportsSorter.format( workingText );
        workingText = new BraceInserter().format( workingText );
//        workingText = LeftBraceCuddler.format( workingText );
//        workingText = RightBraceCuddler.format( workingText );


        System.out.println( workingText );

        //TODO why even return anything here?  it will either succeed or throw
        return true;
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
