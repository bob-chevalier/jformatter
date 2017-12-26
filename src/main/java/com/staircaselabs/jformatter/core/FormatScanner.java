package com.staircaselabs.jformatter.core;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;

import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;

public class FormatScanner extends TreeScanner<Void, Input> {

    protected boolean errorEncountered = false;
    protected boolean rescanRequired = false;

    // we want to sort replacements in reverse order, by replacement range
    protected NavigableSet<Replacement> replacements = new TreeSet<>(
            Comparator.comparingInt( Replacement::getStop ).reversed()
                    .thenComparingInt( Replacement::getStart ).reversed()
                    .thenComparing( Replacement::getNewText )
    );

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

    @Override
    public Void visitErroneous( ErroneousTree node, Input input ) {
        errorEncountered = true;
        return null;
    }

    public void init() {
        replacements.clear();
        errorEncountered = false;
        rescanRequired = false;
    }

    public boolean hasError() {
        return errorEncountered;
    }

    public void forceRescan() {
        rescanRequired = true;
    }

    public boolean isRescanRequired() {
        return rescanRequired;
    }

    public NavigableSet<Replacement> getReplacements() {
        return replacements;
    }

    public void addReplacement( Replacement replacement ) {
        replacements.add( replacement );
    }

}
