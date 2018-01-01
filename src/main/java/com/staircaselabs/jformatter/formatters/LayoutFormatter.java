package com.staircaselabs.jformatter.formatters;

import com.staircaselabs.jformatter.core.CommentedTree;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.Padding;
import com.staircaselabs.jformatter.core.Replacement;
import com.staircaselabs.jformatter.core.ReplacementFormatter;
import com.staircaselabs.jformatter.core.ReplacementScanner;
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
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LambdaExpressionTree.BodyKind;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.staircaselabs.jformatter.core.CompilationUnitUtils.isValid;
import static com.staircaselabs.jformatter.core.Input.SPACE;
import static java.util.stream.Collectors.partitioningBy;

//TODO:
// 1. make sure comments are not being dropped
// 4. check code coverage of all visit methods
// 5. figure out how to handle exceptions
// 8. add java.util.Logging
public class LayoutFormatter extends ReplacementFormatter {

    public LayoutFormatter( Padding padding, boolean cuddleBraces ) {
        super( new LayoutScanner( padding, cuddleBraces ) );
    }

    private static class LayoutScanner extends ReplacementScanner {

        private static final boolean VERBOSE = false;
        private static final boolean ENABLED = true;
        private static final String NAME = "LayoutFormatter::";

        private static final TokenType[] WS_OR_COMMENT = {
                TokenType.WHITESPACE,
                TokenType.COMMENT_BLOCK,
                TokenType.COMMENT_JAVADOC,
                TokenType.COMMENT_LINE
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

        private final Padding padding;
        private final boolean cuddleBraces;

        private LayoutScanner(Padding padding, boolean cuddleBraces ) {
            this.padding = padding;
            this.cuddleBraces = cuddleBraces;
        }

        @Override
        public Void visitAnnotatedType( AnnotatedTypeTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitAnnotatedType======" );
            if( node.getUnderlyingType().getKind() != Kind.ARRAY_TYPE ) {
                List<Tree> annotations =
                        node.getAnnotations().stream().map( Tree.class::cast ).collect( Collectors.toList() );

                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "AnnotatedType" )
                        .appendList( annotations, SPACE )
                        .append( SPACE )
                        .append( node.getUnderlyingType() );
                if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            }

            return super.visitAnnotatedType( node, input );
        }

        @Override
        public Void visitAnnotation( AnnotationTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitAnnotation======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Annotation" )
                    .append( TokenType.AT )
                    .append( node.getAnnotationType() );

            List<? extends ExpressionTree> args = node.getArguments();
            if( !args.isEmpty() ) {
                List<Tree> argsList = args.stream().map( Tree.class::cast ).collect( Collectors.toList() );

                replacement.append( TokenType.LEFT_PAREN )
                        .append( padding.methodArg )
                        .appendList( argsList, TokenType.COMMA, true )
                        .append( padding.methodArg )
                        .append( TokenType.RIGHT_PAREN );
            }

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitAnnotation( node, input );
        }

        @Override
        public Void visitArrayAccess( ArrayAccessTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitArrayAccess======" );
            if( node.getExpression().getKind() == Kind.IDENTIFIER ) {
                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "ArrayAccess" )
                        .append( node.getExpression() )
                        .append( TokenType.LEFT_BRACKET )
                        .append( padding.array )
                        .append( node.getIndex() )
                        .append( padding.array )
                        .append( TokenType.RIGHT_BRACKET );

                int afterRightBracket = replacement.getCurrentPosInclusive();
                int start = input.getFirstTokenIndex( node );
                if( ENABLED ) replacement.build( start, afterRightBracket ).ifPresent( this::addReplacement );
            } else {
                int arrayIndex = input.getFirstTokenIndex( node.getIndex() );
                int prevRightBracket = input.findPrev( arrayIndex, TokenType.RIGHT_BRACKET ).getAsInt();
                int end = input.getLastTokenIndex( node );

                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "ArrayAccess" )
                        .setCurrentPositionInclusive( prevRightBracket + 1 )
                        .append( TokenType.LEFT_BRACKET )
                        .append( padding.array )
                        .append( node.getIndex() )
                        .append( padding.array )
                        .append( TokenType.RIGHT_BRACKET );
                if( ENABLED ) replacement.build( (prevRightBracket + 1), end ).ifPresent( this::addReplacement );
            }

