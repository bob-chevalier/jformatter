package com.staircaselabs.jformatter.core;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.Tree;

public class Replacement {

    private String debugLabel;
    private int startInclusive;
    private int stopExclusive;
    private String oldText;
    private String newText;

    public Replacement( String debugLabel, int startInclusive, int stopExclusive, String oldText, String newText ) {
        this.debugLabel = debugLabel;
        this.startInclusive = startInclusive;
        this.stopExclusive = stopExclusive;
        this.oldText = oldText;
        this.newText = newText;
    }

    public String getDebugLabel() {
        return debugLabel;
    }

    public int getStart() {
        return startInclusive;
    }

    public int getStop() {
        return stopExclusive;
    }

    public String getOldText() {
        return oldText;
    }

    public String getNewText() {
        return newText;
    }

    public void apply( StringBuilder builder ) throws FormatException {
        if( startInclusive >= stopExclusive ) {
            String msg = "Invalid replacement range: [" + startInclusive + ", " + stopExclusive + ") '" + newText + "'";
            throw new FormatException( msg );
        }
        builder.replace( startInclusive, stopExclusive, newText );
    }

    public static class Builder {

        public static final String SPACE = " ";

        private static final TokenType[] COMMENT = {
                TokenType.COMMENT_BLOCK,
                TokenType.COMMENT_JAVADOC,
                TokenType.COMMENT_LINE
        };
        private static final TokenType[] WS_OR_NEWLINE = {
                TokenType.WHITESPACE,
                TokenType.NEWLINE
        };
        private static final TokenType[] WS_NEWLINE_OR_COMMENT = {
                TokenType.WHITESPACE,
                TokenType.NEWLINE,
                TokenType.COMMENT_BLOCK,
                TokenType.COMMENT_JAVADOC,
                TokenType.COMMENT_LINE
        };

        private final Tree treeToReplace;
        private final Input input;
        private final String debugLabel;
        private final StringBuilder output = new StringBuilder();
        private int beginInclusive;
        private int endExclusive;
        private int currentPosInclusive;

        public Builder setCurrentPositionInclusive(int currentPosInclusive ) {
            this.currentPosInclusive = currentPosInclusive;
            return this;
        }

        public int getCurrentPosInclusive() {
            return currentPosInclusive;
        }

        public StringBuilder getOutput() {
            return output;
        }

        public Builder( Tree treeToReplace, Input input, String debugLabel ) {
            this.treeToReplace = treeToReplace;
            this.input = input;
            this.debugLabel = debugLabel;
            this.beginInclusive = input.getFirstTokenIndex( treeToReplace );
            this.endExclusive = input.getLastTokenIndex( treeToReplace );
            this.currentPosInclusive = beginInclusive;
        }

        public Optional<Replacement> build( int startInclusive, int stopExclusive ) {
            String oldText = input.stringifyTokens( startInclusive, stopExclusive );
            String newText = output.toString();
            if( !newText.equals( oldText ) ) {
                TextToken firstTokenToReplace = input.tokens.get( startInclusive );
                TextToken lastTokenToReplace = input.tokens.get( (stopExclusive - 1) );

                return Optional.of(
                        new Replacement(
                                debugLabel,
                                firstTokenToReplace.beginInclusive,
                                lastTokenToReplace.endExclusive,
                                oldText,
                                newText
                        )
                );
            } else {
                return Optional.empty();
            }
        }

        public Optional<Replacement> build() {
            return build( beginInclusive, endExclusive );
        }

        public Builder append( String stringToAppend ) {
            output.append( stringToAppend );
            return this;
        }

        public Builder append( int pos ) {
            appendComments( pos );
            output.append( input.tokens.get( pos ).toString() );
            currentPosInclusive = pos + 1;
            return this;
        }

        /**
         * Append the next token of the given type, if one exists between current position and end of tree.
         * Leading comments and newlines will be appended before the token.
         */
        public Builder append( TokenType type  ) {
            input.findNext( currentPosInclusive, endExclusive, type ).ifPresent( this::append );
            return this;
        }

        public Builder append( Tree tree ) {
            int treeStart = input.getFirstTokenIndex( tree );
            appendComments( treeStart );
            output.append( input.stringifyTree( tree ) );
            currentPosInclusive = input.getLastTokenIndex( tree );
            return this;
        }

        public Builder appendWithLeadingNewlines( TokenType type, int numNewlines ) {
            OptionalInt typePos = input.findNext( currentPosInclusive, endExclusive, type );
            if( typePos.isPresent() ) {
                appendNewlines( typePos.getAsInt(), numNewlines );

                // now append the given token
                append( type );
            }
            return this;
        }

        public Builder appendWithLeadingNewlines( Tree tree, int numNewlines ) {
            int treeStart = input.getFirstTokenIndex( tree );
            appendNewlines( treeStart, numNewlines );

            // now append the given tree
            return append( tree );
        }

        public Builder appendNewlines( int stopPosExclusive, int numNewlines ) {
            // skip any existing newlines or whitespace
            currentPosInclusive =
                    input.findNextByExclusion( currentPosInclusive, stopPosExclusive, TokenType.NEWLINE, TokenType.WHITESPACE )
                            .orElse( currentPosInclusive );

            // append the appropriate number of newlines
            IntStream.range( 0, numNewlines ).forEach( i -> append( input.newline ) );

            return this;
        }

