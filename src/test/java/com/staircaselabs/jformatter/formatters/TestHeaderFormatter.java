package com.staircaselabs.jformatter.formatters;

import static com.google.common.truth.Truth.assertThat;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.formatters.HeaderFormatter;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestHeaderFormatter {

    private static String expectedWithHeader;
    private static String expectedWithNoHeader;

    @BeforeClass
    public static void loadExpectedOutputs() throws IOException {
        expectedWithHeader = Helper.readFileToString( "src/test/data/header_formatter/HeaderPristine.java" );
        expectedWithNoHeader = Helper.readFileToString( "src/test/data/header_formatter/NoHeaderPristine.java" );
    }

    @Test
    public void shouldRemoveExtraWhitespace() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/header_formatter/HeaderExtraWhitespace.java" );
        String newText = HeaderFormatter.format( text );
        assertThat( newText ).isEqualTo( expectedWithHeader );
    }

    @Test
    public void shouldRemoveExtraWhitespaceWithNoHeader() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/header_formatter/NoHeaderExtraWhitespace.java" );
        String newText = HeaderFormatter.format( text );
        assertThat( newText ).isEqualTo( expectedWithNoHeader );
    }

    @Test
    public void shouldAddNewlinesAfter() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/header_formatter/HeaderNoNewlines.java" );
        String newText = HeaderFormatter.format( text );
        assertThat( newText ).isEqualTo( expectedWithHeader );
    }

}