            return super.visitArrayAccess( node, input );
        }

        @Override
        public Void visitArrayType( ArrayTypeTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitArrayType======" );
            int start = input.getFirstTokenIndex( node );
            int end = input.getLastTokenIndex( node );
            int pos = input.findNext( start, WS_NEWLINE_COMMENT_OR_BRACKET ).getAsInt();

            // append array type
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "ArrayType" );
            replacement.append( input.stringifyTokens( start, pos ) );

            OptionalInt leftBracket = input.findNext( pos, TokenType.LEFT_BRACKET );
            while( leftBracket.isPresent() ) {
                // append any annotations
                int leftBracketIdx = leftBracket.getAsInt();
                OptionalInt annotationStart = input.findNext( pos, leftBracketIdx, TokenType.AT );
                if( annotationStart.isPresent() ) {
                    int annotationEnd = input.findPrevByExclusion( leftBracketIdx, WS_NEWLINE_OR_COMMENT ).getAsInt();
                    replacement.append( SPACE )
                            .append( input.stringifyTokens( annotationStart.getAsInt(), (annotationEnd + 1) ) )
                            .append( SPACE )
                            .setCurrentPositionInclusive( annotationEnd + 1 );
                }

                // append brackets
                replacement.append( TokenType.LEFT_BRACKET ).append( TokenType.RIGHT_BRACKET );
                pos = replacement.getCurrentPosInclusive();
                leftBracket = input.findNext( pos, end, TokenType.LEFT_BRACKET );
            }

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
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
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Assignment" )
                    .append( node.getVariable() )
                    .append( SPACE )
                    .append( TokenType.ASSIGNMENT )
                    .append( SPACE )
                    .append( node.getExpression() );
            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );

            return super.visitAssignment( node, input );
        }

        @Override
        public Void visitBinary( BinaryTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitBinary======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Binary" )
                    .append( node.getLeftOperand() )
                    .append( SPACE )
                    .append( TokenUtils.tokenTypeFromBinaryOperator( node.getKind() ) )
                    .append( SPACE )
                    .append( node.getRightOperand() );


            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
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
            // add break keyword
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Break" )
                    .append( TokenType.BREAK );

            // add label
            if( node.getLabel() != null ) {
                int start = input.getFirstTokenIndex( node );
                int breakIdx = input.findNext( start, TokenType.BREAK ).getAsInt();
                int labelStart = input.findNextByExclusion( (breakIdx + 1), WS_NEWLINE_OR_COMMENT ).getAsInt();
                replacement.append( SPACE )
                        .append( labelStart );

            }

            // add closing semicolon
            int end = input.getLastTokenIndex( node );
            int semi = input.findPrev( end, TokenType.SEMICOLON ).getAsInt();
            int lastAdded = input.findPrevByExclusion( semi, WS_NEWLINE_OR_COMMENT ).getAsInt();
            replacement.setCurrentPositionInclusive( lastAdded + 1 )
                    .append( TokenType.SEMICOLON );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitBreak( node, input );
        }

        @Override
        public Void visitCase( CaseTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitCase======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Case" );
            if( node.getExpression() != null ) {
                replacement.append( TokenType.CASE )
                        .append( SPACE )
                        .append( node.getExpression() );
            } else {
                replacement.append( TokenType.DEFAULT );
            }

            replacement.append( TokenType.COLON );

            for( StatementTree tree : node.getStatements() ) {
                // append leading newline, if one exists and then the statement
                replacement.append( TokenType.NEWLINE, input.getFirstTokenIndex( tree ) )
                        .append( tree );
            }

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitCase( node, input );
        }

        @Override
        public Void visitCatch( CatchTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitCatch======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Catch" )
                    .append( TokenType.CATCH )
                    .append( TokenType.LEFT_PAREN )
                    .append( padding.methodArg )
                    .append( node.getParameter() )
                    .append( padding.methodArg )
                    .append( TokenType.RIGHT_PAREN )
                    .appendOpeningBrace( cuddleBraces )
                    .appendBracedBlock( node.getBlock(), input.newline )
                    .append( TokenType.RIGHT_BRACE );
            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );

            return super.visitCatch( node, input );
        }

        @Override
        public Void visitClass( ClassTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitClass======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Class" );

            if( isValid( node.getModifiers() ) ) {
                // append annotations, separated by newlines, and then any flags
                ModifierFormatter.appendAnnotationsAndFlags( node.getModifiers(), input, replacement, true );

                // @interface annotations, are incorrectly parsed, so we may need to manually append `interface` here
                int currentPos = replacement.getCurrentPosInclusive();
                int nextSpacePos = input.findNext( currentPos, TokenType.WHITESPACE ).orElse( currentPos );
                if( currentPos != nextSpacePos ) {
                    // if we've gotten here, then we really only expect to be appending a single token (interface)
                    while( replacement.getCurrentPosInclusive() != nextSpacePos ) {
                        replacement.append( replacement.getCurrentPosInclusive() );
                    }
                    replacement.append( input.newline );
                }
            }

            // the class keyword will not be present if this is an annotation definition
            int currentPos = replacement.getCurrentPosInclusive();
            int lastPos = input.getLastTokenIndex( node );
            if( input.findNext( currentPos, lastPos, TokenType.CLASS ).isPresent() ) {
                replacement.append( TokenType.CLASS )
                        .append( SPACE );

            }

            replacement.append( node.getSimpleName().toString() );

            List<Tree> typeParams = node.getTypeParameters().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            if( !typeParams.isEmpty() ) {
                replacement.append( TokenType.LESS_THAN )
                        .append( padding.typeParam )
                        .appendList( typeParams, TokenType.COMMA, true )
                        .append( padding.typeParam )
                        .append( TokenType.GREATER_THAN );
            }

            if( node.getExtendsClause() != null ) {
                replacement.append( SPACE )
                        .append( TokenType.EXTENDS )
                        .append( SPACE )
                        .append( node.getExtendsClause() );
            }

            List<Tree> interfaces = node.getImplementsClause().stream()
                    .map( Tree.class::cast )
                    .collect( Collectors.toList() );
            if( !interfaces.isEmpty() ) {
                replacement.append( SPACE )
                        .append( TokenType.IMPLEMENTS )
                        .append( SPACE )
                        .appendList( interfaces, TokenType.COMMA );
            }

            replacement.append( cuddleBraces ? SPACE : input.newline )
                    .append( TokenType.LEFT_BRACE );

            if( !node.getMembers().isEmpty() ) {
                Tree firstMember = node.getMembers().get( 0 );

                // group members with their associated comments
                List<CommentedTree> members = new ArrayList<>();
                int pos = replacement.getCurrentPosInclusive();
                for (Tree tree : node.getMembers()) {
                    // collect leading comments
                    int treeBegin = input.getFirstTokenIndex(tree);
                    int treeEnd = input.getLastTokenIndex(tree);
                    Optional<String> leadingComments = input.collectComments(pos, treeBegin );

                    // collect trailing inline comment
                    int endExclusive = input.findNextByExclusion(treeEnd, WS_OR_COMMENT)
                            .orElseThrow(() -> new RuntimeException("Unexpected missing token."));
                    Optional<String> trailingComment = input.collectComments(treeEnd, endExclusive);

                    members.add(new CommentedTree(tree, leadingComments, trailingComment));
                    pos = endExclusive;
                }

                // partition members into variables and methods
                Map<Boolean, List<CommentedTree>> partitionedMembers =
                        members.stream().collect(partitioningBy(t -> t.tree instanceof VariableTree));

                // add all member variables first
                for (CommentedTree ctree : partitionedMembers.get(true)) {
                    // insert newlines between annotations before appending
                    replacement.append(input.newline).append(input.newline);
                    ctree.leadingComments.ifPresent(replacement::append);

                    // insert newlines between annotations before appending
                    replacement.setCurrentPositionInclusive(input.getFirstTokenIndex(ctree.tree));
                    VariableFormatter.appendVariable((VariableTree) ctree.tree, input, replacement, true);

                    ctree.trailingInlineComment.ifPresent(replacement::append);
                }

                // now add all member methods
                for (CommentedTree ctree : partitionedMembers.get(false)) {
                    replacement.append(input.newline).append(input.newline);
                    ctree.leadingComments.ifPresent(replacement::append);
                    replacement.setCurrentPositionInclusive(input.getFirstTokenIndex(ctree.tree))
                            .append(ctree.tree);
                    ctree.trailingInlineComment.ifPresent(replacement::append);
                }
            }

            // append any remaining comments, followed by two newlines, and then the closing brace
            replacement.appendWithLeadingNewlines( TokenType.RIGHT_BRACE, 2 );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitClass( node, input );
        }

        @Override
        public Void visitCompilationUnit( CompilationUnitTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitCompUnit======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "CompilationUnit" );

            // append package annotations and package name
            node.getPackageAnnotations().stream().forEach( t -> replacement.append( t ).append( input.newline ) );
            if( node.getPackageName() != null ) {
                replacement.append( TokenType.PACKAGE ).append( SPACE ).append( node.getPackageName() );
            }

            // only replace package annotations and package name, everything else is formatted elsewhere
            int replaceStart = input.getFirstTokenIndex( node );
            int replaceStop = replacement.getCurrentPosInclusive();

            if( ENABLED ) replacement.build( replaceStart, replaceStop ).ifPresent( this::addReplacement );
            return super.visitCompilationUnit( node, input );
        }

        @Override
        public Void visitCompoundAssignment( CompoundAssignmentTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======CompoundAssignment======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "CompoundAssignment" )
                    .append( node.getVariable() )
                    .append( SPACE )
                    .append( TokenUtils.tokenTypeFromCompoundOperator( node.getKind() ) )
                    .append( SPACE )
                    .append( node.getExpression() );
            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );

            return super.visitCompoundAssignment( node, input );
        }

        @Override
        public Void visitConditionalExpression( ConditionalExpressionTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitConditionalExpression======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "ConditionalExpression" )
                    .append( node.getCondition() )
                    .append( SPACE )
                    .append( TokenType.QUESTION )
                    .append( SPACE )
                    .append( node.getTrueExpression() )
                    .append( SPACE )
                    .append( TokenType.COLON )
                    .append( SPACE )
                    .append( node.getFalseExpression() );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitConditionalExpression( node, input );
        }

        @Override
        public Void visitContinue(ContinueTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitContinue======" );
            // add break keyword
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Continue" )
                    .append( TokenType.CONTINUE );

            // add label
            if( node.getLabel() != null ) {
                int start = input.getFirstTokenIndex( node );
                int continueIdx = input.findNext( start, TokenType.CONTINUE ).getAsInt();
                int labelStart = input.findNextByExclusion( (continueIdx + 1), WS_NEWLINE_OR_COMMENT ).getAsInt();
                replacement.append( SPACE )
                        .append( labelStart );
            }

            // add closing semicolon
            int end = input.getLastTokenIndex( node );
            int semi = input.findPrev( end, TokenType.SEMICOLON ).getAsInt();
            int lastAdded = input.findPrevByExclusion( semi, WS_NEWLINE_OR_COMMENT ).getAsInt();
            replacement.setCurrentPositionInclusive( lastAdded + 1 )
                    .append( TokenType.SEMICOLON );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitContinue( node, input );
        }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitDoWhileLoop======" );
            // ensure that do-while body is surrounded by braces
            Optional<Replacement> braceReplacement = surroundWithBraces( node.getStatement(), input );
            if( braceReplacement.isPresent() ) {
                // need to insert braces around do-while body so don't bother processing
                // the rest of the do-while until after the braces have been inserted
                addReplacement( braceReplacement.get() );
                forceRescan();
            } else {
                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "DoWhileLoop" )
                        .append( TokenType.DO )
                        .appendOpeningBrace( cuddleBraces )
                        .appendBracedBlock( node.getStatement(), input.newline )
                        .append( TokenType.RIGHT_BRACE )
                        .append( cuddleBraces ? SPACE : input.newline )
                        .append( TokenType.WHILE )
                        .append( padding.methodName )
                        .append( TokenType.LEFT_PAREN )
                        .append( padding.methodArg )
                        .stripParenthesesAndAppend( node.getCondition() )
                        .append( padding.methodArg )
                        .append( TokenType.RIGHT_PAREN )
                        .append( TokenType.SEMICOLON );

                if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            }

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
            // ensure that for-loop body is surrounded by braces
            Optional<Replacement> braceReplacement = surroundWithBraces( node.getStatement(), input );
            if( braceReplacement.isPresent() ) {
                // need to insert braces around for-loop body so don't bother processing
                // the rest of the for-loop until after the braces have been inserted
                addReplacement( braceReplacement.get() );
                forceRescan();
            } else {
                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "EnhancedForLoop" )
                        .append( TokenType.FOR )
                        .append( padding.methodName )
                        .append( TokenType.LEFT_PAREN )
                        .append( padding.methodArg )
                        .append( node.getVariable() )
                        .append( SPACE )
                        .append( TokenType.COLON )
                        .append( SPACE )
                        .append( node.getExpression() )
                        .append( padding.methodArg )
                        .append( TokenType.RIGHT_PAREN )
                        .appendOpeningBrace( cuddleBraces )
                        .appendBracedBlock( node.getStatement(), input.newline )
                        .append( TokenType.RIGHT_BRACE );

                if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            }

            return super.visitEnhancedForLoop( node, input );
        }

