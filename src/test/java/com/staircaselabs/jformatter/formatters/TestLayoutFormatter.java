package com.staircaselabs.jformatter.formatters;

import static com.google.common.truth.Truth.assertThat;

import com.staircaselabs.jformatter.core.FormatException;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestLayoutFormatter {

    private static String arraysExpected;
    private static String binaryAndUnaryExpected;
    private static String forLoopsExpected;
    private static String labeledBreaksExpected;
    private LayoutFormatter formatter;

    @BeforeClass
    public static void loadExpectedOutputs() throws IOException {
        arraysExpected = Helper.readFileToString( "src/test/data/layout/ArraysPristine.java" );
        binaryAndUnaryExpected = Helper.readFileToString( "src/test/data/layout/BinaryAndUnaryPristine.java" );
        forLoopsExpected = Helper.readFileToString( "src/test/data/layout/ForLoopsPristine.java" );
        labeledBreaksExpected = Helper.readFileToString( "src/test/data/layout/LabeledBreaksPristine.java" );
    }

    @Before
    public void clearScanner() {
        formatter = new LayoutFormatter();
    }

    @Test
    public void shouldLayoutArrays() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/layout/Arrays.java" );
        String newText = formatter.format( text );
        assertThat( newText ).isEqualTo( arraysExpected );
    }

    @Test
    public void shouldLayoutBinaryAndUnary() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/layout/BinaryAndUnary.java" );
        String newText = formatter.format( text );
        assertThat( newText ).isEqualTo( binaryAndUnaryExpected );
    }

    @Test
    public void shouldLayoutForLoops() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/layout/ForLoops.java" );
        String newText = formatter.format( text );
        assertThat( newText ).isEqualTo( forLoopsExpected );
    }

    @Test
    public void shouldLayoutLabeledBreaks() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/layout/LabeledBreaks.java" );
        String newText = formatter.format( text );
        assertThat( newText ).isEqualTo( labeledBreaksExpected );
    }

}
