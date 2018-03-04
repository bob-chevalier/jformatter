package com.staircaselabs.jformatter.formatters;

import com.staircaselabs.jformatter.core.Formatter;
import com.staircaselabs.jformatter.debug.FormatException;
import com.staircaselabs.jformatter.formatters.header.HeaderFormatter;
import com.staircaselabs.jformatter.formatters.imports.SortedImportsFormatter;
import com.staircaselabs.jformatter.formatters.layout.LayoutFormatter;
import com.staircaselabs.jformatter.formatters.linewrap.LineBreakFormatter;
import com.staircaselabs.jformatter.formatters.modifier.ModifierFormatter;
import com.staircaselabs.jformatter.formatters.parentheses.ParenthesesFormatter;
import com.staircaselabs.jformatter.formatters.variable.VariableFormatter;
import com.staircaselabs.jformatter.formatters.whitespace.TrailingWhitespaceFormatter;
import com.staircaselabs.jformatter.formatters.whitespace.WhitespaceFormatter;

import java.util.ArrayList;
import java.util.List;

public class Formatters {

    protected List<Formatter> formatters = new ArrayList<>();

    public Formatters() {
        formatters.add( new WhitespaceFormatter() );
        formatters.add( new HeaderFormatter() );
        formatters.add( new SortedImportsFormatter() );
        formatters.add( new ModifierFormatter() );
        formatters.add( new VariableFormatter() );
        formatters.add( new ParenthesesFormatter() );
        formatters.add( new LayoutFormatter() );
        formatters.add( new LineBreakFormatter() );
        formatters.add( new TrailingWhitespaceFormatter() );
    }

    public String format( final String originalText ) throws FormatException {
        String workingText = originalText;
        for( Formatter formatter : formatters ) {
            workingText = formatter.format( workingText );
        }
        return workingText;
    }

}
