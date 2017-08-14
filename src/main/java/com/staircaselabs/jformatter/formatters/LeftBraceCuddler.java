package com.staircaselabs.jformatter.formatters;

import static com.staircaselabs.jformatter.core.TokenUtils.isComment;
import static com.staircaselabs.jformatter.core.TokenUtils.stringifyTokens;

import java.util.Optional;
import java.util.OptionalInt;

import com.staircaselabs.jformatter.core.FormatException;
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
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.tools.javac.tree.JCTree;

public class LeftBraceCuddler extends ScanningFormatter {

    public LeftBraceCuddler() {
        super( new LeftBraceCuddlerScanner() );
    }

    @Override
    public String format( String text ) throws FormatException {
        String textWithBraces = new BraceInserter().format( text );
        return super.format( textWithBraces );
    }

    private static class LeftBraceCuddlerScanner extends FormatScanner {

        private static final TokenType[] COMMENT = {
                TokenType.COMMENT_BLOCK,
                TokenType.COMMENT_LINE,
                TokenType.COMMENT_JAVADOC
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
            return super.visitBlock( node, input );
        }

        @Override
        public Void visitCase( CaseTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitCase( node, input );
        }

        @Override
        public Void visitCatch( CatchTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitCatch( node, input );
        }

        @Override
        public Void visitClass( ClassTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitClass( node, input );
        }

        @Override
        public Void visitDoWhileLoop( DoWhileLoopTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitDoWhileLoop( node, input );
        }

        @Override
        public Void visitEnhancedForLoop( EnhancedForLoopTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitEnhancedForLoop( node, input );
        }

        @Override
        public Void visitForLoop( ForLoopTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitForLoop( node, input );
        }

        @Override
        public Void visitIf( IfTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node.getThenStatement() ).ifPresent( this::addReplacement );

            StatementTree elseStatement = node.getElseStatement();
            if( elseStatement != null && elseStatement.getKind() != Kind.IF ) {
                // cuddle final else-statement brace
                cuddleLeftBrace( input, (JCTree)elseStatement ).ifPresent( this::addReplacement );
            }

            return super.visitIf( node, input );
        }

        @Override
        public Void visitLambdaExpression( LambdaExpressionTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitLambdaExpression( node, input );
        }

        @Override
        public Void visitMethod( MethodTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitMethod( node, input );
        }

        @Override
        public Void visitSwitch( SwitchTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitSwitch( node, input );
        }

        @Override
        public Void visitSynchronized( SynchronizedTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitSynchronized( node, input );
        }

        @Override
        public Void visitTry( TryTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );

            if( node.getFinallyBlock() != null ) {
                // cuddle finally block opening brace
                cuddleLeftBrace( input, (JCTree)node.getFinallyBlock() ).ifPresent( this::addReplacement );
            }

            return super.visitTry( node, input );
        }

        @Override
        public Void visitWhileLoop( WhileLoopTree node, Input input ) {
            cuddleLeftBrace( input, (JCTree)node ).ifPresent( this::addReplacement );
            return super.visitWhileLoop( node, input );
        }

        private Optional<Replacement> cuddleLeftBrace( Input input, JCTree tree ) {
            // find indices of tokens that correspond to first and last characters in tree
            int startIdx = input.getFirstTokenIndex( tree );
            int endIdx = input.getLastTokenIndex( tree );

            // find first opening curly brace
            OptionalInt optionalBraceIdx = input.findNext( startIdx, (endIdx + 1), TokenType.LEFT_BRACE );
            if( !optionalBraceIdx.isPresent() ) {
                // this tree doesn't contain an opening brace so there's nothing to do
                return Optional.empty();
            }
            int braceIdx = optionalBraceIdx.getAsInt();

            // find first non-whitespace, non-newline, non-comment token before opening brace
            //TODO make these throw FormatException with line/column info
            int parentIdx = input.findPrevByExclusion( braceIdx, WS_NEWLINE_OR_COMMENT )
                    .orElseThrow( () -> new RuntimeException(
                            "Missing parent statement: " + tree.getKind().toString() ) );

            StringBuilder sb = new StringBuilder( " {" );
            sb.append( input.newline );

            if( input.containsComments( (parentIdx + 1), braceIdx ) ) {
                int commentIdx = input.findNext( (parentIdx + 1), braceIdx, COMMENT ).getAsInt();
                int nextNewlineIdx =
                        input.findNext( (parentIdx + 1), braceIdx, TokenType.NEWLINE )
                        .getAsInt();

                // trim trailing whitespace
                int lastNonWhitespaceIdx =
                        input.findPrevByExclusion( braceIdx, TokenType.WHITESPACE ).getAsInt();

                if( nextNewlineIdx < commentIdx ) {
                    sb.append( stringifyTokens( input.tokens, (nextNewlineIdx + 1), (lastNonWhitespaceIdx + 1) ) );
                } else {
                    sb.append( stringifyTokens( input.tokens, (parentIdx + 1), (lastNonWhitespaceIdx + 1) ) );
                }
            }

            // find first non-whitespace token after brace
            int firstNonWS = input.findNextByExclusion( (braceIdx + 1), TokenType.WHITESPACE ).getAsInt();
            int lastToReplace;
            if( input.tokens.get( firstNonWS ).type == TokenType.NEWLINE ) {
                // skip leading newline since we already added one
                lastToReplace = firstNonWS;
            } else if( isComment( input.tokens.get( firstNonWS ) ) ) {
                // append up to and including next newline
                int nextNewline = input.findNext( (firstNonWS + 1), TokenType.NEWLINE ).getAsInt();
                sb.append( stringifyTokens( input.tokens, (braceIdx + 1), (nextNewline + 1) ) );
                lastToReplace = nextNewline;
            } else {
                // it's not a comment or a newline so it must be actual code
                lastToReplace = firstNonWS - 1;
            }

            return createReplacement( input, (parentIdx + 1), (lastToReplace + 1), sb );
        }

    }

}
