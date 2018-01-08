package com.staircaselabs.jformatter;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.Indent;
import com.staircaselabs.jformatter.core.Padding;
import com.staircaselabs.jformatter.formatters.*;

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
        boolean cuddleBraces = true;
        Padding padding = new Padding.Builder().build();
        int tabWidth = 4;
        int numLineWrapTabs = 2;
        Indent indent = Indent.spaces( tabWidth, numLineWrapTabs );
        int maxLineWidth = 60;

        try {
            originalText = workingText = readFileToString( path );

            workingText = new WhitespaceFormatter( true ).format( workingText );
            workingText = new HeaderFormatter().format( workingText );
            workingText = new SortedImportsFormatter().format( workingText );
            workingText = new ModifierFormatter().format( workingText );
            workingText = new VariableFormatter().format( workingText );
            workingText = new ParenthesesFormatter( padding ).format( workingText );
            workingText = new LayoutFormatter( padding, cuddleBraces ).format( workingText );
            workingText = new LineBreakFormatter( indent, maxLineWidth ).format( workingText );
            workingText = new WhitespaceFormatter( false ).format( workingText );

//            System.out.println( "============" );
//            System.out.println( originalText );
            System.out.println( "============" );
            System.out.println( workingText );
            System.out.println( "============" );
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
