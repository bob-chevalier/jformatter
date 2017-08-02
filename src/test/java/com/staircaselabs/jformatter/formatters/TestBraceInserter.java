package com.staircaselabs.jformatter.formatters;

import static com.google.common.truth.Truth.assertThat;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.formatters.BraceInserter;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestBraceInserter {

    private static String expectedConditionals;
    private static String expectedEnhancedForLoops;
    private static String expectedForLoops;
    private static String expectedSynchronizedBlocks;
    private BraceInserter braceInserter;

    @BeforeClass
    public static void loadExpectedOutputs() throws IOException {
        expectedConditionals = Helper.readFileToString( "src/test/data/brace_inserter/ConditionalsPristine.java" );
        expectedEnhancedForLoops = Helper.readFileToString( "src/test/data/brace_inserter/EnhancedForLoopsPristine.java" );
        expectedForLoops = Helper.readFileToString( "src/test/data/brace_inserter/ForLoopsPristine.java" );
        expectedSynchronizedBlocks = Helper.readFileToString( "src/test/data/brace_inserter/SynchronizedBlocksPristine.java" );
    }

    @Before
    public void clearScanner() {
        braceInserter = new BraceInserter();
    }

    @Test
    public void shouldSurroundConditionals() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/brace_inserter/Conditionals.java" );
        String newText = braceInserter.format( text );
        assertThat( newText ).isEqualTo( expectedConditionals );
    }

    @Test
    public void shouldSurroundEnhancedForLoops() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/brace_inserter/EnhancedForLoops.java" );
        String newText = braceInserter.format( text );
        assertThat( newText ).isEqualTo( expectedEnhancedForLoops );
    }

    @Test
    public void shouldSurroundForLoops() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/brace_inserter/ForLoops.java" );
        String newText = braceInserter.format( text );
        assertThat( newText ).isEqualTo( expectedForLoops );
    }

    @Test
    public void shouldSurroundSynchronizedBlocks() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/brace_inserter/SynchronizedBlocks.java" );
        String newText = braceInserter.format( text );
        assertThat( newText ).isEqualTo( expectedSynchronizedBlocks );
    }

}
