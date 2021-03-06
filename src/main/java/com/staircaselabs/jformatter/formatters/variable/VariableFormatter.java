package com.staircaselabs.jformatter.formatters.variable;

import com.staircaselabs.jformatter.core.*;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.staircaselabs.jformatter.formatters.modifier.ModifierFormatter;
import com.sun.source.tree.VariableTree;

import static com.staircaselabs.jformatter.core.CompilationUnitUtils.isValid;
import static com.staircaselabs.jformatter.core.Input.SPACE;

/**
 * Variables are formatted outside of the LayoutFormatter so that member variable annotations can be treated
 * differently from normal variable annotations.  For the former, we would like to have the annotations on separate
 * lines.  We first process all Variables using this formatter, which will separate annotations by a single SPACE.
 * The LayoutFormatter then re-processes class member Variables, inserting newlines.
 */
public class VariableFormatter extends ReplacementFormatter {

    public VariableFormatter() {
        super( new VariableScanner() );
    }

    private static class VariableScanner extends ReplacementScanner {

        private static final String NAME = "VariableFormatter::";

        @Override
        public Void visitVariable( VariableTree node, Input input ) {
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Variable" );
            appendVariable( node, input, replacement, false );

            replacement.build().ifPresent( this::addReplacement );
            return super.visitVariable( node, input );
        }
    }

    public static void appendVariable(
            VariableTree node,
            Input input,
            Replacement.Builder replacement,
            boolean putAnnotationsOnSeparateLines
    ) {
        if( isValid( node.getModifiers() ) ) {
            ModifierFormatter.appendAnnotationsAndFlags(
                    node.getModifiers(),
                    input,
                    replacement, putAnnotationsOnSeparateLines
            );
        }
        if( isValid( node.getType() ) ) {
            replacement.append( node.getType() )
                    .append( SPACE );
        }

        replacement.append( node.getName().toString() );

        if( isValid( node.getInitializer() ) ) {
            replacement.append( SPACE )
                    .append( TokenType.ASSIGNMENT )
                    .append( SPACE )
                    .append( node.getInitializer() );
        }

        replacement.append( TokenType.SEMICOLON );

    }

}
