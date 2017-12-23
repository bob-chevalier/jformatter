package com.staircaselabs.jformatter.formatters;

import com.staircaselabs.jformatter.core.FormatScanner;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.Padding;
import com.staircaselabs.jformatter.core.Replacement;
import com.staircaselabs.jformatter.core.ScanningFormatter;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.VariableTree;

import static com.staircaselabs.jformatter.core.Input.SPACE;

/**
 * Parentheses are formatted outside of the LayoutFormatter so that we can apply different padding sizes to each of
 * the following types of parentheses:
 *      - normal grouping parentheses
 *      - if block parentheses
 *      - else-if block parentheses
 *      - do-while block parentheses
 *      - switch block parentheses
 *      - synchronized block parentheses
 *
 * We first process all parentheses using this formatter, which will apply padding appropriate for normal grouping
 * parentheses.  The LayoutFormatter then re-processes all other types of parentheses, applying appropriate padding.
 */
public class ParenthesizedFormatter extends ScanningFormatter {

    public ParenthesizedFormatter( Padding padding ) {
        super( new ParenthesizedFormatterScanner( padding ) );
    }

    private static class ParenthesizedFormatterScanner extends FormatScanner {

        private static final boolean VERBOSE = true;
        private static final boolean ENABLED = true;
        private static final String NAME = "ParenthesizedFormatter::";

        private final Padding padding;

        private ParenthesizedFormatterScanner( Padding padding ) {
            this.padding = padding;
        }

        @Override
        public Void visitParenthesized(ParenthesizedTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======ParenthesizedFormatter::visitParenthesized======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Parenthesized" )
                    .append( TokenType.LEFT_PAREN )
                    .append( padding.parenGrouping )
                    .append( node.getExpression() )
                    .append( padding.parenGrouping )
                    .append( TokenType.RIGHT_PAREN );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitParenthesized( node, input );
        }
    }

}
