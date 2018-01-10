package com.staircaselabs.jformatter.formatters;

import com.staircaselabs.jformatter.core.BreakType;
import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.Indent;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.Line;
import com.staircaselabs.jformatter.core.MarkupTool;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.staircaselabs.jformatter.core.TokenUtils;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.stream.Collectors;

import static com.staircaselabs.jformatter.core.CompilationUnitUtils.getCompilationUnit;
import static com.staircaselabs.jformatter.core.CompilationUnitUtils.isValid;
import static com.staircaselabs.jformatter.core.TokenUtils.tokenizeText;

public class LineBreakFormatter {

    private static final boolean VERBOSE = false;
    private static final boolean ENABLED = true;
    private final Indent indent;
    private final int maxLineWidth;

    public LineBreakFormatter( Indent indent, int maxLineWidth ) {
        this.indent = indent;
        this.maxLineWidth = maxLineWidth;
    }

    public String format( String text ) throws FormatException {
        List<TextToken> tokens = tokenizeText( text );
        JCCompilationUnit unit = getCompilationUnit( text );
        Input input = new Input( tokens, unit.endPositions );

        // markup tokens with line-break options and indentation levels
        LineBreakScanner lineBreakScanner = new LineBreakScanner();
        lineBreakScanner.scan( unit, input );

        // group tokens into lines
        List<Line> lines = new ArrayList<>();
        Queue<TextToken> lineTokens = new LinkedList<>();
        int prevLineIndentLevel = 0;
        for( TextToken token : tokens ) {
            lineTokens.add( token );

            if( token.getType() == TokenType.NEWLINE ) {
                Line line = new Line( indent, prevLineIndentLevel, lineTokens );
                lines.add( line );
                prevLineIndentLevel = line.getIndentLevel();
                lineTokens = new LinkedList<>();
            }
        }

//        lines.forEach( Line::printMarkup );
//        lines.get( 3 ).writeDotFile( "/Users/rchevalier/bob.dot" );

        // insert additional line-breaks where necessary to enforce maximum line width
        ListIterator<Line> iter = lines.listIterator();
        while( iter.hasNext() ) {
            Line line = iter.next();
            Deque<Line> extraLines = new ArrayDeque<>();

            // if necessary, wrap line to ensure that it fits within max line width
            while( line.getWidth() > maxLineWidth && line.canBeSplit() ) {
                // add additional lines in reverse order to the front of the extra lines deque
                line.wrap( input.newline ).descendingIterator().forEachRemaining( extraLines::addFirst );
            }

            // recursively expand any extra lines that were generated from original line
            Line extraLine = extraLines.pollFirst();
            while( extraLine != null ) {
                while( extraLine.getWidth() > maxLineWidth && extraLine.canBeSplit() ) {
                    // add additional lines in reverse order to the front of the extra lines deque
                    extraLine.wrap( input.newline ).descendingIterator().forEachRemaining( extraLines::addFirst );
                }

                // head of extra line is now either sufficiently trimmed or at least as small as it's going to get
                iter.add( extraLine );
                extraLine = extraLines.pollFirst();
            }
        }

        return lines.stream().map( Line::toString ).collect( Collectors.joining() );
    }

    private static class LineBreakScanner extends TreePathScanner<Void, Input> {

