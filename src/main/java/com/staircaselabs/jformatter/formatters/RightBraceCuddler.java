package com.staircaselabs.jformatter.formatters;

import java.util.Optional;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.FormatScanner;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.Replacement;
import com.staircaselabs.jformatter.core.ScanningFormatter;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.tools.javac.tree.JCTree;

public class RightBraceCuddler extends ScanningFormatter {

    public RightBraceCuddler() {
        super( new RightBraceCuddlerScanner() );
    }

    @Override
    public String format( String text ) throws FormatException {
        String textWithBraces = new BraceInserter().format( text );
        return super.format( textWithBraces );
    }

    private static class RightBraceCuddlerScanner extends FormatScanner {

        @Override
        public Void visitCatch( CatchTree node, Input input ) {
            int catchStartIdx = input.getFirstTokenIndex( (JCTree)node );
            int catchIdx = input.findNext( catchStartIdx, TokenType.CATCH ).getAsInt();
            int rightBraceIdx = input.findPrev( catchIdx, TokenType.BRACE_RIGHT ).getAsInt();
            int blockStartIdx = input.getFirstTokenIndex( (JCTree)node.getBlock() );
            int leftBraceIdx = input.findPrev( (blockStartIdx + 1), TokenType.BRACE_LEFT ).getAsInt();

            // cuddle catch statement to its parent's closing brace
            cuddleRightBrace( input, rightBraceIdx, catchIdx, leftBraceIdx ).ifPresent( this::addReplacement );

            // process statements within catch block's body
            return scan( node.getBlock(), input );
        }

        @Override
        public Void visitDoWhileLoop( DoWhileLoopTree node, Input input ) {
            // find index of last token within do-while body
            int bodyEndIdx = input.getLastTokenIndex( (JCTree)node.getStatement() );

            // find while-token
            int whileIdx = input.findNext( bodyEndIdx, TokenType.WHILE ).getAsInt();

            // find first do-block's closing brace
            int braceIdx = input.findPrev( whileIdx, TokenType.BRACE_RIGHT ).getAsInt();

            // find first non-whitespace token before closing brace
            int parentIdx = input.findPrevByExclusion( braceIdx, TokenType.WHITESPACE ).getAsInt();

            StringBuilder sb = new StringBuilder();

            if( input.containsComments( (braceIdx + 1), whileIdx ) ) {
                sb.append( input.stringifyTokens( (braceIdx + 1), whileIdx ) );
            } else if( input.tokens.get( parentIdx ).type != TokenType.NEWLINE ) {
                sb.append( input.newline );
            }

            sb.append( input.stringifyTokens( (parentIdx + 1), braceIdx ) );
            sb.append( "} " );

            // cuddle while statement to the do-block's closing brace
            createReplacement( input, (parentIdx + 1), (whileIdx - 1), sb ).ifPresent( this::addReplacement );

            // cuddle braces contained within loop's body
            return scan( node.getStatement(), input );
        }

        @Override
        public Void visitIf( IfTree node, Input input ) {
            if( node.getThenStatement().getKind() == Kind.BLOCK ) {
                // cuddle statements within then-block
                scan( (BlockTree)node.getThenStatement(), input );
            }

            int startIdx = input.getFirstTokenIndex( (JCTree)node );
            int elseIfIdx = input.findPrevByExclusion( startIdx, TokenType.WHITESPACE ).getAsInt();
            if( input.tokens.get( elseIfIdx ).type == TokenType.ELSE ) {
                // cuddle else-if statement to its parent's closing brace
                int rightBraceIdx = input.findPrev( elseIfIdx, TokenType.BRACE_RIGHT ).getAsInt();
                int condEndIdx = input.getLastTokenIndex( (JCTree)node.getCondition() );

                // scanner gets confused if there's not a space after condition, so check for either case
                int leftBraceIdx = input.tokens.get( condEndIdx ).type == TokenType.BRACE_LEFT
                        ? condEndIdx
                        : input.findNext( (condEndIdx + 1), TokenType.BRACE_LEFT ).getAsInt();

                cuddleRightBrace( input, rightBraceIdx, elseIfIdx, leftBraceIdx ).ifPresent( this::addReplacement );
            }

            StatementTree elseStatement = node.getElseStatement();
            if( elseStatement != null ) {
                if( elseStatement.getKind() == Kind.IF ) {
                    // call visitIf on everything after else-if statement
                    scan( elseStatement, input );
                } else {
                    // cuddle else statement to its parent's closing brace
                    int blockStartIdx = input.getFirstTokenIndex( (JCTree)elseStatement );
                    int elseIdx = input.findPrev( blockStartIdx, TokenType.ELSE ).getAsInt();
                    int rightBraceIdx = input.findPrev( elseIdx, TokenType.BRACE_RIGHT ).getAsInt();
                    int leftBraceIdx = input.findNext( elseIdx, TokenType.BRACE_LEFT ).getAsInt();
                    cuddleRightBrace( input, rightBraceIdx, elseIdx, leftBraceIdx ).ifPresent( this::addReplacement );

                    if( elseStatement.getKind() == Kind.BLOCK ) {
                        // cuddle statements within else-block
                        scan( (BlockTree)elseStatement, input );
                    }
                }
            }

            return null;
        }

