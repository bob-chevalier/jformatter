package com.staircaselabs.jformatter.formatters;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.Indent;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.LineBreak;
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
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalInt;
import java.util.function.Predicate;
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
        System.out.println( input.tokens.stream().map( TextToken::toMarkupString ).collect( Collectors.joining() ) );

        // group tokens into lines
        List<Line> lines = new ArrayList<>();
        List<TextToken> lineTokens = new ArrayList<>();
        int prevLineIndentLevel = 0;
        for( TextToken token : tokens ) {
            lineTokens.add( token );

            if( token.getType() == TokenType.NEWLINE ) {
                Line line = new Line( maxLineWidth, indent, prevLineIndentLevel, lineTokens );
                lines.add( line );
                prevLineIndentLevel += line.tokens.get( 0 ).getIndentOffset();
                lineTokens = new ArrayList<>();
            }
        }

        // insert additional line-breaks where necessary to enforce maximum line width
        ListIterator<Line> iter = lines.listIterator();
        while( iter.hasNext() ) {
            Line head = iter.next();
            while( head.needsLineBreak() ) {
                List<TextToken> tailTokens = head.truncate();
                if( !tailTokens.isEmpty() ) {
                    // insert a line-break where we truncated the original line
                    head.tokens.add( new TextToken( input.newline, TokenType.NEWLINE, 0, 0 ) );

                    // update the indentation of the first extracted token based on its break type
                    tailTokens.get( 0 ).updateIndentOffset( tailTokens.get( 0 ).getLineBreak().getIndentOffset() );

                    // create a new line from the tokens that were extracted from the original line
                    Line tail = new Line( maxLineWidth, indent, head.indentLevel, tailTokens );

                    // add the new line and then check whether it needs to be split again
                    iter.add( tail );
                    head = tail;
                } else {
                    // we couldn't find a good place to break the line so just let it be
                    break;
                }
            }
        }

        return lines.stream().map( Line::toString ).collect( Collectors.joining() );
    }

    private static class LineBreakScanner extends TreePathScanner<Void, Input> {

        @Override
        public Void visitArrayAccess(ArrayAccessTree node, Input input ) {
            if( node.getExpression().getKind() == Tree.Kind.IDENTIFIER ) {
//                System.out.println( "BFC IDENTIFIER" );
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
            input.getFirstToken(node.getExpression()).setLineBreakTag( LineBreak.INDEPENDENT, "visitAssignment" );
//            int variableEnd = input.getLastTokenIndex( node.getVariable() );
//            int exprBegin = input.getFirstTokenIndex( node.getExpression() );
//            int assignment = input.findNext( variableEnd, exprBegin, TokenType.ASSIGNMENT )
//                    .orElseThrow( () -> new RuntimeException( "Missing expected = sign." ) );
//            input.tokens.get( assignment ).setBreakType( BreakType.INDEPENDENT, "visitAssignment" );

            return super.visitAssignment(node, input);
        }


        @Override
        public Void visitBinary(BinaryTree node, Input input ) {
            int left = input.getLastTokenIndex( node.getLeftOperand() );
            int right = input.getFirstTokenIndex( node.getRightOperand() );
            int operator = input.findNext( left, right, TokenUtils.tokenTypeFromBinaryOperator( node.getKind() ) )
                    .orElseThrow( () -> new RuntimeException( "Missing binary operator." ) );
            input.tokens.get( operator ).setLineBreakTag( LineBreak.INDEPENDENT, "visitBinary" );

            return super.visitBinary( node, input );
        }

        @Override
        public Void visitClass(ClassTree node, Input input) {
            MarkupTool markup = new MarkupTool(node, input);
            if (node.getExtendsClause() != null) {
                markup.tagIndependentBreak(TokenType.EXTENDS, "visitClass");
            }

            if (!node.getImplementsClause().isEmpty()) {
                markup.tagIndependentBreak(TokenType.IMPLEMENTS, "visitClass");
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

            // all a line-break before the question mark
            int question = input.findNext( condition, trueExprBegin, TokenType.QUESTION )
                    .orElseThrow( () -> new RuntimeException( "Missing expected ?." ) );
            input.tokens.get( question ).setLineBreakTag( LineBreak.UNIFIED_FIRST, "visitConditionalExpr" );

            // ensure that if a line-break is inserted before question mark, one is also inserted before colon
            int colon = input.findNext( trueExprEnd, falseExpr, TokenType.COLON )
                    .orElseThrow( () -> new RuntimeException( "Missing expected :." ) );
            input.tokens.get( colon ).setLineBreakTag( LineBreak.UNIFIED_LAST, "visitConditionalExpr" );

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
                System.out.println( "BFC Lambda expression" );
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
            if( node.getExpression().getKind() == Tree.Kind.METHOD_INVOCATION ) {
                int exprEnd = input.getLastTokenIndex( node.getExpression() );
                int dot = input.findNext( exprEnd, input.getLastTokenIndex( node ), TokenType.DOT )
                        .orElseThrow( () -> new RuntimeException( "Missing dot." ) );
                input.tokens.get( dot ).setLineBreakTag( LineBreak.UNIFIED, "visitMemberSelect" );
            }

            return super.visitMemberSelect( node, input );
        }

        @Override
        public Void visitMethod(MethodTree node, Input input) {
//            if( VERBOSE ) System.out.println( "======visitMethod======" );
            MarkupTool markup = new MarkupTool(node, input);
            markup.tagUnifiedUnjustifiedBreaks(node.getParameters(), TokenType.RIGHT_PAREN, "visitMethod");
            if (!node.getThrows().isEmpty()) {
                markup.tagIndependentBreak(TokenType.THROWS, "visitMethod");
                markup.tagUnifiedBreaks(node.getThrows(), "visitMethod");
            }

            markup.indentBracedBlock(node.getBody());

            return super.visitMethod(node, input);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Input input) {
//            if( VERBOSE ) System.out.println( "======visitMethodInvocation======" );
            MarkupTool markup = new MarkupTool(node, input);
            markup.tagUnifiedUnjustifiedBreaks(node.getArguments(), TokenType.RIGHT_PAREN, "visitMethodInvocation" );

//            ExpressionTree ms = node.getMethodSelect();
//            System.out.println( "BFC k: " + ms.getKind() + ", ms: [" + input.stringifyTree( node.getMethodSelect() ) + "]" );

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

//        @Override
//        public Void visitNewClass( NewClassTree node, Input input ) {
//            if( VERBOSE ) System.out.println( "======visitNewClass======" );
//            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "NewClass" )
//                    .append( TokenType.NEW )
//                    .append( SPACE )
//                    .append( node.getIdentifier() )
//                    .append( TokenType.LEFT_PAREN );
//
//            List<Tree> args = node.getArguments().stream().map( Tree.class::cast ).collect( Collectors.toList() );
//            if( !args.isEmpty() ) {
//                replacement.append( padding.methodArg )
//                        .appendList( args, TokenType.COMMA, true )
//                        .append( padding.methodArg );
//            }
//            replacement.append( TokenType.RIGHT_PAREN );
//
//            if( node.getClassBody() != null ) {
//                replacement.appendOpeningBrace( cuddleBraces )
//                        .appendBracedBlock( node.getClassBody(), input.newline )
//                        .append( TokenType.RIGHT_BRACE );
//            }
//
//            if( ENABLED ) replacement.build().ifPresent( this::addReplacement );
//            return super.visitNewClass( node, input );
//        }

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
                markup.tagUnifiedUnjustifiedBreaks(node.getResources(), TokenType.RIGHT_PAREN, "visitTry");
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
                markup.tagIndependentBreak(node.getInitializer(), "visitVariable");
//                int initializerBegin = input.getFirstTokenIndex( node.getInitializer() );
//                int assignment = input.findPrev( input.getFirstTokenIndex( node ), initializerBegin, TokenType.ASSIGNMENT )
//                        .orElseThrow( () -> new RuntimeException( "Missing expected = sign." ) );
//                input.tokens.get( assignment ).setBreakType( BreakType.INDEPENDENT, "visitVariable" );
            }

            return super.visitVariable(node, input);
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree node, Input input) {
            new MarkupTool( node, input ).indentBracedBlock( node.getStatement() );
            return super.visitWhileLoop(node, input);
        }

    }

    private static class Line {

        private final int maxWidth;
        private final Indent indent;
        private int indentLevel;
        private Predicate<LineBreak> isValidBreakPoint;
        private List<TextToken> tokens;

        public Line( int maxWidth, Indent indent, int parentIndentLevel, List<TextToken> tokens ) {
            this.maxWidth = maxWidth;
            this.indent = indent;
            this.tokens = tokens;

            TextToken firstToken = tokens.get( 0 );
            indentLevel = parentIndentLevel + firstToken.getIndentOffset();
            isValidBreakPoint = LineBreak.getValidBreakPredicate( firstToken.getLineBreak() );
        }

        public boolean needsLineBreak() {
            return isPartOfUnifiedGroup() || getWidth() > maxWidth;
        }

        public List<TextToken> truncate() {
            List<TextToken> tokensAfterBreak = new ArrayList<>();

            // skip the first token
            Iterator<TextToken> iter = tokens.listIterator( 1 );
            boolean foundValidBreakPoint = false;
            while( iter.hasNext() ) {
                TextToken token = iter.next();
                if( foundValidBreakPoint || isValidBreakPoint.test( token.getLineBreak() ) ) {
                    tokensAfterBreak.add( token );
                    iter.remove();
                    foundValidBreakPoint = true;
                }
            }

            return tokensAfterBreak;
        }

        @Override
        public String toString() {
            String indentString = getTextWidth() == 0 ? "" : indent.getText( indentLevel );
            return indentString + tokens.stream().map( TextToken::toString ).collect( Collectors.joining() );
        }

        private boolean isPartOfUnifiedGroup() {
            LineBreak firstTokenBreakType = tokens.get( 0 ).getLineBreak();
            return firstTokenBreakType == LineBreak.UNIFIED_FIRST || firstTokenBreakType == LineBreak.UNIFIED;
        }

        private int getWidth() {
            int textWidth = getTextWidth();
            return textWidth == 0 ? 0 : textWidth + indent.getWidth( indentLevel );
        }

        private int getTextWidth() {
            return tokens.stream().mapToInt( TextToken::getWidth ).sum();
        }

    }

}