        @Override
        public Void visitArrayAccess(ArrayAccessTree node, Input input ) {
            if( node.getExpression().getKind() == Tree.Kind.IDENTIFIER ) {
//                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "ArrayAccess" )
//                        .append( node.getExpression() )
//                        .append( TokenType.LEFT_BRACKET )
//                        .append( padding.array )
//                        .append( node.getIndex() )
//                        .append( padding.array )
//                        .append( TokenType.RIGHT_BRACKET );
//
//                int afterRightBracket = replacement.getCurrentPosInclusive();
//                int start = input.getFirstTokenIndex( node );
//                if( ENABLED ) replacement.build( start, afterRightBracket ).ifPresent( this::addReplacement );
            } else {
//                System.out.println( "BFC NOT IDENTIFIER" );
//                int arrayIndex = input.getFirstTokenIndex( node.getIndex() );
//                int prevRightBracket = input.findPrev( arrayIndex, TokenType.RIGHT_BRACKET ).getAsInt();
//                int end = input.getLastTokenIndex( node );
//
//                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "ArrayAccess" )
//                        .setCurrentPositionInclusive( prevRightBracket + 1 )
//                        .append( TokenType.LEFT_BRACKET )
//                        .append( padding.array )
//                        .append( node.getIndex() )
//                        .append( padding.array )
//                        .append( TokenType.RIGHT_BRACKET );
//                if( ENABLED ) replacement.build( (prevRightBracket + 1), end ).ifPresent( this::addReplacement );
            }

            return super.visitArrayAccess( node, input );
        }

//        @Override
//        public Void visitArrayType( ArrayTypeTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitArrayType======" );
//            int start = input.getFirstTokenIndex( node );
//            int end = input.getLastTokenIndex( node );
//            int pos = input.findNext( start, WS_NEWLINE_COMMENT_OR_BRACKET ).getAsInt();
//
//            // append array type
//            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "ArrayType" );
//            replacement.append( input.stringifyTokens( start, pos ) );
//
//            OptionalInt leftBracket = input.findNext( pos, TokenType.LEFT_BRACKET );
//            while( leftBracket.isPresent() ) {
//                // append any annotations
//                int leftBracketIdx = leftBracket.getAsInt();
//                OptionalInt annotationStart = input.findNext( pos, leftBracketIdx, TokenType.AT );
//                if( annotationStart.isPresent() ) {
//                    int annotationEnd = input.findPrevByExclusion( leftBracketIdx, WS_NEWLINE_OR_COMMENT ).getAsInt();
//                    replacement.append( SPACE )
//                            .append( input.stringifyTokens( annotationStart.getAsInt(), (annotationEnd + 1) ) )
//                            .append( SPACE )
//                            .setCurrentPositionInclusive( annotationEnd + 1 );
//                }
//
//                // append brackets
//                replacement.append( TokenType.LEFT_BRACKET ).append( TokenType.RIGHT_BRACKET );
//                pos = replacement.getCurrentPosInclusive();
//                leftBracket = input.findNext( pos, end, TokenType.LEFT_BRACKET );
//            }
//
//            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
//            return super.visitArrayType( node, input );
//        }

        @Override
        public Void visitAssignment(AssignmentTree node, Input input) {
            new MarkupTool( node, input ).tagLineBreak(BreakType.ASSIGNMENT, node.getExpression(), "visitAssignment");
            return super.visitAssignment(node, input);
        }


        @Override
        public Void visitBinary(BinaryTree node, Input input ) {
            int left = input.getLastTokenIndex( node.getLeftOperand() );
            int right = input.getFirstTokenIndex( node.getRightOperand() );
            int operator = input.findNext( left, right, TokenUtils.tokenTypeFromBinaryOperator( node.getKind() ) )
                    .orElseThrow( () -> new RuntimeException( "Missing binary operator." ) );
            input.tokens.get( operator ).setLineBreakTag( BreakType.ASSIGNMENT, "visitBinary" );

            return super.visitBinary( node, input );
        }

        @Override
        public Void visitClass(ClassTree node, Input input) {
            MarkupTool markup = new MarkupTool(node, input);
            if (node.getExtendsClause() != null) {
                markup.tagLineBreak(BreakType.EXTENDS, TokenType.EXTENDS, "visitClass");
            }

            if (!node.getImplementsClause().isEmpty()) {
                markup.tagLineBreak(BreakType.IMPLEMENTS, TokenType.IMPLEMENTS, "visitClass");
            }

            if (!node.getMembers().isEmpty()) {
                int classStart = input.getFirstTokenIndex(node);
                int classEnd = input.getLastTokenIndex(node);

                // find opening brace
                int firstMember = input.getFirstTokenIndex(node.getMembers().get(0));
                int leftBrace = input.findPrev(classStart, firstMember, TokenType.LEFT_BRACE)
                        .orElseThrow(() -> new RuntimeException("Unexpected missing token."));

                // find closing brace
                int rightBrace = input.findPrev(leftBrace, classEnd, TokenType.RIGHT_BRACE)
                        .orElseThrow(() -> new RuntimeException("Unexpected missing token."));

                // find anything between opening and closing braces besides newlines and whitespace
                OptionalInt afterLeftBrace =
                        input.findNextByExclusion(leftBrace + 1, rightBrace, TokenType.WHITESPACE, TokenType.NEWLINE);

                if (afterLeftBrace.isPresent()) {
                    input.tokens.get(afterLeftBrace.getAsInt()).updateIndentOffset(1);
                    input.tokens.get(rightBrace).updateIndentOffset(-1);
                }
            }

            return super.visitClass(node, input);
        }

