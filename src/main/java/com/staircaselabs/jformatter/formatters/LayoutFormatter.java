package com.staircaselabs.jformatter.formatters;

import static com.staircaselabs.jformatter.core.TokenUtils.isComment;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.staircaselabs.jformatter.core.FormatScanner;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.Padding;
import com.staircaselabs.jformatter.core.Replacement;
import com.staircaselabs.jformatter.core.ScanningFormatter;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.staircaselabs.jformatter.core.TokenUtils;
import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
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
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LambdaExpressionTree.BodyKind;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
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
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.tools.javac.tree.JCTree;

public class LayoutFormatter extends ScanningFormatter {

    public LayoutFormatter( Padding padding, boolean cuddleBraces ) {
        super( new LayoutFormatterScanner( padding, cuddleBraces ) );
    }

//    public static enum IndentType { SPACES, TABS }

    private static class LayoutFormatterScanner extends FormatScanner {

        private static final boolean VERBOSE = false;
        private static final boolean ENABLED = true;

        private static final TokenType[] COMMENT_OR_NEWLINE = {
                TokenType.COMMENT_BLOCK,
                TokenType.COMMENT_JAVADOC,
                TokenType.COMMENT_LINE,
                TokenType.NEWLINE
        };
        private static final TokenType[] WS_OR_NEWLINE = {
                TokenType.WHITESPACE,
                TokenType.NEWLINE
        };
        private static final TokenType[] WS_NEWLINE_COLON_OR_LEFT_BRACE = {
                TokenType.WHITESPACE,
                TokenType.NEWLINE,
                TokenType.COLON,
                TokenType.LEFT_BRACE
        };
        private static final TokenType[] WS_NEWLINE_OR_COMMENT = {
                TokenType.WHITESPACE,
                TokenType.NEWLINE,
                TokenType.COMMENT_BLOCK,
                TokenType.COMMENT_JAVADOC,
                TokenType.COMMENT_LINE
        };
        private static final TokenType[] WS_NEWLINE_COMMENT_OR_BRACKET = {
                TokenType.WHITESPACE,
                TokenType.NEWLINE,
                TokenType.COMMENT_BLOCK,
                TokenType.COMMENT_JAVADOC,
                TokenType.COMMENT_LINE,
                TokenType.LEFT_BRACKET
        };
        private static final TokenType[] WS_NEWLINE_OR_RIGHT_BRACE = {
                TokenType.WHITESPACE,
                TokenType.NEWLINE,
                TokenType.RIGHT_BRACE
        };

        private static final String SPACE = " ";
//        private final IndentType indentType;
//        private final int numTabSpaces;
        private final Padding padding;
        private final boolean cuddleBraces;

        public LayoutFormatterScanner( Padding padding, boolean cuddleBraces ) {
            this.padding = padding;
            this.cuddleBraces = cuddleBraces;
        }

        @Override
        public Void visitAnnotatedType( AnnotatedTypeTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitAnnotatedType======" );
            if( node.getUnderlyingType().getKind() != Kind.ARRAY_TYPE ) {
                StringBuilder sb = new StringBuilder();
                for( AnnotationTree annotation : node.getAnnotations() ) {
                    sb.append( input.stringifyTree( (JCTree)annotation ) );
                    sb.append( SPACE );
                }
                sb.append( input.stringifyTree( (JCTree)node.getUnderlyingType() ) );

                createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            }

            return super.visitAnnotatedType( node, input );
        }

        @Override
        public Void visitAnnotation( AnnotationTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitAnnotation======" );
            StringBuilder sb = new StringBuilder( "@" );
            sb.append( input.stringifyTree( (JCTree)node.getAnnotationType() ) );

            List<? extends ExpressionTree> args = node.getArguments();
            if( !args.isEmpty() ) {
                sb.append( "(" );

                // add each annotation argument, delimited by a comma and single-space
                //TODO determine whether we actually need to trim the args below
                sb.append(
                        node.getArguments()
                                .stream()
                                .map( JCTree.class::cast )
                                .map( input::stringifyTreeAndTrim )
                                .collect( Collectors.joining( ", " ) )
                );
                sb.append( ")" );
            }

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitAnnotation( node, input );
        }

        @Override
        public Void visitArrayAccess( ArrayAccessTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitArrayAccess======" );
            if( node.getExpression().getKind() == Kind.IDENTIFIER ) {
                StringBuilder sb = new StringBuilder();
                sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
                sb.append( "[" );
                sb.append( input.stringifyTree( (JCTree)node.getIndex() ) );
                sb.append( "]" );

                int id = input.getFirstTokenIndex( (JCTree)node.getExpression() );
                int rightBracket = input.findNext( id, TokenType.RIGHT_BRACKET ).getAsInt();
                if( ENABLED ) createReplacement( input, id, (rightBracket + 1), sb ).ifPresent( this::addReplacement );
            } else {
                int index = input.getFirstTokenIndex( (JCTree)node.getIndex() );
                int leftBracket = input.findPrev( index, TokenType.LEFT_BRACKET ).getAsInt();
                int prevRightBracket = input.findPrev( leftBracket, TokenType.RIGHT_BRACKET ).getAsInt();

                StringBuilder sb = new StringBuilder( "][" );
                if( ENABLED ) createReplacement( input, prevRightBracket, (leftBracket + 1), sb ).ifPresent( this::addReplacement );
            }

            return super.visitArrayAccess( node, input );
        }

