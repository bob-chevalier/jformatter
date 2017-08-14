package com.staircaselabs.jformatter.core;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;

public class FormatScanner extends TreeScanner<Void, Input> {

    @Override
    public Void scan( Tree node, Input input ) {
        // filter out tokens that don't have a valid character position
        // NOTE: I'm not sure why TreeScanner would produce a token with pos < 0
        if( node != null && ((JCTree)node).pos < 0 ) {
            return null;
        } else {
            return super.scan( node, input );
        }
    }

    private NavigableSet<Replacement> replacements =
            new TreeSet<>( Comparator.comparingInt( Replacement::getStart ) );

    public NavigableSet<Replacement> getReplacements() {
        return replacements;
    };

    public void addReplacement( Replacement replacement ) {
        replacements.add( replacement );
    }

    public void clearReplacements() {
        replacements.clear();
    }

    protected Optional<Replacement> createReplacement(
            Input input,
            int startIdxInclusive,
            int endIdxExclusive,
            StringBuilder newTextBuilder
    ) {
        String oldText = input.stringifyTokens( startIdxInclusive, endIdxExclusive );
        String newText = newTextBuilder.toString();
        if( !newText.equals( oldText ) ) {
            TextToken firstTokenToReplace = input.tokens.get( startIdxInclusive );
            TextToken lastTokenToReplace = input.tokens.get( (endIdxExclusive - 1) );
            return Optional.of(
                    new Replacement(
                            firstTokenToReplace.start,
                            lastTokenToReplace.end,
                            newText
                    )
            );
        } else {
            return Optional.empty();
        }
    }

    protected Optional<Replacement> createReplacement( Input input, JCTree tree, StringBuilder newTextBuilder ) {
        int startIdx = input.getFirstTokenIndex( tree );
        int endIdx = input.getLastTokenIndex( tree );
        return createReplacement( input, startIdx, endIdx, newTextBuilder );
    }

}
