package com.staircaselabs.jformatter.formatters;

import static com.staircaselabs.jformatter.core.TokenUtils.containsComments;
import static com.staircaselabs.jformatter.core.TokenUtils.findNextIndexByType;
import static com.staircaselabs.jformatter.core.TokenUtils.findNextIndexByTypeExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.findPrevIndexByTypeExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.isSingleWhitespace;
import static com.staircaselabs.jformatter.core.TokenUtils.stringifyTokens;

import java.util.Optional;
import java.util.OptionalInt;

import com.staircaselabs.jformatter.core.FormatScanner;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.Replacement;
import com.staircaselabs.jformatter.core.ScanningFormatter;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.tools.javac.tree.JCTree;

public class LeftBraceCuddler extends ScanningFormatter {

    public LeftBraceCuddler() {
        super( new LeftBraceCuddlerScanner() );
    }

    private static class LeftBraceCuddlerScanner extends FormatScanner {

        private static final TokenType[] WS_OR_COMMENT = {
                TokenType.WHITESPACE,
                TokenType.COMMENT_BLOCK,
                TokenType.COMMENT_LINE
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
            if( node.isStatic() ) {
                cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            }

            // cuddle braces contained in block statements
            for( StatementTree statement : node.getStatements() ) {
                scan( statement, input );
            }

            return null;
        }

        @Override
        public Void visitCase( CaseTree node, Input input ) {
            // cuddle opening brace of case statement
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );

            // cuddle braces contained in case statements
            for( StatementTree statement : node.getStatements() ) {
                scan( statement, input );
            }

            return null;
        }

        @Override
        public Void visitCatch( CatchTree node, Input input ) {
            return cuddleBlockContainer( input, (JCTree)node, node.getBlock() );
        }

        @Override
        public Void visitClass( ClassTree node, Input input ) {
            // cuddle opening brace of class
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );

            // cuddle braces contained in class methods
            for( Tree member : node.getMembers() ) {
                //TODO should we just scan all members?
                if(  member.getKind() == Kind.CLASS
                        || member.getKind() == Kind.BLOCK
                        || member.getKind() == Kind.METHOD ) {
                    scan( member, input );
                }
            }