        @Override
        public Void visitArrayType( ArrayTypeTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitArrayType======" );
            int startIdx = input.getFirstTokenIndex( (JCTree)node );
            int endIdx = input.getLastTokenIndex( (JCTree)node );
            int pos = input.findNext( startIdx, WS_NEWLINE_COMMENT_OR_BRACKET ).getAsInt();

            StringBuilder sb = new StringBuilder();
            sb.append( input.stringifyTokens( startIdx, pos ) );

            OptionalInt leftBracket = input.findNext( startIdx, TokenType.LEFT_BRACKET );
            while( leftBracket.isPresent() ) {
                int leftBracketIdx = leftBracket.getAsInt();
                OptionalInt annotationStart = input.findNext( pos, leftBracketIdx, TokenType.AT );
                if( annotationStart.isPresent() ) {
                    OptionalInt annotationEnd =
                            input.findNext( annotationStart.getAsInt(), (leftBracketIdx + 1), WS_NEWLINE_COMMENT_OR_BRACKET );
                    sb.append( SPACE );
                    sb.append( input.stringifyTokens( annotationStart.getAsInt(), annotationEnd.getAsInt() ) );
                    sb.append( SPACE );
                }
                sb.append( "[]" );

                pos = input.findNext( leftBracketIdx, TokenType.RIGHT_BRACKET ).getAsInt() + 1;
                leftBracket = input.findNext( pos, endIdx, TokenType.LEFT_BRACKET );
            }

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitArrayType( node, input );
        }

//        @Override
//        public Void visitAssert( AssertTree node, Input input ) {
//            System.out.println( "======visitAssert======" );
//            return super.visitAssert( node, input );
//        }

        @Override
        public Void visitAssignment( AssignmentTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitAssignment======" );
            StringBuilder sb = new StringBuilder();
            sb.append( input.stringifyTree( (JCTree)node.getVariable() ) );
            sb.append( SPACE );
            sb.append( "=" );
            sb.append( SPACE );
            sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitAssignment( node, input );
        }

        @Override
        public Void visitBinary( BinaryTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitBinary======" );
            StringBuilder sb = new StringBuilder();
            sb.append( input.stringifyTree( (JCTree)node.getLeftOperand() ) );
            sb.append( SPACE );

            // find token that represents the operation
            int leftOperand = input.getLastTokenIndex( (JCTree)node.getLeftOperand() ) - 1;
            int operator = input.findNextByExclusion( (leftOperand + 1), WS_NEWLINE_OR_COMMENT ).getAsInt();
            sb.append( input.tokens.get( operator ).getText() );

            sb.append( SPACE );
            sb.append( input.stringifyTree( (JCTree)node.getRightOperand() ) );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitBinary( node, input );
        }

//        @Override
//        public Void visitBlock(BlockTree node, Input input){
//            System.out.println( "======visitBlock======" );
//            return super.visitBlock( node, input );
//        }

        @Override
        public Void visitBreak( BreakTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitBreak======" );
            StringBuilder sb = new StringBuilder( "break" );
            if( node.getLabel() != null ) {
                sb.append( SPACE );
                sb.append( node.getLabel() );
            }
            sb.append( ";" );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitBreak( node, input );
        }

        @Override
        public Void visitCase( CaseTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitCase======" );
            StringBuilder sb = new StringBuilder();
            sb.append( node.getExpression() == null ? "default" : "case" );
            if( node.getExpression() != null ) {
                sb.append( SPACE );
                sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
            }
            sb.append( ":" );
            sb.append( input.newline );

            int start = input.getFirstTokenIndex( (JCTree)node );
            TokenType keywordType = node.getExpression() == null ? TokenType.DEFAULT : TokenType.CASE;
            int keyword = input.findNext( start, keywordType ).getAsInt();
            int colon = input.findNext( keyword, TokenType.COLON ).getAsInt();
            int end = input.getLastTokenIndex( (JCTree)node );
            OptionalInt bodyStart = input.findNextByExclusion( (colon + 1), end, WS_NEWLINE_COLON_OR_LEFT_BRACE );
            if( bodyStart.isPresent() ) {
                int bodyEnd = input.findPrevByExclusion( end, WS_NEWLINE_OR_RIGHT_BRACE ).getAsInt();
                sb.append( input.stringifyTokens( bodyStart.getAsInt(), (bodyEnd + 1) ) );
            }

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitCase( node, input );
        }

        @Override
        public Void visitCatch( CatchTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitCatch======" );
            StringBuilder sb = new StringBuilder();
            openArgsList( "catch", sb );
            sb.append( padding.methodArg );
            sb.append( input.stringifyTree( (JCTree)node.getParameter() ) );
            sb.append( padding.methodArg );
            sb.append( ")" );
            appendOpeningBrace( input.newline, sb );

            // collect leading comments and place them below opening left brace
            int start = input.getFirstTokenIndex( (JCTree)node );
            int leftBrace = input.getFirstTokenIndex( (JCTree)node.getBlock() );
            sb.append( input.collectCommentsAndNewlines( (start + 1), leftBrace ) );

            sb.append( stripBraces( (JCTree)node.getBlock(), input ) );
            sb.append( input.newline );
            sb.append( "}" );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitCatch( node, input );
        }

        @Override
        public Void visitClass( ClassTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitClass======" );
            //TODO put variables and methods in alphabetical/static order (Do this in a separate formatter: one for members and one for methods?)
            //TODO handle parenthesized in PaddingFormatter
//            for( Tree member : node.getMembers() ) {
//                System.out.println( "member: " + member.getKind() + "\n" + member );
//            }
            return super.visitClass( node, input );
        }

        @Override
        public Void visitCompilationUnit( CompilationUnitTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitCompUnit======" );
//            return super.visitCompilationUnit( node, input );
            scan( node.getTypeDecls(), input );
            return null;
        }

        @Override
        public Void visitCompoundAssignment( CompoundAssignmentTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======CompoundAssignment======" );
            StringBuilder sb = new StringBuilder();
            sb.append( input.stringifyTree( (JCTree)node.getVariable() ) );
            sb.append( SPACE );
            sb.append( getOperator( node.getKind() ) );
            sb.append( SPACE );
            sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitCompoundAssignment( node, input );
        }

        @Override
        public Void visitConditionalExpression( ConditionalExpressionTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitConditionalExpression======" );
            StringBuilder sb = new StringBuilder();
            sb.append( input.stringifyTree( (JCTree)node.getCondition() ) );
            sb.append( SPACE );
            sb.append( "?" );
            sb.append( SPACE );
            sb.append( input.stringifyTree( (JCTree)node.getTrueExpression() ) );
            sb.append( SPACE );
            sb.append( ":" );
            sb.append( SPACE );
            sb.append( input.stringifyTree( (JCTree)node.getFalseExpression() ) );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitConditionalExpression( node, input );
        }

