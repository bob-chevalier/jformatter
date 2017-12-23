package com.staircaselabs.jformatter;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.Padding;
import com.staircaselabs.jformatter.formatters.LayoutFormatter;
import com.staircaselabs.jformatter.formatters.ModifierFormatter;
import com.staircaselabs.jformatter.formatters.ParenthesizedFormatter;
import com.staircaselabs.jformatter.formatters.VariableFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FileFormatter implements Callable<Boolean> {
    private final Path path;
    private String originalText;
    private String workingText;

    public FileFormatter( Path path ) {
        this.path = path;
    }

    @Override
    //  public String call() throws FormatterException {
    public Boolean call() throws FormatException {
        Padding padding = new Padding.Builder().build();

        try {
            originalText = workingText = readFileToString( path );

//          workingText = HeaderFormatter.format( workingText );
//          workingText = TrailingWhitespaceRemover.format( workingText );
//          workingText = UnusedImportsRemover.format( workingText );
//          workingText = ImportsSorter.format( workingText );
//          workingText = new BraceInserter().format( workingText );
//          workingText = new LeftBraceCuddler().format( workingText );
//          workingText = new RightBraceCuddler().format( workingText );
//          workingText = new PaddingFormatter( 1 ).format( workingText );
            workingText = new ModifierFormatter().format( workingText );
            workingText = new VariableFormatter().format( workingText );
            workingText = new ParenthesizedFormatter( padding ).format( workingText );
            boolean cuddleBraces = true;
            workingText = new LayoutFormatter( padding, cuddleBraces ).format( workingText );

            System.out.println( workingText );
        } catch ( FormatException ex ) {
            // add filename info to the exception
            throw new FormatException( ex.getMessage() + " in file: " + path.toString() );
        }

        return !workingText.equals( originalText );
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
