package com.staircaselabs.jformatter.formatters;

import com.staircaselabs.jformatter.core.*;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import javax.lang.model.element.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static com.staircaselabs.jformatter.core.Input.SPACE;

/**
 * Variables are formatted outside of the LayoutFormatter so that member variable annotations can be treated
 * differently from normal variable annotations.  For the former, we would like to have the annotations on separate
 * lines.  We first process all Variables using this formatter, which will separate annotations by a single SPACE.
 * The LayoutFormatter then re-processes class member Variables, inserting newlines.
 */
public class VariableFormatter extends ScanningFormatter {

    public VariableFormatter() {
        super( new VariableFormatterScanner() );
    }

    private static class VariableFormatterScanner extends FormatScanner {

        private static final boolean VERBOSE = false;
        private static final boolean ENABLED = true;
        private static final String NAME = "VariableFormatter::";

        @Override
        public Void visitVariable( VariableTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitVariable======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Variable" );
            appendVariable( node, input, replacement, false );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitVariable( node, input );
        }
    }

    public static void appendVariable(
            VariableTree node,
            Input input,
            Replacement.Builder replacement,
            boolean putAnnotationsOnSeparateLines
    ) {
        if( input.isValid( node.getModifiers() ) ) {
            ModifierFormatter.appendAnnotationsAndFlags(
                    node.getModifiers(),
                    input,
                    replacement, putAnnotationsOnSeparateLines
            );
        }
        if( input.isValid( node.getType() ) ) {
            replacement.append( node.getType() )
                    .append( SPACE );
        }

        replacement.append( node.getName().toString() );

        if( input.isValid( node.getInitializer() ) ) {
            replacement.append( SPACE )
                    .append( TokenType.ASSIGNMENT )
                    .append( SPACE )
                    .append( node.getInitializer() );
        }

        replacement.append( TokenType.SEMICOLON );

    }

}
