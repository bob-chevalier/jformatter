package com.staircaselabs.jformatter.formatters;

import static com.google.common.truth.Truth.assertThat;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.formatters.RightBraceCuddler;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestRightBraceCuddler {

    private static String expectedConditionals;
    private static String expectedDoWhileLoops;
    private static String expectedTryCatches;
    private RightBraceCuddler braceCuddler;

    @BeforeClass
    public static void loadExpectedOutputs() throws IOException {
        expectedConditionals = Helper.readFileToString( "src/test/data/cuddle_right/ConditionalsPristine.java" );
        expectedDoWhileLoops = Helper.readFileToString( "src/test/data/cuddle_right/DoWhileLoopsPristine.java" );
        expectedTryCatches = Helper.readFileToString( "src/test/data/cuddle_right/TryCatchesPristine.java" );
    }

    @Before
    public void clearScanner() {
        braceCuddler = new RightBraceCuddler();
    }

    @Test
    public void shouldCuddleConditionalBraces() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/cuddle_right/Conditionals.java" );
        String newText = braceCuddler.format( text );
        assertThat( newText ).isEqualTo( expectedConditionals );
    }

    @Test
    public void shouldCuddleDoWhileLoopBraces() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/cuddle_right/DoWhileLoops.java" );
        String newText = braceCuddler.format( text );
        assertThat( newText ).isEqualTo( expectedDoWhileLoops );
    }

    @Test
    public void shouldCuddleTryCatchBraces() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/cuddle_right/TryCatches.java" );
        String newText = braceCuddler.format( text );
        assertThat( newText ).isEqualTo( expectedTryCatches );
    }

}
