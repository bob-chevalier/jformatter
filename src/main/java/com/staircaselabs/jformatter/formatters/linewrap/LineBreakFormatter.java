package com.staircaselabs.jformatter.formatters.linewrap;

import com.staircaselabs.jformatter.config.Config;
import com.staircaselabs.jformatter.core.Formatter;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.LineWrap;
import com.staircaselabs.jformatter.core.LineWrapPriority.Strategy;
import com.staircaselabs.jformatter.core.LineWrapTag;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.staircaselabs.jformatter.core.TokenUtils;
import com.staircaselabs.jformatter.debug.FormatException;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
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
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.staircaselabs.jformatter.core.CompilationUnitUtils.getCompilationUnit;
import static com.staircaselabs.jformatter.core.CompilationUnitUtils.isValid;
import static com.staircaselabs.jformatter.core.TokenUtils.tokenizeText;

public class LineBreakFormatter implements Formatter {

    private static final Logger log = Logger.getLogger( LineBreakFormatter.class.getName() );

    @Override
    public String format( String text ) throws FormatException {
        List<TextToken> tokens = tokenizeText( text );
        JCCompilationUnit unit = getCompilationUnit( text );
        Input input = new Input( tokens, unit.endPositions );

        // markup tokens with line-break options and indentation levels
        LineBreakScanner lineBreakScanner = new LineBreakScanner();
        lineBreakScanner.scan( unit, input );

        // group tokens into lines
        List<Line> lines = new ArrayList<>();
        List<TextToken> lineTokens = new ArrayList<>();
        int prevLineIndentLevel = 0;
        for( TextToken token : tokens ) {
            lineTokens.add( token );

            if( token.getType() == TokenType.NEWLINE ) {
                // if there are any member select line wrap tags, we need to group them
                groupMemberSelectTags( lineTokens );

                Line line = new Line( prevLineIndentLevel, lineTokens, input.newline, Strategy.PRIMARY );
                if( line.isWrapped() ) {
                    // line will be wrapped so see if we can do any better with another wrapping strategy
                    int wrapCount = line.getLineWrapCount();
                    Line alternate = new Line( prevLineIndentLevel, lineTokens, input.newline, Strategy.SECONDARY );
                    if( alternate.getLineWrapCount() < wrapCount ) {
                        line = alternate;
                    }
                }

                lines.add( line );
                prevLineIndentLevel = line.getIndentLevel();
                lineTokens = new LinkedList<>();
            }
        }

        // debugging
        log.fine( lines.stream().map( Line::getMarkupString ).collect( Collectors.joining() ) );
        //TODO make this type of debugging configurable
        lines.get( 3 ).writeDotFile( "/Users/rchevalier/bob.dot" );

        return lines.stream().map( Line::toString ).collect( Collectors.joining() );
    }

    private void groupMemberSelectTags( List<TextToken> tokens ) {
        int openParens = 0;
        Map<Integer, String> groups = new HashMap<>();
        for( TextToken token : tokens ) {
            openParens += token.getType() == TokenType.LEFT_PAREN ? 1 : 0;
            openParens += token.getType() == TokenType.RIGHT_PAREN ? -1 : 0;

            Optional<LineWrapTag> tag = token.getLineWrapTag();
            if( tag.isPresent()  && tag.get().getType() == LineWrap.MEMBER_SELECT) {
                if( !groups.containsKey( openParens ) ) {
                    groups.put( openParens, UUID.randomUUID().toString() );
                }
                tag.get().setGroupId( groups.get( openParens ) );
            }
        }
    }

    private static class LineBreakScanner extends TreePathScanner<Void, Input> {

        @Override
        public Void visitAssignment(AssignmentTree node, Input input) {
            new MarkupTool( node, input ).tagLineWrap(LineWrap.ASSIGNMENT, node.getExpression(), "visitAssignment");
            return super.visitAssignment(node, input);
        }


        @Override
        public Void visitBinary(BinaryTree node, Input input ) {
            int left = input.getLastTokenIndex( node.getLeftOperand() );
            int right = input.getFirstTokenIndex( node.getRightOperand() );
            int operator = input.findNext( left, right, TokenUtils.tokenTypeFromBinaryOperator( node.getKind() ) )
                    .orElseThrow( () -> new RuntimeException( "Missing binary operator." ) );
            input.tokens.get( operator ).allowLineWrap( new LineWrapTag( LineWrap.ASSIGNMENT, "visitBinary" ) );

            return super.visitBinary( node, input );
        }

