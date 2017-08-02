package com.staircaselabs.jformatter.core;

import static com.staircaselabs.jformatter.core.TokenUtils.getLinebreak;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;

public class Input {

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
                positionToIndexMap.put( token.start, idx );
            }
        }
    }

    public int getFirstTokenIndex( JCTree tree ) {
        return getTokenIndexFromPosition( tree.getStartPosition() );
    }

    public int getLastTokenIndex( JCTree tree ) {
        return getTokenIndexFromPosition( tree.getEndPosition( endPosTable ) );
    }

    public int getTokenIndexFromPosition( int charPosition ) {
        //TODO add comment explaining how this lookup works
        return positionToIndexMap.floorEntry( charPosition ).getValue();
    }

}
