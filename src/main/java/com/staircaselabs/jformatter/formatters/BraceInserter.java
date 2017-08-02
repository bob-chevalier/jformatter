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
import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
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
//            if( node.isStatic() ) {
//                surroundWithBraces( input, (JCTree)node );
//            }

            // insert braces contained in block statements
            for( StatementTree statement : node.getStatements() ) {
                scan( statement, input );
            }

            return null;
        }

//        @Override
//        public Void visitCatch( CatchTree node, Input input ) {
//            return cuddleBlockContainer( input, (JCTree)node, node.getBlock() );
//        }

//        @Override
//        public Void visitClass( ClassTree node, Input input ) {
//            // cuddle opening brace of class
//            cuddleLeftBrace( input, (JCTree)node, node.getSimpleName().toString() ).ifPresent( replacements::add );
//
//            // cuddle braces contained in class methods
//            for( Tree member : node.getMembers() ) {
//                //TODO should we just scan all members?
//                if(  member.getKind() == Kind.CLASS
//                        || member.getKind() == Kind.BLOCK
//                        || member.getKind() == Kind.METHOD ) {
//                    scan( member, input );
//                }
//            }
//
//            return null;
//        }

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

//        @Override
//        public Void visitSwitch( SwitchTree node, Input input ) {
//            // cuddle opening brace of case statement
//            cuddleLeftBrace( input, (JCTree)node, node.getKind().toString() ).ifPresent( replacements::add );
//
//            // cuddle braces contained in case statements
//            for( CaseTree statement : node.getCases() ) {
//                scan( statement, input );
//            }
//
//            return null;
//        }

        @Override
        public Void visitSynchronized( SynchronizedTree node, Input input ) {
            surroundWithBraces( input, (JCTree)node.getBlock() ).ifPresent( this::addReplacement );
     
            return scan( node.getBlock(), input );
//            return null;
        }

//        @Override
//        public Void visitTry( TryTree node, Input input ) {
//            cuddleBlockContainer( input, (JCTree)node, node.getBlock() );
//
//            // cuddle braces contained in each catch block
//            for( CatchTree catchTree : node.getCatches() ) {
//                scan( catchTree, input );
//            }
//
//            // cuddle braces contained in finally block
//            if( node.getFinallyBlock() != null ) {
//                scan( node.getFinallyBlock(), input );
//            }
//
//            return null;
//        }

//        @Override
//        public Void visitWhileLoop( WhileLoopTree node, Input input ) {
//            return cuddleLoopBrace( input, (JCTree)node, node.getStatement() );
//        }

//        private Void insertLoopBraces( Input input, StatementTree loopBody ) {
//            // insert braces around for loop
//            surroundWithBraces( input, (JCTree)loopBody );
//
//            // insert braces into blocks contained within loop's body
//            return scan( loopBody, input );
//        }

//        private Void cuddleBlockContainer( Input input, JCTree tree, BlockTree block ) {
//            // cuddle opening brace of block container
//            cuddleLeftBrace( input, tree, tree.getKind().toString() ).ifPresent( replacements::add );
//
//            // cuddle braces contained within block
//            scan( block, input );
//
//            return null;
//        }

        private Optional<Replacement> surroundWithBraces( Input input, JCTree tree ) {
            // get token indices corresponding to the bounds of the tree
            int treeStartIdx = input.getFirstTokenIndex( tree );
            int treeEndIdx = input.getLastTokenIndex( tree );

            // check if tree is surrounded by curly braces
            if( input.tokens.get( treeStartIdx ).type != TokenType.BRACE_LEFT ) {
                System.out.println( "Here we go" );
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
//                if( lastTokenToReplace.type != TokenType.NEWLINE ) {
//                    sb.append( newline );
//                }
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
