package com.staircaselabs.jformatter.formatters;

import static com.google.common.truth.Truth.assertThat;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.formatters.TrailingWhitespaceRemover;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTrailingWhitespaceRemover {

    private static String expected;

    @BeforeClass
    public static void loadExpectedOutputs() throws IOException {
        expected = Helper.readFileToString( "src/test/data/trailing_whitespace/Pristine.java" );
    }

    @Test
    public void shouldRemoveTrailingWhitespace() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/trailing_whitespace/ExtraWhitespace.java" );
        String newText = TrailingWhitespaceRemover.format( text );
        assertThat( newText ).isEqualTo( expected );
    }

}
