package com.staircaselabs.jformatter.formatters;

import static com.staircaselabs.jformatter.core.CompilationUnitUtils.getCompilationUnit;
import static com.staircaselabs.jformatter.core.TokenUtils.containsComments;
import static com.staircaselabs.jformatter.core.TokenUtils.findNextIndexByType;
import static com.staircaselabs.jformatter.core.TokenUtils.findNextIndexByTypeExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.findPrevIndexByTypeExclusion;
import static com.staircaselabs.jformatter.core.TokenUtils.getLinebreak;
import static com.staircaselabs.jformatter.core.TokenUtils.isSingleWhitespace;
import static com.staircaselabs.jformatter.core.TokenUtils.stringifyTokens;
import static com.staircaselabs.jformatter.core.TokenUtils.tokenizeText;

import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeSet;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.Replacement;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.staircaselabs.jformatter.core.TokenUtils;
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
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

public class LeftBraceCuddler {

    private static final TokenType[] WS_OR_NEWLINE = {
            TokenType.WHITESPACE,
            TokenType.NEWLINE
    };

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

    public static String format( String text ) throws FormatException {
        List<TextToken> tokens = tokenizeText( text );
        JCCompilationUnit unit = getCompilationUnit( text );
        Input input = new Input( tokens, unit.endPositions );

        String newline = getLinebreak( tokens );
        CuddleLeftBracesScanner scanner = new CuddleLeftBracesScanner( newline );
        try {
            NavigableSet<Replacement> replacements = scanner.scan( unit, input );

            // apply replacements in reverse order to maintain character position integrity
            StringBuilder sb = new StringBuilder( text );
            for( Replacement replacement : replacements.descendingSet() ) {
                replacement.apply( sb );
            }
            return sb.toString();
        } catch( Throwable throwable ) {
            //TODO include stacktrace or diagnostic info?
            throwable.printStackTrace();
            throw new FormatException( throwable.getMessage() );
        }
    }

    private static class CuddleLeftBracesScanner extends TreeScanner<NavigableSet<Replacement>, Input> {
        private String newline;

        public CuddleLeftBracesScanner( String newline ) {
            this.newline = newline;
        }

        @Override
        public NavigableSet<Replacement> visitBlock( BlockTree node, Input input ) {
            NavigableSet<Replacement> replacements =
                new TreeSet<>( Comparator.comparingInt( Replacement::getStart ) );

            if( node.isStatic() ) {
                cuddleLeftBrace( input, (JCTree)node, node.getKind().toString() ).ifPresent( replacements::add );
            }

            // cuddle braces contained in block statements
            for( StatementTree statement : node.getStatements() ) {
                Optional.ofNullable( scan( statement, input ) ).ifPresent( replacements::addAll );
            }

            return replacements;
        }

        @Override
        public NavigableSet<Replacement> visitCase( CaseTree node, Input input ) {
            NavigableSet<Replacement> replacements =
                new TreeSet<>( Comparator.comparingInt( Replacement::getStart ) );

            // cuddle opening brace of case statement
            cuddleLeftBrace( input, (JCTree)node, node.getKind().toString() ).ifPresent( replacements::add );

            // cuddle braces contained in case statements
            for( StatementTree statement : node.getStatements() ) {
                Optional.ofNullable( scan( statement, input ) ).ifPresent( replacements::addAll );
            }

            return replacements;
        }

        @Override
        public NavigableSet<Replacement> visitCatch( CatchTree node, Input input ) {
            return cuddleBlockContainer( input, (JCTree)node, node.getBlock() );
        }

        @Override
        public NavigableSet<Replacement> visitClass( ClassTree node, Input input ) {
            NavigableSet<Replacement> replacements =
                new TreeSet<>( Comparator.comparingInt( Replacement::getStart ) );

            // cuddle opening brace of class
            cuddleLeftBrace( input, (JCTree)node, node.getSimpleName().toString() ).ifPresent( replacements::add );

            // cuddle braces contained in class methods
            for( Tree member : node.getMembers() ) {
                //TODO should we just scan all members?
                if(  member.getKind() == Kind.CLASS || member.getKind() == Kind.BLOCK || member.getKind() == Kind.METHOD ) {
                        Optional.ofNullable( scan( member, input ) ).ifPresent( replacements::addAll );
                }
            }

            return replacements;
        }

        @Override
        public NavigableSet<Replacement> visitDoWhileLoop( DoWhileLoopTree node, Input input ) {
            return cuddleLoopBrace( input, (JCTree)node, node.getStatement() );
        }

        @Override
        public NavigableSet<Replacement> visitEnhancedForLoop( EnhancedForLoopTree node, Input input ) {
            return cuddleLoopBrace( input, (JCTree)node, node.getStatement() );
        }

        @Override
        public NavigableSet<Replacement> visitForLoop( ForLoopTree node, Input input ) {
            return cuddleLoopBrace( input, (JCTree)node, node.getStatement() );
        }