        @Override
        public Void visitClass(ClassTree node, Input input) {
            MarkupTool markup = new MarkupTool(node, input);
            if (node.getExtendsClause() != null) {
                markup.tagLineWrap( LineWrap.EXTENDS, TokenType.EXTENDS, "visitClass");
            }

            if (!node.getImplementsClause().isEmpty()) {
                markup.tagLineWrap( LineWrap.IMPLEMENTS, TokenType.IMPLEMENTS, "visitClass");
            }
            //TODO tag all implemented classes using unbounded_list?

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
            LineWrapTag tag = new LineWrapTag( LineWrap.TERNARY, "visitConditionalExpr" );
            input.tokens.get( question ).allowLineWrap( tag );

            // ensure that if a line-break is inserted before question mark, one is also inserted before colon
            int colon = input.findNext( trueExprEnd, falseExpr, TokenType.COLON )
                    .orElseThrow( () -> new RuntimeException( "Missing expected :." ) );
            input.tokens.get( colon )
                    .allowLineWrap( new LineWrapTag( tag.getGroupId(), LineWrap.TERNARY, "visitConditionalExpr" ) );

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
            //TODO should we just filter on kind == METHOD_INVOCATION?
            if( Config.INSTANCE.lineWrap.allowMemberSelectLineWrap( node.getExpression().getKind() ) ) {
                //TODO will exprEnd always be the same as dot?
                int exprEnd = input.getLastTokenIndex( node.getExpression() );
                int dot = input.findNext( exprEnd, input.getLastTokenIndex( node ), TokenType.DOT )
                        .orElseThrow( () -> new RuntimeException( "Missing dot." ) );

                // this tag's groupId will be overwritten later when the token is added to a Line
                input.tokens.get( dot )
                        .allowLineWrap( new LineWrapTag( LineWrap.MEMBER_SELECT, "visitMemberSelect" ) );
            }

            return super.visitMemberSelect( node, input );
        }

        @Override
        public Void visitMethod(MethodTree node, Input input) {
            MarkupTool markup = new MarkupTool(node, input);
            markup.tagLineWrapGroupWithClosingParen( LineWrap.METHOD_ARG, node.getParameters(), "visitMethod" );

            if (!node.getThrows().isEmpty()) {
                markup.tagLineWrap( LineWrap.THROWS, TokenType.THROWS, "visitMethod" );
                markup.tagLineWrapGroup( LineWrap.UNBOUND_LIST_ITEM, node.getThrows(), "visitMethod" );
            }

            markup.indentBracedBlock(node.getBody());

            return super.visitMethod(node, input);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Input input) {
            MarkupTool markup = new MarkupTool(node, input);
            markup.tagLineWrapGroupWithClosingParen( LineWrap.METHOD_ARG, node.getArguments(), "visitMethodInvocation" );

            return super.visitMethodInvocation(node, input);
        }

        @Override
        public Void visitNewArray(NewArrayTree node, Input input ) {
            if( node.getInitializers() != null ) {
                new MarkupTool( node, input )
                        .tagLineWrapGroupWithClosingBrace( LineWrap.ARRAY, node.getInitializers(), "visitNewArrayTree" );
            }

            return super.visitNewArray( node, input );
        }

        @Override
        public Void visitNewClass(NewClassTree node, Input input ) {
            MarkupTool markup = new MarkupTool(node, input);
            markup.tagLineWrapGroupWithClosingParen( LineWrap.METHOD_ARG, node.getArguments(), "visitNewClass" );

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

        @Override
        public Void visitTry(TryTree node, Input input) {
            MarkupTool markup = new MarkupTool(node, input);

            // allow line-breaks between resources
            markup.tagLineWrapGroup( LineWrap.METHOD_ARG, node.getResources(), "visitTry");

            // indent try-block
            markup.indentBracedBlock(node.getBlock());

            // indent each catch-block
            node.getCatches().stream().forEach(c -> markup.indentBracedBlock(c.getBlock()));

            for( CatchTree cTree : node.getCatches() ) {
                if( cTree.getParameter().getType().getKind() == Tree.Kind.UNION_TYPE ) {
                    // allow line-breaks between unioned catch elements
                    new MarkupTool(cTree, input)
                            .tagLineWrapGroupWithClosingParen( LineWrap.UNION, TokenType.OR, "visitTry");
                }
            }

            // indent finally-block
            if (isValid(node.getFinallyBlock())) {
                markup.indentBracedBlock(node.getFinallyBlock());
            }

            return super.visitTry(node, input);
        }

        @Override
        public Void visitVariable(VariableTree node, Input input) {
            if (isValid(node.getInitializer())) {
                MarkupTool markup = new MarkupTool(node, input);
                markup.tagLineWrap( LineWrap.ASSIGNMENT, node.getInitializer(), "visitVariable");
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
