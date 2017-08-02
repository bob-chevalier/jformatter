package com.staircaselabs.jformatter.core;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.sun.source.util.TreeScanner;

public class FormatScanner extends TreeScanner<Void, Input> {

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
}