        @Override
        public Void visitContinue(ContinueTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitContinue======" );
            StringBuilder sb = new StringBuilder( "continue" );
            if( node.getLabel() != null ) {
                sb.append( SPACE );
                sb.append( node.getLabel() );
            }
            sb.append( ";" );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitContinue( node, input );
        }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitDoWhileLoop======" );
            StringBuilder sb = new StringBuilder();
            sb.append( "do" );
            appendOpeningBrace( input.newline, sb );

            // collect leading comments and place them below opening left brace
            int leftBrace = input.getFirstTokenIndex( (JCTree)node.getStatement() );
            int doStatement = input.findPrev( leftBrace, TokenType.DO ).getAsInt();
            sb.append( input.collectCommentsAndNewlines( (doStatement + 1), leftBrace ) );

            // add loop body, after stripping off surrounding braces
            sb.append( stripBraces( (JCTree)node.getStatement(), input ) );

            sb.append( input.newline );
            sb.append( "}" );
            sb.append( cuddleBraces ? SPACE : input.newline );
            openArgsList( "while", sb );

            // add while-condition, after stripping off surrounding parentheses
            sb.append( padding.methodArg );
            sb.append( stripParens( (JCTree)node.getCondition(), input ) );
            sb.append( padding.methodArg );

            sb.append( ")" );
            sb.append( ";" );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitDoWhileLoop( node, input );
        }

//        @Override
//        public Void visitEmptyStatement(EmptyStatementTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitEmptyStatement======" );
//            return super.visitEmptyStatement( node, input );
//        }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitEnhancedForLoop======" );
            // verify that for-loop body is surrounded by braces
            Optional<Replacement> replacement = surroundWithBraces( (JCTree)node.getStatement(), input );
            if( replacement.isPresent() ) {
                // need to insert braces around for-loop body so don't bother processing
                // the rest of the for-loop until after the braces have been inserted
                addReplacement( replacement.get() );
            } else {
                StringBuilder sb = new StringBuilder();
                openArgsList( "for", sb );

                sb.append( padding.methodArg );
                sb.append( input.stringifyTree( (JCTree)node.getVariable() ) );
                sb.append( SPACE );
                sb.append( ":" );
                sb.append( SPACE );
                sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
                sb.append( padding.methodArg );
                sb.append( ")" );
                appendOpeningBrace( input.newline, sb );

                // collect leading comments and place them below opening left brace
                int start = input.getFirstTokenIndex( (JCTree)node );
                int leftBrace = input.getFirstTokenIndex( (JCTree)node.getStatement() );
                sb.append( input.collectCommentsAndNewlines( (start + 1), leftBrace ) );

                // add loop body, after stripping off surrounding braces
                sb.append( stripBraces( (JCTree)node.getStatement(), input ) );

                sb.append( input.newline );
                sb.append( "}" );

                if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            }

            return super.visitEnhancedForLoop( node, input );
        }

//        @Override
//        public Void visitExpressionStatement(ExpressionStatementTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitExpressionStatement======" );
//            System.out.println( input.stringifyTree( (JCTree)node ) );
//            return super.visitExpressionStatement( node, input );
//        }

        @Override
        public Void visitForLoop(ForLoopTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitForLoop======" );
            // verify that for-loop body is surrounded by braces
            Optional<Replacement> replacement = surroundWithBraces( (JCTree)node.getStatement(), input );
            if( replacement.isPresent() ) {
                // need to insert braces around for-loop body so don't bother processing
                // the rest of the for-loop until after the braces have been inserted
                addReplacement( replacement.get() );
            } else {
                StringBuilder sb = new StringBuilder();
                openArgsList( "for", sb );
                sb.append( padding.methodArg );

                // separate initializers by a single-space
                sb.append(
                        node.getInitializer().stream()
                                .map( JCTree.class::cast )
                                .map( input::stringifyTree )
                                .collect( Collectors.joining( SPACE ) )
                );
                sb.append( ";" );
                sb.append( SPACE );

                sb.append( input.stringifyTree( (JCTree)node.getCondition() ) );
                sb.append( ";" );
                sb.append( SPACE );

                // separate updates by a single-space
                sb.append(
                        node.getUpdate().stream()
                                .map( JCTree.class::cast )
                                .map( input::stringifyTree )
                                .collect( Collectors.joining( SPACE ) )
                );

                sb.append( padding.methodArg );
                sb.append( ")" );
                appendOpeningBrace( input.newline, sb );

                // collect leading comments and place them below opening left brace
                int leftBrace = input.getFirstTokenIndex( (JCTree)node.getStatement() );
                int rightParen = input.findPrev( leftBrace, TokenType.RIGHT_PAREN ).getAsInt();
                sb.append( input.collectCommentsAndNewlines( (rightParen + 1), leftBrace ) );

                // add loop body, after stripping off surrounding braces
                String strippedBody = stripBraces( (JCTree)node.getStatement(), input );
                if( !strippedBody.isEmpty() ) {
                    sb.append( strippedBody );
                    sb.append( input.newline );
                }

                sb.append( "}" );

//                printBeforeAfter( (JCTree)node, input, sb );
                if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            }

            return super.visitForLoop( node, input );
        }

//        @Override
//        public Void visitIdentifier(IdentifierTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitIdentifier======" );
//            return super.visitIdentifier( node, input );
//        }

