package com.staircaselabs.jformatter.formatters;

import static com.google.common.truth.Truth.assertThat;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.formatters.PaddingFormatter;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPaddingFormatter {

    private static String arraysOneSpace;
    private static String conditionalsOneSpace;
    private static String doWhilesOneSpace;
    private static String enhancedForLoopsOneSpace;
    private static String forLoopsOneSpace;
    private static String methodsOneSpace;
    private static String newClassesOneSpace;
    private static String switchesOneSpace;
    private static String tryCatchesOneSpace;
    private static String whileLoopsOneSpace;
    private static String arraysZeroSpace;
    private static String conditionalsZeroSpace;
    private static String doWhilesZeroSpace;
    private static String enhancedForLoopsZeroSpace;
    private static String forLoopsZeroSpace;
    private static String methodsZeroSpace;
    private static String newClassesZeroSpace;
    private static String switchesZeroSpace;
    private static String tryCatchesZeroSpace;
    private static String whileLoopsZeroSpace;
    private PaddingFormatter paddingFormatter;

    @BeforeClass
    public static void loadExpectedOutputs() throws IOException {
        // single-space expected values
        arraysOneSpace = Helper.readFileToString( "src/test/data/padding/ArraysOneSpace.java" );
        conditionalsOneSpace = Helper.readFileToString( "src/test/data/padding/ConditionalsOneSpace.java" );
        doWhilesOneSpace = Helper.readFileToString( "src/test/data/padding/DoWhilesOneSpace.java" );
        enhancedForLoopsOneSpace = Helper.readFileToString( "src/test/data/padding/EnhancedForLoopsOneSpace.java" );
        forLoopsOneSpace = Helper.readFileToString( "src/test/data/padding/ForLoopsOneSpace.java" );
        methodsOneSpace = Helper.readFileToString( "src/test/data/padding/MethodsOneSpace.java" );
        newClassesOneSpace = Helper.readFileToString( "src/test/data/padding/NewClassesOneSpace.java" );
        switchesOneSpace = Helper.readFileToString( "src/test/data/padding/SwitchesOneSpace.java" );
        tryCatchesOneSpace = Helper.readFileToString( "src/test/data/padding/TryCatchesOneSpace.java" );
        whileLoopsOneSpace = Helper.readFileToString( "src/test/data/padding/WhileLoopsOneSpace.java" );

        // zero-space expected values
        arraysZeroSpace = Helper.readFileToString( "src/test/data/padding/ArraysZeroSpace.java" );
        conditionalsZeroSpace = Helper.readFileToString( "src/test/data/padding/ConditionalsZeroSpace.java" );
        doWhilesZeroSpace = Helper.readFileToString( "src/test/data/padding/DoWhilesZeroSpace.java" );
        enhancedForLoopsZeroSpace = Helper.readFileToString( "src/test/data/padding/EnhancedForLoopsZeroSpace.java" );
        forLoopsZeroSpace = Helper.readFileToString( "src/test/data/padding/ForLoopsZeroSpace.java" );
        methodsZeroSpace = Helper.readFileToString( "src/test/data/padding/MethodsZeroSpace.java" );
        newClassesZeroSpace = Helper.readFileToString( "src/test/data/padding/NewClassesZeroSpace.java" );
        switchesZeroSpace = Helper.readFileToString( "src/test/data/padding/SwitchesZeroSpace.java" );
        tryCatchesZeroSpace = Helper.readFileToString( "src/test/data/padding/TryCatchesZeroSpace.java" );
        whileLoopsZeroSpace = Helper.readFileToString( "src/test/data/padding/WhileLoopsZeroSpace.java" );
    }

    // zero-space tests
    @Test
    public void shouldUnpadArrays() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 0 );
        String text = Helper.readFileToString( "src/test/data/padding/Arrays.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( arraysZeroSpace );
    }

    @Test
    public void shouldUnpadConditionals() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 0 );
        String text = Helper.readFileToString( "src/test/data/padding/Conditionals.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( conditionalsZeroSpace );
    }

    @Test
    public void shouldUnpadDoWhiles() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 0 );
        String text = Helper.readFileToString( "src/test/data/padding/DoWhiles.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( doWhilesZeroSpace );
    }

    @Test
    public void shouldUnpadEnhancedForLoops() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 0 );
        String text = Helper.readFileToString( "src/test/data/padding/EnhancedForLoops.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( enhancedForLoopsZeroSpace );
    }

    @Test
    public void shouldUnpadForLoops() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 0 );
        String text = Helper.readFileToString( "src/test/data/padding/ForLoops.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( forLoopsZeroSpace );
    }

    @Test
    public void shouldUnpadMethods() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 0 );
        String text = Helper.readFileToString( "src/test/data/padding/Methods.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( methodsZeroSpace );
    }

    @Test
    public void shouldUnpadNewClasses() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 0 );
        String text = Helper.readFileToString( "src/test/data/padding/NewClasses.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( newClassesZeroSpace );
    }

    @Test
    public void shouldUnpadSwitches() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 0 );
        String text = Helper.readFileToString( "src/test/data/padding/Switches.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( switchesZeroSpace );
    }

    @Test
    public void shouldUnpadTryCatches() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 0 );
        String text = Helper.readFileToString( "src/test/data/padding/TryCatches.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( tryCatchesZeroSpace );
    }

    @Test
    public void shouldUnpadWhileLoops() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 0 );
        String text = Helper.readFileToString( "src/test/data/padding/WhileLoops.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( whileLoopsZeroSpace );
    }

    // single-space tests
    @Test
    public void shouldPadArrays() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 1 );
        String text = Helper.readFileToString( "src/test/data/padding/Arrays.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( arraysOneSpace );
    }

    @Test
    public void shouldPadConditionals() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 1 );
        String text = Helper.readFileToString( "src/test/data/padding/Conditionals.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( conditionalsOneSpace );
    }

    @Test
    public void shouldPadDoWhiles() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 1 );
        String text = Helper.readFileToString( "src/test/data/padding/DoWhiles.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( doWhilesOneSpace );
    }

    @Test
    public void shouldPadEnhancedForLoops() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 1 );
        String text = Helper.readFileToString( "src/test/data/padding/EnhancedForLoops.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( enhancedForLoopsOneSpace );
    }

    @Test
    public void shouldPadForLoops() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 1 );
        String text = Helper.readFileToString( "src/test/data/padding/ForLoops.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( forLoopsOneSpace );
    }

    @Test
    public void shouldPadMethods() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 1 );
        String text = Helper.readFileToString( "src/test/data/padding/Methods.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( methodsOneSpace );
    }

    @Test
    public void shouldPadNewClasses() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 1 );
        String text = Helper.readFileToString( "src/test/data/padding/NewClasses.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( newClassesOneSpace );
    }

    @Test
    public void shouldPadSwitches() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 1 );
        String text = Helper.readFileToString( "src/test/data/padding/Switches.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( switchesOneSpace );
    }

    @Test
    public void shouldPadTryCatches() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 1 );
        String text = Helper.readFileToString( "src/test/data/padding/TryCatches.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( tryCatchesOneSpace );
    }

    @Test
    public void shouldPadWhileLoops() throws IOException, FormatException {
        paddingFormatter = new PaddingFormatter( 1 );
        String text = Helper.readFileToString( "src/test/data/padding/WhileLoops.java" );
        String newText = paddingFormatter.format( text );
        assertThat( newText ).isEqualTo( whileLoopsOneSpace );
    }

}