        @Override
        public Void visitConditionalExpression(ConditionalExpressionTree node, Input input ) {
            int condition = input.getLastTokenIndex( node.getCondition() );
            int trueExprBegin = input.getFirstTokenIndex( node.getTrueExpression() );
            int trueExprEnd = input.getLastTokenIndex( node.getTrueExpression() );
            int falseExpr = input.getFirstTokenIndex( node.getFalseExpression() );

            // add a line-break before the question mark
            int question = input.findNext( condition, trueExprBegin, TokenType.QUESTION )
                    .orElseThrow( () -> new RuntimeException( "Missing expected ?." ) );
            input.tokens.get( question ).setLineBreakTag( BreakType.TERNARY, "visitConditionalExpr" );

            // ensure that if a line-break is inserted before question mark, one is also inserted before colon
            int colon = input.findNext( trueExprEnd, falseExpr, TokenType.COLON )
                    .orElseThrow( () -> new RuntimeException( "Missing expected :." ) );
            input.tokens.get( colon ).setLineBreakTag( BreakType.TERNARY, "visitConditionalExpr" );

            return super.visitConditionalExpression( node, input );
        }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree node, Input input) {
            new MarkupTool(node, input).indentBracedBlock(node.getStatement());
            return super.visitDoWhileLoop(node, input);
        }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree node, Input input) {
            new MarkupTool(node, input).indentBracedBlock(node.getStatement());
            return super.visitEnhancedForLoop(node, input);
        }

        @Override
        public Void visitForLoop(ForLoopTree node, Input input) {
            new MarkupTool(node, input).indentBracedBlock(node.getStatement());
            return super.visitForLoop(node, input);
        }

        @Override
        public Void visitIf(IfTree node, Input input) {
            MarkupTool markup = new MarkupTool(node, input);
            markup.indentBracedBlock(node.getThenStatement());

            // check for else-block
            if (node.getElseStatement() != null && node.getElseStatement().getKind() != Tree.Kind.IF) {
                markup.indentBracedBlock(node.getElseStatement());
            }

            return super.visitIf(node, input);
        }

        @Override
        public Void visitLambdaExpression(LambdaExpressionTree node, Input input ) {
            // Statement lambdas don't appear to be parsed correctly, so we only handle expression lambdas
            if( node.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION ) {
//                List<Tree> params =
//                        node.getParameters().stream().map( Tree.class::cast ).collect( Collectors.toList() );
//
//                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "LambdaExpression" )
//                        .append( TokenType.LEFT_PAREN )
//                        .append( padding.methodArg )
//                        .appendList( params, TokenType.COMMA, true )
//                        .append( padding.methodArg )
//                        .append( TokenType.RIGHT_PAREN )
//                        .append( SPACE )
//                        .append( TokenType.ARROW )
//                        .append( SPACE )
//                        .append( node.getBody() );
//                if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
            }

            return super.visitLambdaExpression( node, input );
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, Input input ) {
            if( node.getExpression().getKind() != Tree.Kind.IDENTIFIER ) {
                int exprEnd = input.getLastTokenIndex( node.getExpression() );
                int dot = input.findNext( exprEnd, input.getLastTokenIndex( node ), TokenType.DOT )
                        .orElseThrow( () -> new RuntimeException( "Missing dot." ) );
                input.tokens.get( dot ).setLineBreakTag( BreakType.METHOD_INVOCATION, "visitMemberSelect" );
            }

            return super.visitMemberSelect( node, input );
        }

        @Override
        public Void visitMethod(MethodTree node, Input input) {
            MarkupTool markup = new MarkupTool(node, input);
            markup.tagLineBreaks(BreakType.METHOD_ARG, node.getParameters(), "visitMethod");
            if (!node.getThrows().isEmpty()) {
                markup.tagLineBreak( BreakType.THROWS, TokenType.THROWS, "visitMethod");
                markup.tagLineBreaks(BreakType.UNBOUND_LIST_ITEM, node.getThrows(), "visitMethod");
            }

            markup.indentBracedBlock(node.getBody());

            return super.visitMethod(node, input);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Input input) {
            MarkupTool markup = new MarkupTool(node, input);
            markup.tagLineBreaks(BreakType.METHOD_ARG, node.getArguments(), "visitMethodInvocation" );

            return super.visitMethodInvocation(node, input);
        }