            return null;
        }

        @Override
        public Void visitDoWhileLoop( DoWhileLoopTree node, Input input ) {
            return cuddleLoopBrace( input, (JCTree)node, node.getStatement() );
        }

        @Override
        public Void visitEnhancedForLoop( EnhancedForLoopTree node, Input input ) {
            return cuddleLoopBrace( input, (JCTree)node, node.getStatement() );
        }

        @Override
        public Void visitForLoop( ForLoopTree node, Input input ) {
            return cuddleLoopBrace( input, (JCTree)node, node.getStatement() );
        }

        @Override
        public Void visitIf( IfTree node, Input input ) {
            // cuddle opening brace of then statement
            StatementTree thenStatement = node.getThenStatement();
            cuddleLeftBrace( input, (JCTree)thenStatement ).ifPresent( this::addReplacement );

            StatementTree elseStatement = node.getElseStatement();
            if( elseStatement != null ) {
                if( elseStatement.getKind() == Kind.BLOCK ) {
                    // cuddle opening brace of else statement
                    cuddleLeftBrace( input, (JCTree)elseStatement ).ifPresent( this::addReplacement );
                } else {
                    // cuddle braces of remaining conditional statements
                    scan( node.getElseStatement(), input );
                }
            }

            return null;
        }

        @Override
        public Void visitLambdaExpression( LambdaExpressionTree node, Input input ) {
            // cuddle opening brace of lambda
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );

            // cuddle braces contained within lambda body
            scan( node.getBody(), input );

            return null;
        }

        @Override
        public Void visitMethod( MethodTree node, Input input ) {
            // cuddle opening brace of method
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );

            // cuddle braces contained within method body
            scan( node.getBody(), input );

            return null;
        }

        @Override
        public Void visitSwitch( SwitchTree node, Input input ) {
            // cuddle opening brace of case statement
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );

            // cuddle braces contained in case statements
            for( CaseTree statement : node.getCases() ) {
                scan( statement, input );
            }

            return null;
        }

        @Override
        public Void visitSynchronized( SynchronizedTree node, Input input ) {
            System.out.println( "BFC synchronized:\n");
            System.out.println( node.toString() );
            return cuddleBlockContainer( input, (JCTree)node, node.getBlock() );
        }

        @Override
        public Void visitTry( TryTree node, Input input ) {
            cuddleBlockContainer( input, (JCTree)node, node.getBlock() );

            // cuddle braces contained in each catch block
            for( CatchTree catchTree : node.getCatches() ) {
                scan( catchTree, input );
            }

            // cuddle braces contained in finally block
            if( node.getFinallyBlock() != null ) {
                scan( node.getFinallyBlock(), input );
            }

            return null;
        }

        @Override
        public Void visitWhileLoop( WhileLoopTree node, Input input ) {
            return cuddleLoopBrace( input, (JCTree)node, node.getStatement() );
        }

        private Void cuddleLoopBrace( Input input, JCTree tree, StatementTree loopBody ) {
            // cuddle opening brace of for loop
            cuddleLeftBrace( input, tree ).ifPresent( this::addReplacement );

            // cuddle braces contained within loop's body
            scan( loopBody, input );

            return null;
        }

        private Void cuddleBlockContainer( Input input, JCTree tree, BlockTree block ) {
            // cuddle opening brace of block container
            cuddleLeftBrace( input, tree ).ifPresent( this::addReplacement );

            // cuddle braces contained within block
            scan( block, input );

            return null;
        }

        private Optional<Replacement> cuddleLeftBrace( Input input, JCTree tree ) {
            // find indices of tokens that correspond to first and last characters in tree
            int startIdx = input.getFirstTokenIndex( tree );
            int endIdx = input.getLastTokenIndex( tree );

            // find first opening curly brace
            OptionalInt optionalBraceIdx = findNextIndexByType( input.tokens, startIdx, (endIdx + 1), TokenType.BRACE_LEFT );
            if( !optionalBraceIdx.isPresent() ) {
                // this tree doesn't contain an opening brace so there's nothing to do
                return Optional.empty();
            }
            int braceIdx = optionalBraceIdx.getAsInt();

            // find first non-whitespace, non-newline, non-comment token before opening brace
            //TODO make these throw FormatException with line/column info
            int parentStatement = findPrevIndexByTypeExclusion( input.tokens, braceIdx, WS_NEWLINE_OR_COMMENT )
                    .orElseThrow( () -> new RuntimeException(
                            "Missing parent statement: " + tree.getKind().toString() ) );

            // find the next non-whitespace, non-comment token following the opening brace
            int trailingCodeOrNewline = findNextIndexByTypeExclusion( input.tokens, (braceIdx + 1), WS_OR_COMMENT )
                    .orElseThrow( () -> new RuntimeException(
                            "Opening brace not closed: " + tree.getKind().toString() ) );

            // we expect to have exactly one whitespace token between end of parent statement and opening brace
            boolean removeLeadingText = !isSingleWhitespace( input.tokens, (parentStatement + 1), braceIdx );

            // we expect a trailing newline before any actual code statements
            TextToken trailingToken = input.tokens.get( trailingCodeOrNewline );
            boolean isMissingTrailingNewline = trailingToken.type != TokenType.NEWLINE;

            if( removeLeadingText || isMissingTrailingNewline ) {
                StringBuilder sb = new StringBuilder();
                sb.append( " {" ); // insert a single space before the left brace
                if( containsComments( input.tokens, (parentStatement + 1), braceIdx ) ) {
                    // comments appear between parentStatement and brace, so move them below brace
                    sb.append( input.newline );
                    sb.append( stringifyTokens( input.tokens, (parentStatement + 1), braceIdx ) );
                }

                // append any trailing comments or whitespace before a newline
                sb.append( stringifyTokens( input.tokens, (braceIdx + 1), trailingCodeOrNewline ) );
                sb.append( input.newline );

                TextToken firstTokenToReplace = input.tokens.get( parentStatement + 1 );
                TextToken lastTokenToReplace = isMissingTrailingNewline
                    ? input.tokens.get( trailingCodeOrNewline - 1 )
                    : input.tokens.get( trailingCodeOrNewline );
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
