package com.staircaselabs.jformatter.formatters;

import static com.google.common.truth.Truth.assertThat;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.formatters.LeftBraceCuddler;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestLeftBraceCuddler {

    private static String expectedAnnotationType;
    private static String expectedBlocks;
    private static String expectedCaseStatements;
    private static String expectedConditionals;
    private static String expectedLambdas;
    private static String expectedLoops;
    private static String expectedMiscellaneous;
    private static String expectedTryCatches;
    private LeftBraceCuddler braceCuddler;

    @BeforeClass
    public static void loadExpectedOutputs() throws IOException {
        expectedAnnotationType = Helper.readFileToString( "src/test/data/cuddle_left/AnnotationTypePristine.java" );
        expectedBlocks = Helper.readFileToString( "src/test/data/cuddle_left/BlocksPristine.java" );
        expectedCaseStatements = Helper.readFileToString( "src/test/data/cuddle_left/CaseStatementsPristine.java" );
        expectedConditionals = Helper.readFileToString( "src/test/data/cuddle_left/ConditionalsPristine.java" );
        expectedLambdas = Helper.readFileToString( "src/test/data/cuddle_left/LambdasPristine.java" );
        expectedLoops = Helper.readFileToString( "src/test/data/cuddle_left/LoopsPristine.java" );
        expectedMiscellaneous = Helper.readFileToString( "src/test/data/cuddle_left/MiscellaneousPristine.java" );
        expectedTryCatches = Helper.readFileToString( "src/test/data/cuddle_left/TryCatchesPristine.java" );
    }

    @Before
    public void clearScanner() {
        braceCuddler = new LeftBraceCuddler();
    }

    @Test
    public void shouldCuddleAnnotationTypeBraces() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/cuddle_left/AnnotationType.java" );
        String newText = braceCuddler.format( text );
        assertThat( newText ).isEqualTo( expectedAnnotationType );
    }

    @Test
    public void shouldCuddleBlockBraces() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/cuddle_left/Blocks.java" );
        String newText = braceCuddler.format( text );
        assertThat( newText ).isEqualTo( expectedBlocks );
    }

    @Test
    public void shouldCuddleCaseStatementBraces() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/cuddle_left/CaseStatements.java" );
        String newText = braceCuddler.format( text );
        assertThat( newText ).isEqualTo( expectedCaseStatements );
    }

    @Test
    public void shouldCuddleConditionalBraces() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/cuddle_left/Conditionals.java" );
        String newText = braceCuddler.format( text );
        assertThat( newText ).isEqualTo( expectedConditionals );
    }

    @Test
    public void shouldCuddleLambdaBraces() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/cuddle_left/Lambdas.java" );
        String newText = braceCuddler.format( text );
        assertThat( newText ).isEqualTo( expectedLambdas );
    }

    @Test
    public void shouldCuddleLoopBraces() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/cuddle_left/Loops.java" );
        String newText = braceCuddler.format( text );
        assertThat( newText ).isEqualTo( expectedLoops );
    }

    @Test
    public void shouldCuddleMiscellaneousBraces() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/cuddle_left/Miscellaneous.java" );
        String newText = braceCuddler.format( text );
        assertThat( newText ).isEqualTo( expectedMiscellaneous );
    }

    @Test
    public void shouldCuddleTryCatchBraces() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/cuddle_left/TryCatches.java" );
        String newText = braceCuddler.format( text );
        assertThat( newText ).isEqualTo( expectedTryCatches );
    }

}
