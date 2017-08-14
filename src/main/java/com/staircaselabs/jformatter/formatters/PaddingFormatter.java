package com.staircaselabs.jformatter.formatters;

import static com.staircaselabs.jformatter.core.TokenUtils.stringifyTokens;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import com.staircaselabs.jformatter.core.FormatScanner;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.Replacement;
import com.staircaselabs.jformatter.core.ScanningFormatter;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.tools.javac.tree.JCTree;

//TODO handle type parameter padding
public class PaddingFormatter extends ScanningFormatter {

    public PaddingFormatter( int numPaddingSpaces ) {
        super( new PaddingFormatterScanner( numPaddingSpaces ) );
    }

    private static class PaddingFormatterScanner extends FormatScanner {

        private final String padding;

        public PaddingFormatterScanner( int numPaddingSpaces ) {
            padding = String.join( "", Collections.nCopies( numPaddingSpaces, " " ) );
        }

        private static final TokenType[] WS_OR_NEWLINE = {
                TokenType.WHITESPACE,
                TokenType.NEWLINE
        };

        @Override
        public Void visitCatch( CatchTree node, Input input ) {
            padTree( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitCatch( node, input );
        }

        @Override
        public Void visitDoWhileLoop( DoWhileLoopTree node, Input input ) {
            padTree( input, (JCTree)node.getCondition() ).ifPresent( this::addReplacement );
            return super.visitDoWhileLoop( node, input );
        }

        @Override
        public Void visitEnhancedForLoop( EnhancedForLoopTree node, Input input ) {
            // find index of opening paren
            int variableStartIdx = input.getFirstTokenIndex( (JCTree)node.getVariable() );
            int leftParenIdx = input.findPrev( variableStartIdx, TokenType.LEFT_PAREN ).getAsInt();

            // find index of closing paren
            int expressionEndIdx = input.getLastTokenIndex( (JCTree)node.getExpression() );
            int rightParenIdx = input.findNext( expressionEndIdx, TokenType.RIGHT_PAREN ).getAsInt();

            insertPadding( input, leftParenIdx, rightParenIdx ).ifPresent( this::addReplacement );

            return super.visitEnhancedForLoop( node, input );
        }

        @Override
        public Void visitForLoop(ForLoopTree node, Input input ) {
            // find index of opening paren
            List<? extends StatementTree> initializers = node.getInitializer();
            int initializersStartIdx = input.getFirstTokenIndex( (JCTree)initializers.get(0 ) );
            int leftParenIdx = input.findPrev( initializersStartIdx, TokenType.LEFT_PAREN ).getAsInt();

            // find index of closing paren
            List<? extends ExpressionStatementTree> updaters = node.getUpdate();
            int expressionEndIdx = input.getLastTokenIndex( (JCTree)updaters.get( updaters.size() - 1 ) );
            int rightParenIdx = input.findNext( expressionEndIdx, TokenType.RIGHT_PAREN ).getAsInt();

            insertPadding( input, leftParenIdx, rightParenIdx ).ifPresent( this::addReplacement );

            return super.visitForLoop( node, input );
        }

        @Override
        public Void visitIf(IfTree node, Input input ) {
            padTree( input, (JCTree)node.getCondition() ).ifPresent( this::addReplacement );
            return super.visitIf( node, input );
        }

        @Override
        public Void visitMethod( MethodTree node, Input input ) {
            // find index of opening paren
            int startIdx = input.getFirstTokenIndex( (JCTree)node );
            int leftParenIdx = input.findNext( startIdx, TokenType.LEFT_PAREN ).getAsInt();

            // find index of closing paren
            int rightParenIdx = input.findNext( (leftParenIdx + 1), TokenType.RIGHT_PAREN ).getAsInt();

            insertPadding( input, leftParenIdx, rightParenIdx ).ifPresent( this::addReplacement );

            return super.visitMethod( node, input );
        }

        @Override
        public Void visitMethodInvocation( MethodInvocationTree node, Input input ) {
            padTree( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitMethodInvocation( node, input );
        }

        @Override
        public Void visitNewArray(NewArrayTree node, Input input ) {
            // find indices of tokens that correspond to first and last characters in tree
            int startIdx = input.getFirstTokenIndex( (JCTree)node );
            int endIdx = input.getLastTokenIndex( (JCTree)node );

            TokenType leftBoundType = node.getInitializers() != null
                    ? TokenType.LEFT_BRACE
                    : TokenType.LEFT_BRACKET;
            TokenType rightBoundType = node.getInitializers() != null
                    ? TokenType.RIGHT_BRACE
                    : TokenType.RIGHT_BRACKET;

            if( node.getInitializers() != null ) {
                // find indices of boundary tokens
                // NOTE: endIdx is the token immediately after the right-boundary-char
                int leftBoundIdx = input.findNext( startIdx, leftBoundType ).getAsInt();
                int rightBoundIdx = input.findPrev( endIdx, rightBoundType ).getAsInt();
                insertPadding( input, leftBoundIdx, rightBoundIdx ).ifPresent( this::addReplacement );
            } else {
                int leftBoundIdx = startIdx - 1;
                int rightBoundIdx = startIdx - 1;
                for( int dim = 0; dim < node.getDimensions().size(); dim++ ) {
                    leftBoundIdx = input.findNext( (leftBoundIdx + 1), leftBoundType ).getAsInt();
                    rightBoundIdx = input.findNext( (rightBoundIdx + 1), rightBoundType ).getAsInt();
                    insertPadding( input, leftBoundIdx, rightBoundIdx ).ifPresent( this::addReplacement );
                }
            }

            return super.visitNewArray( node, input );
        }

        @Override
        public Void visitNewClass( NewClassTree node, Input input ) {
            padTree( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitNewClass( node, input );
        }

        @Override
        public Void visitSwitch( SwitchTree node, Input input ) {
            // find index of opening paren
            int startIdx = input.getFirstTokenIndex( (JCTree)node );
            int leftParenIdx = input.findNext( startIdx, TokenType.LEFT_PAREN ).getAsInt();

            // find index of closing paren
            int expressionEndIdx = input.getLastTokenIndex( (JCTree)node.getExpression() );
            int rightParenIdx = input.findPrev( expressionEndIdx, TokenType.RIGHT_PAREN ).getAsInt();

            insertPadding( input, leftParenIdx, rightParenIdx ).ifPresent( this::addReplacement );

            return super.visitSwitch( node, input );
        }

        @Override
        public Void visitTry( TryTree node, Input input ) {
            List<? extends Tree> resources = node.getResources();
            if( resources != null && !resources.isEmpty() ) {
                // find index of opening paren
                int startIdx = input.getFirstTokenIndex( (JCTree)resources.get( 0 ) );
                int leftParenIdx = input.findPrev( startIdx, TokenType.LEFT_PAREN ).getAsInt();

                // find index of closing paren
                int endIdx = input.getLastTokenIndex( (JCTree)resources.get( (resources.size() - 1) ) );
                int rightParenIdx = input.findPrev( (endIdx + 1), TokenType.RIGHT_PAREN ).getAsInt();

                insertPadding( input, leftParenIdx, rightParenIdx ).ifPresent( this::addReplacement );
            }
            return super.visitTry( node, input );
        }

        @Override
        public Void visitWhileLoop( WhileLoopTree node, Input input ) {
            padTree( input, (JCTree)node.getCondition() ).ifPresent( this::addReplacement );
            return super.visitWhileLoop( node, input );
        }

        private Optional<Replacement> padTree( Input input, JCTree tree ) {
            // find index of opening paren
            int startIdx = input.getFirstTokenIndex( tree );
            int leftParenIdx = input.findNext( startIdx, TokenType.LEFT_PAREN ).getAsInt();

            // find index of closing paren
            int endIdx = input.getLastTokenIndex( tree );
            int rightParenIdx = input.findPrev( endIdx, TokenType.RIGHT_PAREN ).getAsInt();

            return insertPadding( input, leftParenIdx, rightParenIdx );
        }

        private Optional<Replacement> insertPadding( Input input, int leftBoundIdx, int rightBoundIdx ) {
            String leftBoundChar = input.tokens.get( leftBoundIdx ).getText();
            String rightBoundChar = input.tokens.get( rightBoundIdx ).getText();

            // find first code char after left-boundary-char and before right-boundary-char
            OptionalInt firstArg = input.findNextByExclusion( (leftBoundIdx + 1), rightBoundIdx, WS_OR_NEWLINE );
            OptionalInt lastArg = input.findPrevByExclusion( (leftBoundIdx + 1), rightBoundIdx, WS_OR_NEWLINE );

            StringBuilder sb = new StringBuilder();
            if( !firstArg.isPresent() || !lastArg.isPresent() ) {
                // no args found, so ensure that no spaces exist between bounds
                sb.append( leftBoundChar );
                sb.append( rightBoundChar );
            } else {
                int firstArgIdx = firstArg.getAsInt();
                int lastArgIdx = lastArg.getAsInt();

                // check code following left-boundary-char
                if( input.contains( (leftBoundIdx + 1), firstArgIdx, TokenType.NEWLINE ) ) {
                    // there's a newline so all bets are off, just leave it as-is
                    sb.append( input.stringifyTokens( leftBoundIdx, lastArgIdx ) );
                } else {
                    // no newline so go ahead and ensure that left-boundary-char is appropriately padded
                    sb.append( leftBoundChar );
                    sb.append( padding );
                    sb.append( input.stringifyTokens( firstArgIdx, lastArgIdx ) );
                }

                // check code preceeding right-boundary-char
                if( input.contains( lastArgIdx, rightBoundIdx, TokenType.NEWLINE ) ) {
                    // there's a newline so all bets are off, just leave it as-is
                    sb.append( input.stringifyTokens( lastArgIdx, (rightBoundIdx + 1) ) );
                } else {
                    // no newline so go ahead and ensure that right-boundary-char is appropriately padded
                    sb.append( input.stringifyTokens( lastArgIdx, (lastArgIdx + 1) ) );
                    sb.append( padding );
                    sb.append( rightBoundChar );
                }
            }

            return createReplacement( input, leftBoundIdx, (rightBoundIdx + 1), sb );
        }

    }

}