        public Builder appendWithLeadingNewlinesAfterComments( TokenType type, int numNewlines ) {
            int tokenPos = input.findNext( currentPosInclusive, type ).getAsInt();
            appendCommentsAndNewlines( tokenPos, numNewlines );

            // now append the given token
            return append( type );
        }

        public Builder appendWithLeadingNewlinesAfterComments( Tree tree, int numNewlines ) {
            int treeStart = input.getFirstTokenIndex( tree );
            appendCommentsAndNewlines( treeStart, numNewlines );

            // now append the given tree
            output.append( input.stringifyTree( tree ) );
            currentPosInclusive = input.getLastTokenIndex( tree );

            return this;
        }

        private void appendCommentsAndNewlines( int stopPos, int numNewlines ) {
            // find final leading comment before given tree, excluding newlines and whitespace
            OptionalInt leadingComments =
                    input.findPrevByExclusion( currentPosInclusive, stopPos, TokenType.NEWLINE, TokenType.WHITESPACE );
            leadingComments.ifPresent( this::append );

            // skip over any newlines or whitespace between leading comments and token
            setCurrentPositionInclusive( stopPos );

            // append the appropriate number of leading newlines
            IntStream.range( 0, numNewlines ).forEach( i -> append( input.newline ) );
        }

        // this version of appendList will always insert the given delimiter, even if it doesn't exist in original text
        public Builder appendList( List<Tree> list, String delimiter ) {
            if( list.isEmpty() ) {
                return this;
            }

            // append first tree and any leading comments
            append( list.get( 0 ) );

            for( int idx = 1; idx < list.size(); idx++ ) {
                output.append( delimiter );

                // append tree and any leading comments
                append( list.get( idx ) );
            }

            return this;
        }

        //TODO determine if this is actually called anywhere and if not, make includeSpaceAfterDelimiter = true the default
        // this version of appendList will only insert the given delimiter if it exists in the original text
        public Builder appendList( List<Tree> list, TokenType delimiter ) {
            return appendList( list, delimiter, false );
        }

        // this version of appendList will only insert the given delimiter if it exists in the original text
        public Builder appendList( List<Tree> list, TokenType delimiter, boolean includeSpaceAfterDelimiter ) {
            if( list.isEmpty() ) {
                return this;
            }

            // append first tree and any leading comments
            append( list.get( 0 ) );

            for( int idx = 1; idx < list.size(); idx++ ) {
                // append delimiter and any leading comments
                append( delimiter );
                if( includeSpaceAfterDelimiter ) {
                    append(SPACE);
                }

                // append tree and any leading comments
                append( list.get( idx ) );
            }

            return this;
        }

        public Builder appendTypeList( List<TokenType> list, TokenType delimiterType ) {
            if( list.isEmpty() ) {
                return this;
            }

            // append first token and any leading comments
            append( list.get( 0 ) );

            for( int idx = 1; idx < list.size(); idx++ ) {
                if( delimiterType != TokenType.WHITESPACE ) {
                    // append delimiter and any leading comments
                    append( delimiterType );
                }
                output.append( SPACE );

                // append token and any leading comments
                append( list.get( idx ) );
            }

            return this;
        }

        public Builder appendComments( int stopExclusive ) {
            OptionalInt firstComment = input.findNext(currentPosInclusive, stopExclusive, COMMENT );
            if( firstComment.isPresent() ) {
                output.append( SPACE );

                // strip out everything except comments and newlines
                output.append( IntStream.range(currentPosInclusive, stopExclusive )
                        .mapToObj( input.tokens::get )
                        .filter( t -> TokenUtils.isComment( t ) || t.type == TokenType.NEWLINE )
                        .map( TextToken::toString )
                        .collect( Collectors.joining() )
                );
            }
            currentPosInclusive = stopExclusive;
            return this;
        }

        public Builder appendBracedBlock( Tree tree, String newline ) {
            int start = input.getFirstTokenIndex( tree );
            if( input.tokens.get( start ).type != TokenType.LEFT_BRACE ) {
                // tree is not surrounded by grouping symbols so just append the whole thing
                return append( tree );
            } else {
                int bodyStart = input.findNextByExclusion( (start + 1), WS_NEWLINE_OR_COMMENT ).getAsInt();
                appendComments( bodyStart );

                // check to see if there's any actual code in this block
                int end = input.getLastTokenIndex( tree );
                int rightBrace = input.findPrev( end, TokenType.RIGHT_BRACE ).getAsInt();
                if( bodyStart != rightBrace ) {
                    // trim leading whitespace and trailing whitespace and newlines
                    int bodyEnd = input.findPrevByExclusion( rightBrace, WS_OR_NEWLINE ).getAsInt();
                    append( input.stringifyTokens( bodyStart, (bodyEnd + 1) ) )
                        .append( newline )
                        .setCurrentPositionInclusive( bodyEnd + 1 );
                }

                return this;
            }
        }

        public Builder appendOpeningBrace( boolean cuddleBraces ) {
            append( cuddleBraces ? SPACE : input.newline )
                    .append( TokenType.LEFT_BRACE );

            // determine whether a trailing newline should be appended
            int child = input.findNextByExclusion(currentPosInclusive, WS_NEWLINE_OR_COMMENT ).getAsInt();
            if( !input.containsComments(currentPosInclusive, child ) ) {
                return append( input.newline );
            } else {
                // there's a comment between opening brace and child token so we'll get newline from comment
                return this;
            }

        }

    }

}