//        @Override
//        public Void visitExpressionStatement(ExpressionStatementTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitExpressionStatement======" );
//            return super.visitExpressionStatement( node, input );
//        }

        @Override
        public Void visitForLoop(ForLoopTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitForLoop======" );
            // verify that for-loop body is surrounded by braces
            Optional<Replacement> braceReplacement = surroundWithBraces( node.getStatement(), input );
            if( braceReplacement.isPresent() ) {
                // need to insert braces around for-loop body so don't bother processing
                // the rest of the for-loop until after the braces have been inserted
                addReplacement( braceReplacement.get() );
                forceRescan();
            } else {
                List<Tree> initializers = node.getInitializer().stream().map( Tree.class::cast ).collect( Collectors.toList() );
                List<Tree> updates = node.getUpdate().stream().map( Tree.class::cast ).collect( Collectors.toList() );

                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "ForLoop" )
                        .append( TokenType.FOR )
                        .append( padding.methodName )
                        .append( TokenType.LEFT_PAREN )
                        .append( padding.methodArg )
                        .appendList( initializers, SPACE )
                        .append( TokenType.SEMICOLON )
                        .append( SPACE )
                        .append( node.getCondition() )
                        .append( TokenType.SEMICOLON )
                        .append( SPACE )
                        .appendList( updates, SPACE )
                        .append( padding.methodArg )
                        .append( TokenType.RIGHT_PAREN )
                        .appendOpeningBrace( cuddleBraces )
                        .appendBracedBlock( node.getStatement(), input.newline )
                        .append( TokenType.RIGHT_BRACE );

                if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
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
            Optional<Replacement> braceReplacement = surroundWithBraces( node.getThenStatement(), input );
            if( braceReplacement.isPresent() ) {
                // need to insert braces around if-block body so don't bother processing
                // the rest of the if-block until after the braces have been inserted
                addReplacement( braceReplacement.get() );
                forceRescan();
            } else {
                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "If" )
                        .append( TokenType.IF )
                        .append( padding.methodName );

                // insert appropriate padding inside of parentheses
                padParentheses( node.getCondition(), input, padding.methodArg, replacement );

                replacement.appendOpeningBrace( cuddleBraces )
                        .appendBracedBlock( node.getThenStatement(), input.newline )
                        .append( TokenType.RIGHT_BRACE );

                StatementTree elseStatement = node.getElseStatement();
                if( elseStatement != null ) {
                    replacement.append( cuddleBraces ? SPACE : input.newline )
                            .append( TokenType.ELSE );

                    if( elseStatement.getKind() == Kind.IF ) {
                        // this is an else-if block
                        replacement.append( SPACE )
                                .append( elseStatement );
                    } else {
                        // this is just an else block
                        replacement.appendOpeningBrace( cuddleBraces )
                            .appendBracedBlock( elseStatement, input.newline )
                            .append( TokenType.RIGHT_BRACE );
                    }
                }

                if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            }

            return super.visitIf( node, input );
        }

        @Override
        public Void visitImport(ImportTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitImport======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Import" )
                    .append( TokenType.IMPORT )
                    .append( SPACE );

            if( node.isStatic() ) {
                replacement.append( TokenType.STATIC )
                        .append( SPACE );
            }

            replacement.append( node.getQualifiedIdentifier() )
                    .append( TokenType.SEMICOLON );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitImport( node, input );
        }

        @Override
        public Void visitInstanceOf(InstanceOfTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitInstanceOf======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "InstanceOf" )
                    .append( node.getExpression() )
                    .append( SPACE )
                    .append( TokenType.INSTANCE_OF )
                    .append( SPACE )
                    .append( node.getType() );
            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );

            return super.visitInstanceOf( node, input );
        }

        @Override
        public Void visitLabeledStatement(LabeledStatementTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitLabeledStatement======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "LabeledStatement" )
                    .append( node.getLabel().toString() )
                    .append( TokenType.COLON )
                    .append( input.newline )
                    .append( node.getStatement() );
            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );

            return super.visitLabeledStatement( node, input );
        }

        @Override
        public Void visitLambdaExpression(LambdaExpressionTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitLambdaExpression======" );
            // Statement lambdas don't appear to be parsed correctly, so we only handle expression lambdas
            if( node.getBodyKind() == BodyKind.EXPRESSION ) {
                List<Tree> params =
                        node.getParameters().stream().map( Tree.class::cast ).collect( Collectors.toList() );

                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "LambdaExpression" );

                if( node.getParameters().size() != 1 ) {
                    replacement.append(TokenType.LEFT_PAREN)
                            .append(padding.methodArg);
                }
                replacement.appendList( params, TokenType.COMMA, true );
                if( node.getParameters().size() != 1 ) {
                    replacement.append(padding.methodArg)
                            .append(TokenType.RIGHT_PAREN);
                }

                replacement.append( SPACE )
                        .append( TokenType.ARROW )
                        .append( SPACE )
                        .append( node.getBody() );
                if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
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
            Object value = node.getValue();
            if( value != null && value.toString().startsWith( "-" ) ) {
                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Literal" )
                        .append( node.getValue().toString() );
                if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            }

            return super.visitLiteral( node, input );
        }

        @Override
        public Void visitMemberReference(MemberReferenceTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitMemberReference======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "MemberReference" )
                    .append( node.getQualifierExpression() )
                    .append( TokenType.REFERENCE );

            if( node.getMode() == ReferenceMode.NEW ) {
                replacement.append( TokenType.NEW );
            } else {
                // we need to manually append comments between reference token and name
                int end = input.getLastTokenIndex( node );
                int reference = input.findPrev( end, TokenType.REFERENCE ).getAsInt();
                int name = input.findNextByExclusion( reference, WS_NEWLINE_OR_COMMENT ).getAsInt();
                replacement.appendComments( name )
                        .append( node.getName().toString() );
            }

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitMemberReference( node, input );
        }

        @Override
        public Void visitMemberSelect( MemberSelectTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitMemberSelect======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "MemberSelect" )
                    .append( node.getExpression() )
                    .append( TokenType.DOT );
            //TODO what if type parameter appears after dot?

            // we need to manually append comments between reference token and name
            int exprEnd = input.getLastTokenIndex( node.getExpression() );
            int dot = input.findNext( exprEnd, TokenType.DOT ).getAsInt();
            int identifier = input.findNextByExclusion( dot, WS_NEWLINE_OR_COMMENT ).getAsInt();
            replacement.appendComments( identifier )
                    .append( node.getIdentifier().toString() );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitMemberSelect( node, input );
        }

        @Override
        public Void visitMethod( MethodTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitMethod======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Method" );

            if( isValid( node.getModifiers() ) ) {
                // append annotations, separated by newlines, and then any flags
                ModifierFormatter.appendAnnotationsAndFlags(node.getModifiers(), input, replacement, true);
            }

            List<Tree> typeParams = node.getTypeParameters().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            if( !typeParams.isEmpty() ) {
                replacement.append( TokenType.LESS_THAN )
                        .append( padding.typeParam )
                        .appendList( typeParams, TokenType.COMMA, true )
                        .append( padding.typeParam )
                        .append( TokenType.GREATER_THAN )
                        .append( SPACE );
            }

            if( node.getReturnType() != null ) {
                replacement.append( node.getReturnType() )
                        .append( SPACE );
            }

            String name = node.getName().toString();
            if( name.equals( "<init>" ) ) {
                // this is a constructor so we have to manually find the method name
                int scanPos = replacement.getCurrentPosInclusive();
                int nameIdx = input.findNextByExclusion( scanPos, WS_NEWLINE_OR_COMMENT ).getAsInt();
                name = input.tokens.get( nameIdx ).toString();
            }
            replacement.append( name )
                    .append( padding.methodName )
                    .append( TokenType.LEFT_PAREN );

            List<Tree> params = node.getParameters().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            if( !params.isEmpty() ) {
                replacement.append( padding.methodArg )
                        .appendList( params, TokenType.COMMA, true )
                        .append( padding.methodArg );
            }
            replacement.append( TokenType.RIGHT_PAREN );

            List<Tree> throwsArgs = node.getThrows().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            if( !throwsArgs.isEmpty() ) {
                replacement.append( SPACE )
                        .append( TokenType.THROWS )
                        .append( SPACE )
                        .appendList( throwsArgs, TokenType.COMMA, true );
            }

            if( node.getDefaultValue() != null ) {
                replacement.append( SPACE )
                        .append( TokenType.DEFAULT )
                        .append( SPACE )
                        .append( node.getDefaultValue() );
            }

            if( node.getBody() == null ) {
                replacement.append( TokenType.SEMICOLON );
            } else {
                replacement.appendOpeningBrace( cuddleBraces )
                        .appendBracedBlock( node.getBody(), input.newline )
                        .append( TokenType.RIGHT_BRACE );
            }

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitMethod( node, input );
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitMethodInvocation======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "MethodInvocation" )
                    .append( node.getMethodSelect() )
                    .append( padding.methodName )
                    .append( TokenType.LEFT_PAREN );

            List<Tree> args = node.getArguments().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            if( !args.isEmpty() ) {
                replacement.append( padding.methodArg )
                        .appendList( args, TokenType.COMMA, true )
                        .append( padding.methodArg );
            }

            replacement.append( TokenType.RIGHT_PAREN );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitMethodInvocation( node, input );
        }

        @Override
        public Void visitNewArray( NewArrayTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitNewArray======" );
            //TODO what about top-level annotations?
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "NewArray");

            if( node.getInitializers() != null ) {
                List<Tree> initializers = node.getInitializers().stream()
                        .map( Tree.class::cast )
                        .collect( Collectors.toList() );

                replacement.append( TokenType.LEFT_BRACE )
                        .append( padding.array )
                        .appendList( initializers, TokenType.COMMA, true )
                        .append( padding.array )
                        .append( TokenType.RIGHT_BRACE );
            } else {
                replacement.append( TokenType.NEW )
                        .append( SPACE )
                        .append( node.getType() );

                Iterator<? extends List<? extends AnnotationTree>> dimAnnos = node.getDimAnnotations().iterator();
                Iterator<? extends ExpressionTree> dims = node.getDimensions().iterator();
                while( dimAnnos.hasNext() && dims.hasNext() ) {
                    List<Tree> annos = dimAnnos.next().stream().map( Tree.class::cast ).collect( Collectors.toList() );
                    if( !annos.isEmpty() ) {
                        replacement.append( SPACE )
                                .appendList( annos, SPACE )
                                .append( SPACE );
                    }

                    replacement.append( TokenType.LEFT_BRACKET )
                            .append( padding.array )
                            .append( dims.next() )
                            .append( padding.array )
                            .append( TokenType.RIGHT_BRACKET );
                }
            }

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitNewArray( node, input );
        }

        @Override
        public Void visitNewClass( NewClassTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitNewClass======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "NewClass" )
                    .append( TokenType.NEW )
                    .append( SPACE )
                    .append( node.getIdentifier() )
                    .append( TokenType.LEFT_PAREN );

            List<Tree> args = node.getArguments().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            if( !args.isEmpty() ) {
                replacement.append( padding.methodArg )
                        .appendList( args, TokenType.COMMA, true )
                        .append( padding.methodArg );
            }
            replacement.append( TokenType.RIGHT_PAREN );

            if( node.getClassBody() != null ) {
                replacement.appendOpeningBrace( cuddleBraces )
                        .appendBracedBlock( node.getClassBody(), input.newline )
                        .append( TokenType.RIGHT_BRACE );
            }

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
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
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "ParameterizedType" )
                    .append( node.getType() )
                    .append( TokenType.LESS_THAN );

            List<Tree> args = node.getTypeArguments().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            if( !args.isEmpty() ) {
                replacement.append( padding.typeParam )
                        .appendList( args, TokenType.COMMA );
            }
            replacement.append( TokenType.GREATER_THAN );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitParameterizedType( node, input );
        }

