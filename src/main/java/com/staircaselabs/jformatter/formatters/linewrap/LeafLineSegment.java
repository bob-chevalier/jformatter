package com.staircaselabs.jformatter.formatters.linewrap;

import com.staircaselabs.jformatter.core.DotFile;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.staircaselabs.jformatter.core.params.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LeafLineSegment extends LineSegment {

    protected final List<TextToken> tokens = new ArrayList<>();
    protected int width = 0;

    public LeafLineSegment(int prevOffset, List<TextToken> tokens, LineSegment parent) {
        super(parent);
        this.tokens.addAll( tokens );
        offset = prevOffset + tokens.get( 0 ).getIndentOffset();
        width = tokens.stream().mapToInt( TextToken::getWidth ).sum();
    }

    public LeafLineSegment(int prevOffset, LineSegment parent) {
        super(parent);
        offset = prevOffset;
    }

    public void addTokens(List<TextToken> tokens) {
        this.tokens.addAll( tokens );
        offset += tokens.get( 0 ).getIndentOffset();
        width += tokens.stream().mapToInt( TextToken::getWidth ).sum();
    }

    public void appendLineBreak(String newline) {
        tokens.add(new TextToken(newline, TokenType.NEWLINE, 0, 0));
    }

    public int getWidth() {
        return offset + width;
    }

    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    @Override
    public List<TextToken> getTokens() {
        return tokens;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int getLeafCount() {
        return 1;
    }

    @Override
    public LineSegment getFirstLeaf() {
        return this;
    }

    @Override
    public LineSegment getLastLeaf() {
        return this;
    }

    @Override
    public void loadDotFile(String parentId, DotFile dotfile) {
        // generate a unique ID for this node and remove any dashes
        String uuid = UUID.randomUUID().toString();

        // strip off any newlines and double-quotes because Graphviz doesn't like them
        String label = tokens.stream().map(TextToken::toString).collect(Collectors.joining());
        label = label.replace("\"", "");
        label = "(" + offset + ")" + label;

        // add a label entry for this node
        dotfile.addNode(uuid, label);

        // add an edge from parent to this node
        if (parentId != null) {
            dotfile.addEdge(parentId, uuid);
        }
    }

    @Override
    public String toString() {
        String indentText = width == 0 ? "" : Config.INSTANCE.indent.getText( offset );
        return indentText + tokens.stream().map(TextToken::toString).collect(Collectors.joining());
    }

}