        @Override
        public Void visitTry( TryTree node, Input input ) {
            // cuddle each catch statement to its parent's closing brace
            for( CatchTree catchTree : node.getCatches() ) {
                scan( catchTree, input );
            }

            if( node.getFinallyBlock() != null ) {
                int finallyStartIdx = input.getFirstTokenIndex( (JCTree)node.getFinallyBlock() );
                int finallyIdx = input.findPrev( finallyStartIdx, TokenType.FINALLY ).getAsInt();
                int rightBraceIdx = input.findPrev( finallyIdx, TokenType.BRACE_RIGHT ).getAsInt();
                int leftBraceIdx = input.findNext( finallyIdx, TokenType.BRACE_LEFT ).getAsInt();

                // cuddle finally statement to its parent's closing brace
                cuddleRightBrace( input, rightBraceIdx, finallyIdx, leftBraceIdx ).ifPresent( this::addReplacement );

                // process statements within finally block's body
                scan( node.getFinallyBlock(), input );
            }

            return null;
        }

        private Optional<Replacement> cuddleRightBrace( Input input, int rightBraceIdx, int childIdx, int leftBraceIdx ) {
            StringBuilder sb = new StringBuilder( "} " );
            sb.append( input.stringifyTokens( childIdx, (leftBraceIdx + 1) ) );

            // check for comments before the child token
            if( input.containsComments( (rightBraceIdx + 1), childIdx ) ) {
                // determine if a leading newline should be inserted
                int firstNonWSIdx = input.findNextByExclusion( (rightBraceIdx + 1), TokenType.WHITESPACE ).getAsInt();
                if( input.tokens.get( firstNonWSIdx ).type != TokenType.NEWLINE ) {
                    sb.append( input.newline );
                }

                // we'll exclude the final newline before the child token, but
                // move any whitespace after the newline in front of the first comment
                int lastNewlineIdx = input.findPrev( childIdx, TokenType.NEWLINE ).getAsInt();
                sb.append( input.stringifyTokens( (lastNewlineIdx + 1), childIdx ) );
                sb.append( input.stringifyTokens( (rightBraceIdx + 1), lastNewlineIdx ) );
            }

            int lastIdxToReplace;
            int firstNonWSIdx = input.findNextByExclusion( (leftBraceIdx + 1), TokenType.WHITESPACE ).getAsInt();
            if( input.tokens.get( firstNonWSIdx ).type != TokenType.NEWLINE ) {
                sb.append( input.newline );
                int nextNewlineIdx = input.findNext( firstNonWSIdx, TokenType.NEWLINE ).getAsInt();
                sb.append( input.stringifyTokens( (leftBraceIdx + 1), nextNewlineIdx ) );
                lastIdxToReplace = nextNewlineIdx - 1;
            } else {
                lastIdxToReplace = firstNonWSIdx - 1;
            }

            return createReplacement( input, rightBraceIdx, lastIdxToReplace, sb );
        }

        private Optional<Replacement> createReplacement( Input input, int startIdx, int endIdx, StringBuilder sb ) {
            String oldText = input.stringifyTokens( startIdx, (endIdx + 1) );
            String replacementText = sb.toString();

            if( !replacementText.equals( oldText ) ) {
                TextToken firstTokenToReplace = input.tokens.get( startIdx );
                TextToken lastTokenToReplace = input.tokens.get( endIdx );
                return Optional.of(
                      new Replacement(
                              firstTokenToReplace.start,
                              lastTokenToReplace.end,
                              sb.toString()
                      )
                );
            } else {
                return Optional.empty();
            }
        }

    }

}