//        @Override
//        public Void visitIntersectionType(IntersectionTypeTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitIntersection======" );
//
//            return super.visitIntersectionType( node, input );
//        }

        @Override
        public Void visitIf(IfTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitIf======" );
            // verify that if-block body is surrounded by braces
            JCTree thenStatement = (JCTree)node.getThenStatement();
            Optional<Replacement> replacement = surroundWithBraces( thenStatement, input );
            if( replacement.isPresent() ) {
                // need to insert braces around if-block body so don't bother processing
                // the rest of the if-block until after the braces have been inserted
                addReplacement( replacement.get() );
            } else {
                //NOTE: Conditions are also processed by the visitParenthesized method, so we
                //      are unable to specify desired padding here without causing a conflict.
                //      As such, if-elseif padding is handled by a separate formatter
                StringBuilder sb = new StringBuilder();
                sb.append( "if" );
                sb.append( input.stringifyTree( (JCTree)node.getCondition() ) );
                appendOpeningBrace( input.newline, sb );

                // collect leading comments and place them below opening left brace
                int endCondition = input.getLastTokenIndex( (JCTree)node.getCondition() );
                int leftBrace = input.findNext( (endCondition + 1), TokenType.LEFT_BRACE ).getAsInt();
                sb.append( input.collectCommentsAndNewlines( (endCondition + 1), leftBrace ) );

                sb.append( stripBraces( (JCTree)node.getThenStatement(), input ) );
                sb.append( input.newline );
                sb.append( "}" );

                StatementTree elseStatement = node.getElseStatement();
                if( elseStatement != null ) {
                    sb.append( cuddleBraces ? SPACE : input.newline );
                    sb.append( "else" );

                    // collect leading comments
                    int elseStart = input.getFirstTokenIndex( (JCTree)elseStatement );
                    int rightBrace = input.findPrev( elseStart, TokenType.RIGHT_BRACE ).getAsInt();
                    leftBrace = input.findNext( (rightBrace + 1), TokenType.LEFT_BRACE ).getAsInt();
                    String leadingComments = input.collectCommentsAndNewlines( (rightBrace + 1), leftBrace );

                    if( elseStatement.getKind() == Kind.IF ) {
                        // this is an else-if block
                        sb.append( SPACE );
                        if( !leadingComments.isEmpty() ) {
                            // filter out any comments between else-if and opening left brace
                            for( int idx = elseStart; idx < (leftBrace + 1); idx++ ) {
                                if( !TokenUtils.isComment( input.tokens.get( idx ) ) ) {
                                    sb.append( input.tokens.get( idx ).getText() );
                                }
                            }

                            // insert leading comments below opening left brace
                            sb.append( input.newline );
                            sb.append( leadingComments );

                            // insert remainder of else statement
                            int elseEnd = input.getLastTokenIndex( (JCTree)elseStatement );
                            sb.append( input.stringifyTokens( (leftBrace + 2), elseEnd ) );
                        } else {
                            // no leading comments so we can just insert the entire else statement
                            sb.append( input.stringifyTree( (JCTree)elseStatement ) );
                        }
                    } else {
                        // this is just an else block
                        appendOpeningBrace( input.newline, sb );

                        // insert leading comments below opening left brace
                        sb.append( leadingComments );

                        sb.append( stripBraces( (JCTree)elseStatement, input ) );
                        sb.append( input.newline );
                        sb.append( "}" );
                    }
                }

                if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            }

            return super.visitIf( node, input );
        }

        @Override
        public Void visitImport(ImportTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitImport======" );
            return super.visitImport( node, input );
        }

        @Override
        public Void visitInstanceOf(InstanceOfTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitInstanceOf======" );
            StringBuilder sb = new StringBuilder();
            sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
            sb.append( SPACE );
            sb.append( "instanceof" );
            sb.append( SPACE );
            sb.append( input.stringifyTree( (JCTree)node.getType() ) );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitInstanceOf( node, input );
        }

        @Override
        public Void visitLabeledStatement(LabeledStatementTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitLabeledStatement======" );
            StringBuilder sb = new StringBuilder( node.getLabel() );
            sb.append( ":" );
            sb.append( input.newline );
            sb.append( input.stringifyTree( (JCTree)node.getStatement() ) );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitLabeledStatement( node, input );
        }

        @Override
        public Void visitLambdaExpression(LambdaExpressionTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitLambdaExpression======" );
            // Statement lambdas don't appear to be parsed correctly, so we only handle expression lambdas
            if( node.getBodyKind() == BodyKind.EXPRESSION ) {
                StringBuilder sb = new StringBuilder( "(" );
                sb.append(
                        node.getParameters().stream()
                                .map( JCTree.class::cast )
                                .map( input::stringifyTree )
                                .collect( Collectors.joining( ", ") )
                );
                sb.append( ")" );
                sb.append( SPACE );
                sb.append( "->" );
                sb.append( SPACE );
                sb.append( input.stringifyTree( (JCTree)node.getBody() ) );

                if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            }

            return super.visitLambdaExpression( node, input );
        }

        @Override
        public Void visitLiteral(LiteralTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitLiteral======" );
            // A negative numeric literal -n is usually represented as unary minus on n,
            // but that doesn't work for integer or long MIN_VALUE. The parser works
            // around that by representing it directly as a singed literal. The
            // getValue().toString() operation will remove any extra whitespace between
            // the minus sign and the number.
            if( node.getValue().toString().startsWith( "-" ) ) {
                StringBuilder sb = new StringBuilder( node.getValue().toString() );
                if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            }

            return super.visitLiteral( node, input );
        }

        @Override
        public Void visitMemberReference(MemberReferenceTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitMemberReference======" );
            StringBuilder sb = new StringBuilder();
            sb.append( input.stringifyTree( (JCTree)node.getQualifierExpression() ) );
            sb.append( "::" );
            sb.append( node.getMode() == ReferenceMode.NEW ? "new" : node.getName() );

//            printBeforeAfter( (JCTree)node, input, sb );
            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitMemberReference( node, input );
        }

        @Override
        public Void visitMemberSelect( MemberSelectTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitMemberSelect======" );
            StringBuilder sb = new StringBuilder();
            sb.append( node.getExpression() );
            sb.append( "." );
            sb.append( node.getIdentifier() );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitMemberSelect( node, input );
        }

        @Override
        public Void visitMethod( MethodTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitMethod======" );
            StringBuilder sb = new StringBuilder();

            if( input.isValid( (JCTree)node.getModifiers() ) ) {
                sb.append( input.stringifyTree( (JCTree)node.getModifiers() ) );
                sb.append( SPACE );
            }

            sb.append( appendTypeParameters( node.getTypeParameters(), input, sb ) );
            sb.append( input.stringifyTree( (JCTree)node.getReturnType() ) );
            sb.append( SPACE );

            openArgsList( node.getName().toString(), sb );

            if( !node.getParameters().isEmpty() ) {
                sb.append( padding.methodArg );
            }
            // separate method params by a comma and single-space
            sb.append(
                    node.getParameters().stream()
                            .map( JCTree.class::cast )
                            .map( input::stringifyTree )
                            .collect( Collectors.joining( ", " ) )
            );
            if( !node.getParameters().isEmpty() ) {
                sb.append( padding.methodArg );
            }
            sb.append( ")" );

            if( node.getThrows() != null && !node.getThrows().isEmpty() ) {
                sb.append( SPACE );
                sb.append( "throws" );
                sb.append( SPACE );
                sb.append(
                        node.getThrows().stream()
                                .map( JCTree.class::cast )
                                .map( input::stringifyTree )
                                .collect( Collectors.joining( ", ") )
                );
            }

            if( node.getDefaultValue() != null ) {
                sb.append( SPACE );
                sb.append( "default" );
                sb.append( SPACE );
                sb.append( input.stringifyTree( (JCTree)node.getDefaultValue() ) );
            }

            if( node.getBody() != null ) {
                appendOpeningBrace( input.newline, sb );
                sb.append( stripBraces( (JCTree)node.getBody(), input ) );
                sb.append( input.newline );
                sb.append( "}" );
            } else {
                sb.append( ";" );
            }

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitMethod( node, input );
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitMethodInvocation======" );
            StringBuilder sb = new StringBuilder();
            openArgsList( input.stringifyTree( (JCTree)node.getMethodSelect() ), sb );

            if( !node.getArguments().isEmpty() ) {
                sb.append( padding.methodArg );
                sb.append(
                        node.getArguments().stream()
                                .map( JCTree.class::cast )
                                .map( input::stringifyTree )
                                .collect( Collectors.joining( ", " ) )
                );
                sb.append( padding.methodArg );
            }
            sb.append( ")" );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitMethodInvocation( node, input );
        }

        @Override
        public Void visitModifiers( ModifiersTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitModifiers======" );
            StringBuilder sb = new StringBuilder();
            if( input.isValid( (JCTree)node ) ) {
                if( !node.getAnnotations().isEmpty() ) {
                    // append annotations, delimited by spaces
                    sb.append(
                            node.getAnnotations().stream()
                                    .map( JCTree.class::cast )
                                    .map( input::stringifyTree )
                                    .collect( Collectors.joining( SPACE ) )
                    );
                    if( !node.getFlags().isEmpty() ) {
                        sb.append( SPACE );
                    }
                }

                // append modifier flags, delimited by a single-space
                sb.append( node.getFlags().stream().map( Modifier::toString ).collect( Collectors.joining( SPACE ) ) );

                if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            }

            return super.visitModifiers( node, input );
        }

        @Override
        public Void visitNewArray( NewArrayTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitNewArray======" );
            //TODO what about top-level annotations?
            //TODO can we use getKind() here instead of checking for null initializers?
            if( node.getInitializers() != null ) {
                // check first initializer for a left-brace because we only want
                // to process the innermost initializers (only relevant for matrices)
                ExpressionTree first = node.getInitializers().get( 0 );
                int firstExprStartIdx = input.getFirstTokenIndex( (JCTree)first );
                int firstExprEndIdx = input.getLastTokenIndex( (JCTree)first );

                // only process the innermost initializers (only relevant for matrices)
                if( !input.findNext( firstExprStartIdx, firstExprEndIdx, TokenType.LEFT_BRACE ).isPresent() ) {
                    // separate each initializer by a command and a single-space
                    StringBuilder sb = new StringBuilder();
                    sb.append(
                            node.getInitializers()
                                    .stream()
                                    .map( ExpressionTree::toString )
                                    .collect( Collectors.joining( ", " ) )
                    );

                    ExpressionTree last = node.getInitializers().get( node.getInitializers().size() - 1 );
                    int lastExprEndIdx = input.getLastTokenIndex( (JCTree)last );

                    if( ENABLED ) createReplacement( input, firstExprStartIdx, lastExprEndIdx, sb ).ifPresent( this::addReplacement );
                }
            } else {
                StringBuilder sb = new StringBuilder( "new " );
                sb.append( input.stringifyTree( (JCTree)node.getType() ) );
                List<? extends List<? extends AnnotationTree>> dimAnnotations = node.getDimAnnotations();
                for( int dim = 0; dim < node.getDimensions().size(); dim++ ) {
                    if( !dimAnnotations.get( dim ).isEmpty() ) {
                        sb.append( SPACE );
                        for( AnnotationTree annotation : dimAnnotations.get( dim ) ) {
                            sb.append( input.stringifyTree( (JCTree)annotation ) );
                            sb.append( SPACE );
                        }
                    }

                    sb.append( "[" );
                    sb.append( node.getDimensions().get( dim ).toString() );
                    sb.append( "]" );
                }

                int startIdx = input.getFirstTokenIndex( (JCTree)node );
                int semicolonIdx = input.findNext( startIdx, TokenType.SEMICOLON ).getAsInt();
                if( ENABLED ) createReplacement( input, startIdx, semicolonIdx, sb ).ifPresent( this::addReplacement );
            }

            return super.visitNewArray( node, input );
        }

        @Override
        public Void visitNewClass( NewClassTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitNewClass======" );
            StringBuilder sb = new StringBuilder( "new" );
            sb.append( SPACE );
            openArgsList( input.stringifyTree( (JCTree)node.getIdentifier() ), sb );
            if( !node.getArguments().isEmpty() ) {
                sb.append( padding.methodArg );
                sb.append(
                        node.getArguments().stream()
                                .map( JCTree.class::cast )
                                .map( input::stringifyTree )
                                .collect( Collectors.joining( ", ") )
                );
                sb.append( padding.methodArg );
            }
            sb.append( ")" );

            if( node.getClassBody() != null ) {
                appendOpeningBrace( input.newline, sb );
                sb.append( stripBraces( (JCTree)node.getClassBody(), input ) );
                sb.append( input.newline );
                sb.append( "}" );
            }

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitNewClass( node, input );
        }

