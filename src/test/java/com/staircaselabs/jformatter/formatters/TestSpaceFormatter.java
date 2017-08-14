package com.staircaselabs.jformatter.formatters;

import static com.google.common.truth.Truth.assertThat;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.formatters.SpaceFormatter;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSpaceFormatter {

    private static String arraysExpected;
    private SpaceFormatter formatter;

    @BeforeClass
    public static void loadExpectedOutputs() throws IOException {
        arraysExpected = Helper.readFileToString( "src/test/data/space/ArraysPristine.java" );
    }

    @Before
    public void clearScanner() {
        formatter = new SpaceFormatter();
    }

    @Test
    public void shouldSpaceArrays() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/space/Arrays.java" );
        String newText = formatter.format( text );
        assertThat( newText ).isEqualTo( arraysExpected );
    }

}
