package com.staircaselabs.jformatter.formatters;

import static com.staircaselabs.jformatter.core.TokenUtils.findNextIndexByType;
import static com.staircaselabs.jformatter.core.TokenUtils.findPrevIndexByTypeExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.isComment;
import static com.staircaselabs.jformatter.core.TokenUtils.stringifyTokens;

import java.util.Optional;

import com.staircaselabs.jformatter.core.FormatScanner;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.Replacement;
import com.staircaselabs.jformatter.core.ScanningFormatter;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.JCTree;

public class BraceInserter extends ScanningFormatter {

    public BraceInserter() {
        super( new BraceInserterScanner() );
    }

    private static class BraceInserterScanner extends FormatScanner {

        private static final TokenType[] COMMENT_OR_NEWLINE = {
                TokenType.COMMENT_BLOCK,
                TokenType.COMMENT_JAVADOC,
                TokenType.COMMENT_LINE,
                TokenType.NEWLINE
        };

        private static final TokenType[] WS_NEWLINE_OR_COMMENT = {
                TokenType.WHITESPACE,
                TokenType.COMMENT_BLOCK,
                TokenType.COMMENT_JAVADOC,
                TokenType.COMMENT_LINE,
                TokenType.NEWLINE
        };

        @Override
        public Void visitBlock( BlockTree node, Input input ) {
            // insert braces around statements within block body
            for( StatementTree statement : node.getStatements() ) {
                scan( statement, input );
            }

            return null;
        }

        @Override
        public Void visitDoWhileLoop( DoWhileLoopTree node, Input input ) {
            // insert braces around for loop
            surroundWithBraces( input, (JCTree)node.getStatement() ).ifPresent( this::addReplacement );

            // insert braces into blocks contained within loop's body
            return scan( node.getStatement(), input );
        }

        @Override
        public Void visitEnhancedForLoop( EnhancedForLoopTree node, Input input ) {
            // insert braces around for loop
            surroundWithBraces( input, (JCTree)node.getStatement() ).ifPresent( this::addReplacement );

            // insert braces into blocks contained within loop's body
            return scan( node.getStatement(), input );
        }

        @Override
        public Void visitForLoop( ForLoopTree node, Input input ) {
            // insert braces around for loop
            surroundWithBraces( input, (JCTree)node.getStatement() ).ifPresent( this::addReplacement );

            // insert braces into blocks contained within loop's body
            return scan( node.getStatement(), input );
        }

        @Override
        public Void visitIf( IfTree node, Input input ) {
            // ensure that then-statement is surrounded by curly braces
            surroundWithBraces( input, (JCTree)node.getThenStatement() ).ifPresent( this::addReplacement );

            StatementTree elseStatement = node.getElseStatement();
            if( elseStatement != null ) {
                if( elseStatement.getKind() == Kind.IF ) {
                    // there are more if-statements to process
                    scan( node.getElseStatement(), input );
                } else {
                    // this must be the final else-statement
                    surroundWithBraces( input, (JCTree)elseStatement ).ifPresent( this::addReplacement );
                }
            }

            return null;
        }

        @Override
        public Void visitSynchronized( SynchronizedTree node, Input input ) {
            surroundWithBraces( input, (JCTree)node.getBlock() ).ifPresent( this::addReplacement );
     
            return scan( node.getBlock(), input );
        }

        private Optional<Replacement> surroundWithBraces( Input input, JCTree tree ) {
            // get token indices corresponding to the bounds of the tree
            int treeStartIdx = input.getFirstTokenIndex( tree );
            int treeEndIdx = input.getLastTokenIndex( tree );

            // check if tree is surrounded by curly braces
            if( input.tokens.get( treeStartIdx ).type != TokenType.BRACE_LEFT ) {
                // find first non-whitespace, non-newline, non-comment token before tree
                int parentStatement = findPrevIndexByTypeExclusion( input.tokens, treeStartIdx, WS_NEWLINE_OR_COMMENT )
                        //TODO throw FormatException with line/column numbers?
                        .orElseThrow( () -> new RuntimeException(
                                "Missing parent statement: " + tree.getKind().toString() ) );

                // make sure we preserve any comments before the tree
                int firstToKeep =
                        findNextIndexByType( input.tokens, (parentStatement + 1), treeStartIdx, COMMENT_OR_NEWLINE )
                        .orElse( treeStartIdx );

                // make sure we preserve any inline comments following the first line of code in the tree
                int lastToKeep =
                        findNextIndexByType( input.tokens, treeStartIdx, TokenType.NEWLINE )
                        .orElse( treeEndIdx );

                // determine token range to be replaced
                TextToken firstTokenToReplace = input.tokens.get( parentStatement + 1 );
                TextToken lastTokenToReplace = input.tokens.get( lastToKeep );

                // build up replacement text
                StringBuilder sb = new StringBuilder();
                sb.append( " {" );
                if( isComment(input.tokens.get( firstToKeep ) ) ) {
                    sb.append( " " );
                }
                sb.append( stringifyTokens( input.tokens, firstToKeep, (lastToKeep + 1) ) );
                sb.append( "}" );
                sb.append( input.newline );

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
