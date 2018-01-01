package com.staircaselabs.jformatter.core;

import com.sun.source.tree.Tree;

import java.util.Optional;

public class CommentedTree {

    public final Tree tree;
    public final Optional<String> leadingComments;
    public final Optional<String> trailingInlineComment;

    public CommentedTree( Tree tree, Optional<String> leadingComments, Optional<String> trailingInlineComment ) {
        this.tree = tree;
        this.leadingComments = leadingComments;
        this.trailingInlineComment = trailingInlineComment;
    }

}