//        @Override
//        public Void visitNewArray( NewArrayTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitNewArray======" );
//            //TODO what about top-level annotations?
//            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "NewArray");
//
//            if( node.getInitializers() != null ) {
//                List<Tree> initializers = node.getInitializers().stream()
//                        .map( Tree.class::cast )
//                        .collect( Collectors.toList() );
//
//                replacement.append( TokenType.LEFT_BRACE )
//                        .append( padding.array )
//                        .appendList( initializers, TokenType.COMMA, true )
//                        .append( padding.array )
//                        .append( TokenType.RIGHT_BRACE );
//            } else {
//                replacement.append( TokenType.NEW )
//                        .append( SPACE )
//                        .append( node.getType() );
//
//                Iterator<? extends List<? extends AnnotationTree>> dimAnnos = node.getDimAnnotations().iterator();
//                Iterator<? extends ExpressionTree> dims = node.getDimensions().iterator();
//                while( dimAnnos.hasNext() && dims.hasNext() ) {
//                    List<Tree> annos = dimAnnos.next().stream().map( Tree.class::cast ).collect( Collectors.toList() );
//                    if( !annos.isEmpty() ) {
//                        replacement.append( SPACE )
//                                .appendList( annos, SPACE )
//                                .append( SPACE );
//                    }
//
//                    replacement.append( TokenType.LEFT_BRACKET )
//                            .append( padding.array )
//                            .append( dims.next() )
//                            .append( padding.array )
//                            .append( TokenType.RIGHT_BRACKET );
//                }
//            }
//
//            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
//            return super.visitNewArray( node, input );
//        }

        @Override
        public Void visitNewClass(NewClassTree node, Input input ) {
            MarkupTool markup = new MarkupTool(node, input);
            markup.tagLineBreaks(BreakType.METHOD_ARG, node.getArguments(), "visitNewClass" );

            return super.visitNewClass( node, input );
        }

        @Override
        public Void visitSwitch(SwitchTree node, Input input) {
            MarkupTool markup = new MarkupTool(node, input);
            markup.indentBracedBlock(node);

            boolean prevHadStatements = false;
            for (CaseTree tree : node.getCases()) {
                int caseBegin = input.getFirstTokenIndex(tree);
                int caseEnd = input.getLastTokenIndex(tree);

                if (prevHadStatements) {
                    input.tokens.get(caseBegin).updateIndentOffset(-1);
                }

                // attempt to indent next line (will only happen if this case has statements)
                prevHadStatements = markup.indentNextLine(caseBegin, caseEnd);
            }

            if (prevHadStatements) {
                int lastStatementEnd = input.getLastTokenIndex(node.getCases().get(node.getCases().size() - 1));
                markup.unindentNextLine(lastStatementEnd, input.getLastTokenIndex(node));
            }

            return super.visitSwitch(node, input);
        }

//        @Override
//        public Void visitSynchronized( SynchronizedTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitSynchronized======" );
//            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "ArrayType" )
//                    .append( TokenType.SYNCHRONIZED )
//                    .append( padding.methodName );
//
//            // insert appropriate padding inside of parentheses
//            padParentheses( node.getExpression(), input, padding.methodArg, replacement );
//
//            replacement.appendOpeningBrace( cuddleBraces )
//                    .appendBracedBlock( node.getBlock(), input.newline )
//                    .append( TokenType.RIGHT_BRACE );
//
//            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
//            return super.visitSynchronized( node, input );
//        }

        @Override
        public Void visitTry(TryTree node, Input input) {
            MarkupTool markup = new MarkupTool(node, input);

            // allow line-breaks between resources
            if (node.getResources() != null && !node.getResources().isEmpty()) {
                markup.tagLineBreaks(BreakType.METHOD_ARG, node.getResources(), "visitTry");
            }

            // indent try-block
            markup.indentBracedBlock(node.getBlock());

            // indent each catch-block
            node.getCatches().stream().forEach(c -> markup.indentBracedBlock(c.getBlock()));

            // indent finally-block
            if (isValid(node.getFinallyBlock())) {
                markup.indentBracedBlock(node.getFinallyBlock());
            }

            return super.visitTry(node, input);
        }

//        @Override
//        public Void visitUnionType( UnionTypeTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitUnionType======" );
//            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "UnionType" );
//
//            List<Tree> alternatives =
//                    node.getTypeAlternatives().stream().map( Tree.class::cast ).collect( Collectors.toList() );
//            if( !alternatives.isEmpty() ) {
//                replacement.append( alternatives.get( 0 ) );
//                for( int idx = 1; idx < alternatives.size(); idx++ ) {
//                    replacement.append( SPACE )
//                            .append( TokenType.OR )
//                            .append( SPACE )
//                            .append( alternatives.get( idx ) );
//                }
//            }
//
//            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
//            return super.visitUnionType( node, input );
//        }

        @Override
        public Void visitVariable(VariableTree node, Input input) {
            if (isValid(node.getInitializer())) {
                MarkupTool markup = new MarkupTool(node, input);
                markup.tagLineBreak(BreakType.ASSIGNMENT, node.getInitializer(), "visitVariable");
            }

            return super.visitVariable(node, input);
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree node, Input input) {
            new MarkupTool( node, input ).indentBracedBlock( node.getStatement() );
            return super.visitWhileLoop(node, input);
        }

    }

}