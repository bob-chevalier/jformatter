package com.staircaselabs.jformatter.formatters;

import com.staircaselabs.jformatter.core.*;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.*;

import javax.lang.model.element.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static com.staircaselabs.jformatter.core.Input.SPACE;

/**
 * Modifiers are formatted outside of the LayoutFormatter so that class annotations, member annotations, and method
 * annotations can be treated differently from normal variable annotations.  For the former, we would like to have
 * the annotations on separate lines.  We first process all Modifiers using this formatter, which will spearate
 * annotations by a single SPACE.  The LayoutFormatter then re-processes the Modifiers for classes and methods,
 * inserting newlines.
 */
public class ModifierFormatter extends ScanningFormatter {

    public ModifierFormatter() {
        super( new ModifierFormatterScanner() );
    }

    private static class ModifierFormatterScanner extends FormatScanner {

        private static final boolean VERBOSE = false;
        private static final boolean ENABLED = true;
        private static final String NAME = "ModifierFormatter::";

        @Override
        public Void visitModifiers( ModifiersTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitModifiers======" );
            if( input.isValid( node ) ) {
                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Modifiers" );
                appendAnnotationsAndFlags( node, input, replacement, false );

                if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            }

            return super.visitModifiers( node, input );
        }

    }

    public static void appendAnnotationsAndFlags(
            ModifiersTree node,
            Input input,
            Replacement.Builder replacement,
            boolean putAnnotationsOnSeparateLines
    ) {
        // annotations
        List<Tree> annotations = node.getAnnotations().stream()
                .map( Tree.class::cast )
                .collect( Collectors.toList() );

        // the actual modifiers (flags)
        List<TokenType> flags = node.getFlags().stream()
                .map( Modifier::toString )
                .map( TokenUtils::tokenTypeFromModifier )
                .collect( Collectors.toList() );

        // annotations can be interleaved with flags so we have to check their positions before appending
        Iterator<Tree> annoIter = annotations.iterator();
        Iterator<TokenType> flagIter = flags.iterator();
        Tree anno = annoIter.hasNext() ? annoIter.next() : null;
        TokenType flag = flagIter.hasNext() ? flagIter.next() : null;
        boolean firstFlagInserted = false;
        while( anno != null || flag != null ) {
            int annoPos = anno == null ? Integer.MAX_VALUE : input.getFirstTokenIndex( anno );
            int flagPos = flag == null
                    ? Integer.MAX_VALUE
                    : input.findNext( replacement.getCurrentPosInclusive(), flag ).getAsInt();

            if( annoPos < flagPos ) {
                replacement.append( anno );
                anno = annoIter.hasNext() ? annoIter.next() : null;
            } else {
                replacement.append( flag );
                flag = flagIter.hasNext() ? flagIter.next() : null;
                firstFlagInserted = true;
            }

            String delimiter = (putAnnotationsOnSeparateLines && !firstFlagInserted) ? input.newline : SPACE;
            replacement.append( delimiter );
        }

        // some annotations, such as '@interface', seem to be incorrectly parsed so we manually append @ symbol here
        // find next remaining non-whitespace, non-newline token, if there is one
        OptionalInt nextTokenPos = input.findNextByExclusion(
                replacement.getCurrentPosInclusive(),
                input.getLastTokenIndex( node ),
                TokenType.WHITESPACE,
                TokenType.NEWLINE
        );

        if( nextTokenPos.isPresent() ) {
            // find last non-whitespace, non-newline token
            int lastTokenPos = input.findPrevByExclusion(
                    nextTokenPos.getAsInt(),
                    input.getLastTokenIndex( node ),
                    TokenType.WHITESPACE,
                    TokenType.NEWLINE
            ).getAsInt();

            // skip over leading whitespace and newlines
            replacement.setCurrentPositionInclusive( nextTokenPos.getAsInt() );

            // append all remaining tokens, excluding trailing whitespace and newlines
            // if we get here, then we're really only expecting to be appending a single @ symbol
            while( replacement.getCurrentPosInclusive() <= lastTokenPos ) {
                replacement.append( replacement.getCurrentPosInclusive() );
            }
        }
    }

}
