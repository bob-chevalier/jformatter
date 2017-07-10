package com.staircaselabs.jformatter.formatters;

import static com.google.common.truth.Truth.assertThat;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.formatters.UnusedImportsRemover;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestUnusedImportsRemover {

    private static String expected;

    @BeforeClass
    public static void loadExpectedOutputs() throws IOException {
        expected = Helper.readFileToString( "src/test/data/unused_imports/Pristine.java" );
    }

    @Test
    public void shouldRemoveExtraImports() throws IOException, FormatException {
        String text = Helper.readFileToString( "src/test/data/unused_imports/ExtraImports.java" );
        String newText = UnusedImportsRemover.format( text );
        assertThat( newText ).isEqualTo( expected );
    }

}
