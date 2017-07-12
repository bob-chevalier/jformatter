package com.staircaselabs.jformatter.formatters;

import static com.google.common.truth.Truth.assertThat;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.formatters.ImportsSorter;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestImportsSorter {

    private static String expected;

    @BeforeClass
    public static void loadExpectedOutputs() throws IOException {
        expected = Helper.readFileToString( "src/test/data/imports_sorter/Pristine.java" );
    }

    @Test
    public void shouldRemoveExtraImports() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/imports_sorter/Unordered.java" );
        String newText = ImportsSorter.format( text );
        assertThat( newText ).isEqualTo( expected );
    }

}