        @Override
        public NavigableSet<Replacement> visitIf( IfTree node, Input input ) {
            NavigableSet<Replacement> replacements =
                new TreeSet<>( Comparator.comparingInt( Replacement::getStart ) );

            // cuddle opening brace of then statement
            StatementTree thenStatement = node.getThenStatement();
            cuddleLeftBrace( input, (JCTree)thenStatement, thenStatement.getKind().toString() ).ifPresent( replacements::add );

            StatementTree elseStatement = node.getElseStatement();
            if( elseStatement != null ) {
                if( elseStatement.getKind() == Kind.BLOCK ) {
                    // cuddle opening brace of else statement
                    cuddleLeftBrace( input, (JCTree)elseStatement, elseStatement.getKind().toString() ).ifPresent( replacements::add );
                } else {
                    // cuddle braces of remaining conditional statements
                    Optional.ofNullable( scan( node.getElseStatement(), input ) ).ifPresent( replacements::addAll );
                }
            }

            return replacements;
        }

        @Override
        public NavigableSet<Replacement> visitLambdaExpression( LambdaExpressionTree node, Input input ) {
            NavigableSet<Replacement> replacements =
                new TreeSet<>( Comparator.comparingInt( Replacement::getStart ) );

            // cuddle opening brace of lambda
            cuddleLeftBrace( input, (JCTree)node, node.getKind().toString() ).ifPresent( replacements::add );

            // cuddle braces contained within lambda body
            Optional.ofNullable( scan( node.getBody(), input ) ).ifPresent( replacements::addAll );

            return replacements;
        }

        @Override
        public NavigableSet<Replacement> visitMethod( MethodTree node, Input input ){
            NavigableSet<Replacement> replacements =
                new TreeSet<>( Comparator.comparingInt( Replacement::getStart ) );

            // cuddle opening brace of method
            cuddleLeftBrace( input, (JCTree)node, node.getName().toString() ).ifPresent( replacements::add );

            // cuddle braces contained within method body
            Optional.ofNullable( scan( node.getBody(), input ) ).ifPresent( replacements::addAll );

            return replacements;
        }

        @Override
        public NavigableSet<Replacement> visitSwitch( SwitchTree node, Input input ) {
            NavigableSet<Replacement> replacements =
                new TreeSet<>( Comparator.comparingInt( Replacement::getStart ) );

            // cuddle opening brace of case statement
            cuddleLeftBrace( input, (JCTree)node, node.getKind().toString() ).ifPresent( replacements::add );

            // cuddle braces contained in case statements
            for( CaseTree statement : node.getCases() ) {
            Optional.ofNullable( scan( statement, input ) ).ifPresent( replacements::addAll );
            }

            return replacements;
        }

        @Override
        public NavigableSet<Replacement> visitSynchronized( SynchronizedTree node, Input input ) {
            return cuddleBlockContainer( input, (JCTree)node, node.getBlock() );
        }

        @Override
        public NavigableSet<Replacement> visitTry( TryTree node, Input input ) {
            NavigableSet<Replacement> replacements = cuddleBlockContainer( input, (JCTree)node, node.getBlock() );

            // cuddle braces contained in each catch block
            for( CatchTree catchTree : node.getCatches() ) {
                Optional.ofNullable( scan( catchTree, input ) ).ifPresent( replacements::addAll );
            }

            // cuddle braces contained in finally block
            if( node.getFinallyBlock() != null ) {
                Optional.ofNullable( scan( node.getFinallyBlock(), input ) ).ifPresent( replacements::addAll );
            }

            return replacements;
        }

        @Override
        public NavigableSet<Replacement> visitWhileLoop( WhileLoopTree node, Input input ) {
            return cuddleLoopBrace( input, (JCTree)node, node.getStatement() );
        }

        private NavigableSet<Replacement> cuddleLoopBrace( Input input, JCTree tree, StatementTree loopBody ) {
            NavigableSet<Replacement> replacements =
                new TreeSet<>( Comparator.comparingInt( Replacement::getStart ) );

            // cuddle opening brace of for loop
            cuddleLeftBrace( input, tree, tree.getKind().toString() ).ifPresent( replacements::add );

            // cuddle braces contained within loop's body
            Optional.ofNullable( scan( loopBody, input ) ).ifPresent( replacements::addAll );

            return replacements;
        }

        private NavigableSet<Replacement> cuddleBlockContainer( Input input, JCTree tree, BlockTree block ) {
            NavigableSet<Replacement> replacements =
                new TreeSet<>( Comparator.comparingInt( Replacement::getStart ) );

            // cuddle opening brace of block container
            cuddleLeftBrace( input, tree, tree.getKind().toString() ).ifPresent( replacements::add );

            // cuddle braces contained within block
            Optional.ofNullable( scan( block, input ) ).ifPresent( replacements::addAll );

            return replacements;
        }

        private Optional<Replacement> cuddleLeftBrace( Input input, JCTree tree, String id ) {
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
            int parentStatement = findPrevIndexByTypeExclusion( input.tokens, braceIdx, WS_NEWLINE_OR_COMMENT )
                    .orElseThrow( () -> new RuntimeException(
                            "Missing parent statement: " + id ) );

            // find the next non-whitespace, non-comment token following the opening brace
            int trailingCodeOrNewline = findNextIndexByTypeExclusion( input.tokens, (braceIdx + 1), WS_OR_COMMENT )
                    .orElseThrow( () -> new RuntimeException(
                            "Opening brace not closed: " + id ) );

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
                    sb.append( newline );
                    sb.append( stringifyTokens( input.tokens, (parentStatement + 1), braceIdx ) );
                }

                // append any trailing comments or whitespace before a newline
                sb.append( stringifyTokens( input.tokens, (braceIdx + 1), trailingCodeOrNewline ) );
                sb.append( newline );

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