//        @Override
//        public Void visitOther( Tree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitOther======" );
//            return super.visitOther( node, input );
//        }

        @Override
        public Void visitParameterizedType( ParameterizedTypeTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitParameterizedType======" );
            StringBuilder sb = new StringBuilder();
            sb.append( input.stringifyTree( (JCTree)node.getType() ) );
            sb.append( "<" );
            if( !node.getTypeArguments().isEmpty() ) {
                sb.append( padding.typeParam );
            }
            // separate method params by a command and single-space
            sb.append(
                    node.getTypeArguments().stream()
                            .map( JCTree.class::cast )
                            .map( input::stringifyTree )
                            .collect( Collectors.joining( ", " ) )
            );
            if( !node.getTypeArguments().isEmpty() ) {
                sb.append( padding.typeParam );
            }
            sb.append( ">" );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitParameterizedType( node, input );
        }

        @Override
        public Void visitParenthesized( ParenthesizedTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitParenthesized======" );
            //NOTE: This method appears to be called not only for parenthesized groups, but
            //      also for if, else-if, switch, and synchronized conditional statements
            StringBuilder sb = new StringBuilder();
            sb.append( "(" );
            sb.append( padding.parenGrouping );
            sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
            sb.append( padding.parenGrouping );
            sb.append( ")" );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitParenthesized( node, input );
        }

