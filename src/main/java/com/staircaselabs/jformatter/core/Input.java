package com.staircaselabs.jformatter.core;

import static com.staircaselabs.jformatter.core.TokenUtils.getLinebreak;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;

public class Input {

    public static final String SPACE = " ";

    public List<TextToken> tokens;
    public EndPosTable endPosTable;
    public String newline;
    private NavigableMap<Integer, Integer> positionToIndexMap = new TreeMap<>();

    public Input( List<TextToken> tokens, EndPosTable endPosTable ) {
        this.tokens = tokens;
        this.endPosTable = endPosTable;
        this.newline = getLinebreak( tokens );

        for( int idx = 0; idx < tokens.size(); idx++ ) {
            TextToken token = tokens.get( idx );
            if( token.type != TokenType.EOF ) {
                positionToIndexMap.put( token.beginInclusive, idx );
            }
        }
    }

    public int getFirstTokenIndex( Tree tree ) {
        return getTokenIndexFromPosition( ((JCTree)tree).getStartPosition() );
    }

    public int getLastTokenIndex( Tree tree ) {
        return getTokenIndexFromPosition( ((JCTree)tree).getEndPosition( endPosTable ) );
    }

    public int getTokenIndexFromPosition( int charPosition ) {
        //TODO add comment explaining how this lookup works
        return positionToIndexMap.floorEntry( charPosition ).getValue();
    }

    public boolean containsComments( int startInclusive, int endExclusive ) {
        return TokenUtils.containsComments( tokens, startInclusive, endExclusive );
    }

    public boolean contains( int startInclusive, int endExclusive, TokenType... types ) {
        return TokenUtils.contains( tokens, startInclusive, endExclusive, types );
    }

    public OptionalInt findNext( int startInclusive, int endExclusive, TokenType... types ) {
        return TokenUtils.findNext( tokens, startInclusive, endExclusive, types );
    }

    public OptionalInt findNext( int startInclusive, TokenType... types ) {
        return TokenUtils.findNext( tokens, startInclusive, types );
    }

    public OptionalInt findNextByExclusion( int startInclusive, int endExclusive, TokenType... types ) {
        return TokenUtils.findNextByExclusion( tokens, startInclusive, endExclusive, types );
    }

    public OptionalInt findNextByExclusion( int startInclusive, TokenType... types ) {
        return TokenUtils.findNextByExclusion( tokens, startInclusive, types );
    }

    public OptionalInt findPrev( int startInclusive, int endExclusive, TokenType... types ) {
        return TokenUtils.findPrev( tokens, startInclusive, endExclusive, types );
    }

    public OptionalInt findPrev( int stopExclusive, TokenType... types ) {
        return TokenUtils.findPrev( tokens, stopExclusive, types );
    }

    public OptionalInt findPrevByExclusion( int startInclusive, int stopExclusive, TokenType... types ) {
        return TokenUtils.findPrevByExclusion( tokens, startInclusive, stopExclusive, types );
    }

    public OptionalInt findPrevByExclusion( int stopExclusive, TokenType... types ) {
        return TokenUtils.findPrevByExclusion( tokens, stopExclusive, types );
    }

    public boolean isValid( Tree tree ) {
        // Not sure why, but occasionally scanner produces non-printable trees
        return (tree != null && ((JCTree)tree).getStartPosition() >= 0);
    }

    public String stringifyTokens() {
        return TokenUtils.stringifyTokens( tokens, 0, tokens.size() );
    }

    public String stringifyTokens( int startInclusive ) {
        return TokenUtils.stringifyTokens( tokens, startInclusive, tokens.size() );
    }

    public String stringifyTokens( int startInclusive, int endExclusive ) {
        return TokenUtils.stringifyTokens( tokens, startInclusive, endExclusive );
    }

    public String stringifyTree( Tree tree ) {
        int startIdx = getFirstTokenIndex( tree );
        int endIdx = getLastTokenIndex( tree );
        return TokenUtils.stringifyTokens( tokens, startIdx, endIdx );
    }

}