//        @Override
//        public Void visitPrimitiveType( PrimitiveTypeTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitPrimitiveType======" );
//            return super.visitPrimitiveType( node, input );
//        }

        @Override
        public Void visitReturn( ReturnTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitReturn======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Return" )
                    .append( TokenType.RETURN )
                    .append( SPACE )
                    .append( node.getExpression() )
                    .append( TokenType.SEMICOLON );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitReturn( node, input );
        }

        @Override
        public Void visitSwitch( SwitchTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitSwitch======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Switch" )
                    .append( TokenType.SWITCH )
                    .append( padding.methodName );

            // insert appropriate padding inside of parentheses
            padParentheses( node.getExpression(), input, padding.methodArg, replacement );

            replacement.appendOpeningBrace( cuddleBraces )
                    .appendList( node.getCases(), TokenType.NEWLINE )
                    .appendWithLeadingNewlines( TokenType.RIGHT_BRACE, 1 );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitSwitch( node, input );
        }

        @Override
        public Void visitSynchronized( SynchronizedTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitSynchronized======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "ArrayType" )
                    .append( TokenType.SYNCHRONIZED )
                    .append( padding.methodName );

            // insert appropriate padding inside of parentheses
            padParentheses( node.getExpression(), input, padding.methodArg, replacement );

            replacement.appendOpeningBrace( cuddleBraces )
                    .appendBracedBlock( node.getBlock(), input.newline )
                    .append( TokenType.RIGHT_BRACE );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitSynchronized( node, input );
        }

        @Override
        public Void visitThrow( ThrowTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitThrow======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Throw" )
                    .append( TokenType.THROW )
                    .append( SPACE )
                    .append( node.getExpression() )
                    .append( TokenType.SEMICOLON );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitThrow( node, input );
        }

        @Override
        public Void visitTry( TryTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitTry======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Try" )
                    .append( TokenType.TRY );

            List<Tree> resources = node.getResources().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            if( resources != null && !resources.isEmpty() ) {
                replacement.append( padding.methodName )
                        .append( TokenType.LEFT_PAREN )
                        .append( padding.methodArg )
                        .appendList( resources, TokenType.SEMICOLON )
                        .append( padding.methodArg )
                        .append( TokenType.RIGHT_PAREN );
            }

            List<Tree> catches = node.getCatches().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            replacement.appendOpeningBrace( cuddleBraces )
                    .appendBracedBlock( node.getBlock(), input.newline )
                    .append( TokenType.RIGHT_BRACE )
                    .append( cuddleBraces ? SPACE : input.newline )
                    .appendList( catches, SPACE );

            if( isValid( node.getFinallyBlock() ) ) {
                replacement.append( cuddleBraces ? SPACE : input.newline )
                        .append( TokenType.FINALLY )
                        .appendOpeningBrace( cuddleBraces )
                        .appendBracedBlock( node.getFinallyBlock(), input.newline )
                        .append( TokenType.RIGHT_BRACE );
            }

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitTry( node, input );
        }

        @Override
        public Void visitTypeCast( TypeCastTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitTypeCast======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "TypeCast" )
                    .append( TokenType.LEFT_PAREN )
                    .append( padding.typeCast )
                    .append( node.getType() )
                    .append( padding.typeCast )
                    .append( TokenType.RIGHT_PAREN )
                    .append( node.getExpression() );

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitTypeCast( node, input );
        }

        @Override
        public Void visitTypeParameter( TypeParameterTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitTypeParameter======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "TypeParameter" );

            List<Tree> annotations = node.getAnnotations().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            if( !annotations.isEmpty() ) {
                replacement.appendList( annotations, SPACE )
                        .append( SPACE );
            }

            replacement.append( node.getName().toString() );

            List<Tree> bounds = node.getBounds().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            if( !bounds.isEmpty() ) {
                replacement.append( SPACE )
                        .append( TokenType.EXTENDS )
                        .append( SPACE )
                        .appendList( bounds, TokenType.COMMA );
            }

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitTypeParameter( node, input );
        }

        @Override
        public Void visitUnionType(UnionTypeTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitUnionType======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "UnionType" );

            List<Tree> alternatives =
                    node.getTypeAlternatives().stream().map( Tree.class::cast ).collect( Collectors.toList() );
            if( !alternatives.isEmpty() ) {
                replacement.append( alternatives.get( 0 ) );
                for( int idx = 1; idx < alternatives.size(); idx++ ) {
                    replacement.append( SPACE )
                            .append( TokenType.OR )
                            .append( SPACE )
                            .append( alternatives.get( idx ) );
                }
            }

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitUnionType( node, input );
        }

        @Override
        public Void visitUnary(UnaryTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitUnary======" );
            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "Unary" );
            if( node.getKind() == Kind.POSTFIX_DECREMENT || node.getKind() == Kind.POSTFIX_INCREMENT ) {
                replacement.append( node.getExpression() )
                        .append( getOperator( node.getKind() ) );
            } else {
                replacement.append( getOperator( node.getKind() ) )
                        .append( node.getExpression() );
            }

            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            return super.visitUnary( node, input );
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree node, Input input ) {
            if( VERBOSE ) System.out.println( "======visitWhileLoop======" );
            // ensure that do-while body is surrounded by braces
            Optional<Replacement> braceReplacement = surroundWithBraces( node.getStatement(), input );
            if( braceReplacement.isPresent() ) {
                // need to insert braces around while body so don't bother processing
                // the rest of the while until after the braces have been inserted
                addReplacement( braceReplacement.get() );
                forceRescan();
            } else {
                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "WhileLoop" )
                        .append( TokenType.WHILE )
                        .append( padding.methodName )
                        .append( node.getCondition() )
                        .appendOpeningBrace( cuddleBraces )
                        .appendBracedBlock( node.getStatement(), input.newline )
                        .append( TokenType.RIGHT_BRACE );
                if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            }
            return super.visitWhileLoop( node, input );
        }

        private Optional<Replacement> surroundWithBraces( Tree tree, Input input ) {
            if( tree != null ) {
                // check if tree is already surrounded by curly braces
                int start = input.getFirstTokenIndex( tree );
                if( input.tokens.get( start ).getType() != TokenType.LEFT_BRACE ) {
                    // find parent of this tree (excluding whitespace, newlines, and comments)
                    int parent = input.findPrevByExclusion( start, WS_NEWLINE_OR_COMMENT ).getAsInt();

                    Replacement.Builder replacement = new Replacement.Builder( tree, input, NAME + "surroundWithBraces" )
                            .append( "{" )
                            .setCurrentPositionInclusive( parent + 1 ) // include any comments between parent and tree start
                            .append( tree )
                            .append( "}" );

                    int end = input.getLastTokenIndex( tree );
                    return replacement.build( (parent + 1), end );
                }
            }

            return Optional.empty();
        }

        private void padParentheses( Tree tree, Input input, String pad, Replacement.Builder replacement ) {
            int treeBegin = input.getFirstTokenIndex( tree );
            int treeEnd = input.getLastTokenIndex( tree );
            int start = input.findNextByExclusion( treeBegin, treeEnd, TokenType.LEFT_PAREN, TokenType.WHITESPACE )
                    .getAsInt();
            int stop = input.findPrevByExclusion( treeBegin, treeEnd, TokenType.RIGHT_PAREN, TokenType.WHITESPACE )
                    .getAsInt();

            replacement.append( TokenType.LEFT_PAREN )
                    .append( pad );
            IntStream.rangeClosed( start, stop ).forEach( replacement::append );
            replacement.append( pad )
                    .append( TokenType.RIGHT_PAREN );
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

        private void printTree( Tree tree, Input input ) {
            int startIdx = input.getFirstTokenIndex( tree );
            int endIdx = input.getLastTokenIndex( tree );
            for( int pos=startIdx; pos<endIdx; pos++ ) {
                System.out.println( pos + ": [" + input.tokens.get( pos ).toString() + "]" );
            }
        }

        private void printBeforeAfter( Tree tree, Input input, StringBuilder sb ) {
            System.out.println( "++++++++++++++++++++++" );
            System.out.println( "[" + input.stringifyTree( tree ) + "]" );
            System.out.println( "----------------------" );
            System.out.println( "[" + sb.toString() + "]");
            System.out.println( "++++++++++++++++++++++" );
        }

        private void printBeforeAfter( Tree tree, Input input, String newText ) {
            System.out.println( input.stringifyTree( tree ) );
            System.out.println( "----------------------" );
            System.out.println( newText );
        }
    }

}