//        @Override
//        public Void visitPrimitiveType( PrimitiveTypeTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitPrimitiveType======" );
//            return super.visitPrimitiveType( node, input );
//        }

        @Override
        public Void visitReturn( ReturnTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitReturn======" );
            StringBuilder sb = new StringBuilder( "return" );
            sb.append( SPACE );
            sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
            sb.append( ";" );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitReturn( node, input );
        }

        @Override
        public Void visitSwitch( SwitchTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitSwitch======" );
            //NOTE: Switch conditions are also processed by the visitParenthesized method,
            //      so we are unable to specify desired padding here without causing a
            //      conflict. As such, switch padding is handled by a separate formatter
            StringBuilder sb = new StringBuilder( "switch" );
            sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
            appendOpeningBrace( input.newline, sb );

            // collect leading comments and place them below opening left brace
            int endExpression = input.getLastTokenIndex( (JCTree)node.getExpression() );
            int startCases = input.getFirstTokenIndex( (JCTree)node.getCases().get( 0 ) );
            sb.append( input.collectCommentsAndNewlines( (endExpression + 1), startCases ) );

            sb.append(
                    node.getCases().stream()
                            .map( JCTree.class::cast )
                            .map( input::stringifyTree )
                            .collect( Collectors.joining( input.newline ) )
            );
            sb.append( input.newline );
            sb.append( "}" );

            //TODO we're dropping trailing comments (those that come after cases)
            //TODO probably doing this in other places as well

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitSwitch( node, input );
        }

        @Override
        public Void visitSynchronized( SynchronizedTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitSynchronized======" );
            //NOTE: Synchronized conditions are also processed by the visitParenthesized
            //      method, so we are unable to specify desired padding here without causing a
            //      conflict. As such, synchronized padding is handled by a separate formatter
            StringBuilder sb = new StringBuilder( "synchronized" );
            sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
            appendOpeningBrace( input.newline, sb );

            // collect leading comments and place them below opening left brace
            int start = input.getFirstTokenIndex( (JCTree)node );
            int leftBrace = input.getFirstTokenIndex( (JCTree)node.getBlock() );
            sb.append( input.collectCommentsAndNewlines( (start + 1), leftBrace ) );

            sb.append( stripBraces( (JCTree)node.getBlock(), input ) );
            sb.append( input.newline );
            sb.append( "}" );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitSynchronized( node, input );
        }

        @Override
        public Void visitThrow( ThrowTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitThrow======" );
            StringBuilder sb = new StringBuilder( "throw " );
            sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
            sb.append( ";" );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitThrow( node, input );
        }

        @Override
        public Void visitTry( TryTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitTry" );
            StringBuilder sb = new StringBuilder();

            List<? extends Tree> resources = node.getResources();
            if( resources != null && !resources.isEmpty() ) {
                openArgsList( "try", sb );
                sb.append( padding.methodArg );
                sb.append(
                        resources.stream()
                                .map( JCTree.class::cast )
                                .map( input::stringifyTree )
                                .collect( Collectors.joining( ", " ) )
                );
                sb.append( padding.methodArg );
                sb.append( ")" );
            } else {
                sb.append( "try" );
            }
            appendOpeningBrace( input.newline, sb );

            // collect leading comments and place them below opening left brace
            int start = input.getFirstTokenIndex( (JCTree)node );
            int leftBrace = input.getFirstTokenIndex( (JCTree)node.getBlock() );
            sb.append( input.collectCommentsAndNewlines( (start + 1), leftBrace ) );

            sb.append( stripBraces( (JCTree)node.getBlock(), input ) );
            sb.append( input.newline );
            sb.append( "}" );
            sb.append( cuddleBraces ? SPACE : input.newline );

            for( CatchTree tree : node.getCatches() ) {
                sb.append( input.stringifyTree( (JCTree)tree ) );
            }

            if( input.isValid( (JCTree)node.getFinallyBlock() ) ) {
                sb.append( cuddleBraces ? SPACE : input.newline );
                sb.append( "finally" );
                appendOpeningBrace( input.newline, sb );

                // collect leading comments and place them below opening left brace
                leftBrace = input.getFirstTokenIndex( (JCTree)node.getFinallyBlock() );
                int rightBrace = input.findPrev( leftBrace, TokenType.RIGHT_BRACE ).getAsInt();
                sb.append( input.collectCommentsAndNewlines( (rightBrace + 1), leftBrace ) );

                sb.append( stripBraces( (JCTree)node.getFinallyBlock(), input ) );
                sb.append( input.newline );
                sb.append( "}" );
            }

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitTry( node, input );
        }

        @Override
        public Void visitTypeCast( TypeCastTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitTypeCast======" );
            StringBuilder sb = new StringBuilder();
            sb.append( "(" );
            sb.append( padding.typeCast );
            sb.append( input.stringifyTree( (JCTree)node.getType() ) );
            sb.append( padding.typeCast );
            sb.append( ")" );
            sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitTypeCast( node, input );
        }

        @Override
        public Void visitTypeParameter( TypeParameterTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitTypeParameter======" );
            StringBuilder sb = new StringBuilder();
            for( AnnotationTree annotation : node.getAnnotations() ) {
                sb.append( input.stringifyTree( (JCTree)annotation ) );
                sb.append( SPACE );
            }

            sb.append( node.getName() );

            if( !node.getBounds().isEmpty() ) {
                sb.append( SPACE );
                sb.append( "extends" );
                sb.append( SPACE );
                sb.append( node.getBounds().stream().map( Tree::toString ).collect( Collectors.joining( ", " ) ) );
            }

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitTypeParameter( node, input );
        }

        @Override
        public Void visitUnionType(UnionTypeTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitUnionType======" );
            StringBuilder sb = new StringBuilder();
            sb.append(
                    node.getTypeAlternatives().stream()
                            .map( JCTree.class::cast )
                            .map( input::stringifyTree )
                            .collect( Collectors.joining( " | " ) )
            );

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitUnionType( node, input );
        }

        @Override
        public Void visitUnary(UnaryTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitUnary======" );
            StringBuilder sb = new StringBuilder();
            if( node.getKind() == Kind.POSTFIX_DECREMENT || node.getKind() == Kind.POSTFIX_INCREMENT ) {
                sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
                sb.append( getOperator( node.getKind() ) );
            } else {
                sb.append( getOperator( node.getKind() ) );
                sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
            }

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitUnary( node, input );
        }

        @Override
        public Void visitVariable(VariableTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitVariable======" );
            StringBuilder sb = new StringBuilder();

            if( input.isValid( (JCTree)node.getModifiers() ) ) {
                sb.append( input.stringifyTree( (JCTree)node.getModifiers() ) );
                sb.append( SPACE );
            }

            if( input.isValid( (JCTree)node.getType() ) ) {
                sb.append( input.stringifyTree( (JCTree)node.getType() ) );
                sb.append( SPACE );
            }

            sb.append( node.getName() );

            if( input.isValid( (JCTree)node.getInitializer() ) ) {
                sb.append( SPACE );
                sb.append( "=" );
                sb.append( SPACE );
                sb.append( input.stringifyTree( (JCTree)node.getInitializer() ) );
            }

            int startIdx = input.getFirstTokenIndex( (JCTree)node );
            int endIdx = input.getLastTokenIndex( (JCTree)node );
            OptionalInt semi = input.findPrev( startIdx, endIdx, TokenType.SEMICOLON );
            if( semi.isPresent() ) {
                sb.append( ";" );
            }

            if( ENABLED ) createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );
            return super.visitVariable( node, input );
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitWhileLoop======" );
            return super.visitWhileLoop( node, input );
        }

        protected String stripBraces( JCTree tree, Input input ) {
            return stripGroupingSymbols( tree, input, TokenType.LEFT_BRACE, TokenType.RIGHT_BRACE );
        }

        protected String stripParens( JCTree tree, Input input ) {
            return stripGroupingSymbols( tree, input, TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN );
        }

        protected String stripGroupingSymbols( JCTree tree, Input input, TokenType leftToken, TokenType rightToken ) {
            StringBuilder sb = new StringBuilder();

            int start = input.getFirstTokenIndex( tree );
            int end = input.getLastTokenIndex( tree );
            OptionalInt leftSymbol = input.findNextByExclusion( start, end, WS_NEWLINE_OR_COMMENT );

            if( leftSymbol.isPresent() && input.tokens.get( leftSymbol.getAsInt() ).type == leftToken ) {
                int rightSymbolIdx = input.findPrev( (end + 1), rightToken ).getAsInt();
                OptionalInt bodyStart = input.findNextByExclusion( (leftSymbol.getAsInt() + 1), WS_OR_NEWLINE );
                if( bodyStart.isPresent() ) {
                    int bodyEnd = input.findPrevByExclusion( rightSymbolIdx, WS_OR_NEWLINE ).getAsInt();
                    sb.append( input.stringifyTokens( bodyStart.getAsInt(), (bodyEnd + 1) ) );
                }
            }

            return sb.toString();
        }

        protected String appendTypeParameters( List<? extends TypeParameterTree> params, Input input, StringBuilder sb ) {
            if( !params.isEmpty() ) {
                sb.append( "<" );
                sb.append( padding.typeParam );

                sb.append(
                        params.stream()
                                .map( JCTree.class::cast )
                                .map( input::stringifyTree )
                                .collect( Collectors.joining( ", ") )
                );

                sb.append( padding.typeParam );
                sb.append( ">" );
                return SPACE;
            } else {
                return "";
            }
        }

        protected void openArgsList( String controlBlockName, StringBuilder sb ) {
            sb.append( controlBlockName );
            sb.append( padding.methodName );
            sb.append( "(" );
        }

//        protected void closeArgsList(
//                Input input,
//                List<? extends ExpressionTree> throwsTrees,
//                boolean isAbstractMethod,
//                StringBuilder sb
//        ) {
//            sb.append( ")" );
//
//            if( throwsTrees != null && !throwsTrees.isEmpty() ) {
//                sb.append( SPACE );
//                sb.append( "throws" );
//                sb.append( SPACE );
//                sb.append(
//                        throwsTrees.stream()
//                                .map( JCTree.class::cast )
//                                .map( input::stringifyTree )
//                                .collect( Collectors.joining( ", ") )
//                );
//            }
//
//            if( isAbstractMethod ) {
//                sb.append( ";" );
//            } else {
//                appendOpeningBrace( input.newline, sb );
//            }
//        }

        protected void appendOpeningBrace( String newline, StringBuilder sb ) {
            sb.append( cuddleBraces ? SPACE : newline );
            sb.append( "{" );
            sb.append( newline );
        }

//        protected void addBody( JCTree body, Input input, StringBuilder sb ) {
//            if( body == null ) {
//                return;
//            }
//
//            int start = input.getFirstTokenIndex( body );
//            int end = input.getLastTokenIndex( body );
//
//            // here we assume that we've already ensured that the body is surrounded by braces
//            int leftBrace = input.findNext( start, end, TokenType.LEFT_BRACE ).getAsInt();
//            int rightBrace = input.findPrev( (leftBrace + 1), end, TokenType.RIGHT_BRACE ).getAsInt();
//
//            OptionalInt firstCodeOrComment = input.findNextByExclusion( (leftBrace + 1), rightBrace, WS_OR_NEWLINE );
//            if( firstCodeOrComment.isPresent() ) {
//                int lastCodeOrComment = input.findPrevByExclusion( rightBrace, WS_OR_NEWLINE ).getAsInt();
//
//                // the actual body of the block, excluding surrounding braces, whitespace, and newlines
//                sb.append( input.stringifyTokens( firstCodeOrComment.getAsInt(), (lastCodeOrComment + 1) ) );
//                sb.append( input.newline );
//            }
//        }

        private Optional<Replacement> surroundWithBraces( JCTree tree, Input input ) {
            if( tree != null ) {
                // get token indices corresponding to the bounds of the tree
                int treeStartIdx = input.getFirstTokenIndex( tree );
                int treeEndIdx = input.getLastTokenIndex( tree );

                // check if tree is surrounded by curly braces
                if( input.tokens.get( treeStartIdx ).type != TokenType.LEFT_BRACE ) {
                    // find first non-whitespace, non-newline, non-comment token before tree
                    int parentStatement = input.findPrevByExclusion( treeStartIdx, WS_NEWLINE_OR_COMMENT )
                            //TODO throw FormatException with line/column numbers?
                            .orElseThrow( () -> new RuntimeException(
                                    "Missing parent statement: " + tree.getKind().toString() ) );

                    // make sure we preserve any comments before the tree
                    int firstToKeep =
                            input.findNext( (parentStatement + 1), treeStartIdx, COMMENT_OR_NEWLINE )
                            .orElse( treeStartIdx );

                    // make sure we preserve any inline comments following the first line of code in the tree
                    int lastToKeep =
                            input.findNext( treeStartIdx, TokenType.NEWLINE )
                            .orElse( treeEndIdx ); //TODO won't this always be treeEndIdx?

                    // determine token range to be replaced
                    TextToken firstTokenToReplace = input.tokens.get( parentStatement + 1 );
                    TextToken lastTokenToReplace = input.tokens.get( lastToKeep );

                    // build up replacement text
                    StringBuilder sb = new StringBuilder();
                    sb.append( (cuddleBraces ? SPACE : input.newline) );
                    sb.append( "{" );
                    if( isComment(input.tokens.get( firstToKeep ) ) ) {
                        sb.append( SPACE );
                    }
                    sb.append( input.stringifyTokens( firstToKeep, (lastToKeep + 1) ) );
                    sb.append( "}" );
                    sb.append( input.newline );

                    return Optional.of(
                            new Replacement(
                                    firstTokenToReplace.start,
                                    lastTokenToReplace.end,
                                    sb.toString()
                            )
                    );
                }
            }

            return Optional.empty();
        }

        protected String getOperator( Kind kind ) {
            switch( kind ) {
                case PLUS:
                    return "+";
                case MINUS:
                    return "-";
                case LOGICAL_COMPLEMENT:
                    return "!";
                case BITWISE_COMPLEMENT:
                    return "~";
                case PREFIX_INCREMENT:
                    return "++";
                case PREFIX_DECREMENT:
                    return "--";
                case POSTFIX_INCREMENT:
                    return "++";
                case POSTFIX_DECREMENT:
                    return "--";
                case CONDITIONAL_OR:
                    return "||";
                case CONDITIONAL_AND:
                    return "&&";
                case EQUAL_TO:
                    return "==";
                case NOT_EQUAL_TO:
                    return "!=";
                case LESS_THAN:
                    return "<";
                case GREATER_THAN:
                    return ">";
                case LESS_THAN_EQUAL:
                    return "<=";
                case GREATER_THAN_EQUAL:
                    return ">=";
                case OR:
                    return "|";
                case XOR:
                    return "^";
                case AND:
                    return "&";
                case LEFT_SHIFT:
                    return "<<";
                case RIGHT_SHIFT:
                    return ">>";
                case UNSIGNED_RIGHT_SHIFT:
                    return ">>>";
                case UNARY_PLUS:
                    return "+";
                case UNARY_MINUS:
                    return "-";
                case MULTIPLY:
                    return "*";
                case DIVIDE:
                    return "/";
                case REMAINDER:
                    return "%";
                case MULTIPLY_ASSIGNMENT:
                    return "*=";
                case DIVIDE_ASSIGNMENT:
                    return "/=";
                case REMAINDER_ASSIGNMENT:
                    return "/=";
                case PLUS_ASSIGNMENT:
                    return "+=";
                case MINUS_ASSIGNMENT:
                    return "-=";
                case LEFT_SHIFT_ASSIGNMENT:
                    return "<<=";
                case RIGHT_SHIFT_ASSIGNMENT:
                    return ">>=";
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                    return ">>>=";
                case AND_ASSIGNMENT:
                    return "&=";
                case XOR_ASSIGNMENT:
                    return "^=";
                case OR_ASSIGNMENT:
                    return "|=";
                default: throw new Error();
            }
        }

        private void addModifiers( ModifiersTree modifiers, StringBuilder sb ) {
            // separate modifiers by a single space
            if( modifiers != null ) {
                for( Modifier modifier : modifiers.getFlags() ) {
                    sb.append( modifier.toString() );
                    sb.append( SPACE );
                }
            }
        }

        private void addTypeParams( List<? extends TypeParameterTree> params, StringBuilder sb ) {
            // separate type parameters by a comma and a space
            if( params != null ) {
                sb.append( "<" );
                sb.append(
                        params.stream()
                                .map( TypeParameterTree::toString )
                                .collect( Collectors.joining( ", " ) )
                );
                sb.append( ">" );
                sb.append( SPACE );
            }
        }

        private void printTree( JCTree tree, Input input ) {
            int startIdx = input.getFirstTokenIndex( tree );
            int endIdx = input.getLastTokenIndex( tree );
            for( int pos=startIdx; pos<endIdx; pos++ ) {
                System.out.println( pos + ": [" + input.tokens.get( pos ).getText() + "]" );
            }
        }
        private void printBeforeAfter( JCTree tree, Input input, StringBuilder sb ) {
            System.out.println( input.stringifyTree( tree ) );
            System.out.println( "----------------------" );
            System.out.println( sb.toString() );
        }
    }

}
