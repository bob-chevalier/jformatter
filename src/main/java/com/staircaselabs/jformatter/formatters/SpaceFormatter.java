package com.staircaselabs.jformatter.formatters;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.FormatScanner;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.ScanningFormatter;
import com.staircaselabs.jformatter.core.TextToken;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.staircaselabs.jformatter.core.TokenUtils;
import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
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
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.IntersectionTypeTree;
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

public class SpaceFormatter extends ScanningFormatter {

    public SpaceFormatter() {
        super( new SpaceFormatterScanner() );
    }

//    public static enum IndentType { SPACES, TABS }

    private static class SpaceFormatterScanner extends FormatScanner {

        private static final TokenType[] WS_OR_NEWLINE = {
                TokenType.WHITESPACE,
                TokenType.NEWLINE
        };
        private static final TokenType[] WS_NEWLINE_COMMENT_OR_BRACKET = {
                TokenType.WHITESPACE,
                TokenType.NEWLINE,
                TokenType.COMMENT_BLOCK,
                TokenType.COMMENT_JAVADOC,
                TokenType.COMMENT_LINE,
                TokenType.LEFT_BRACKET
        };

        private static final String SPACE = " ";
//        private final IndentType indentType;
//        private final int numTabSpaces;

        @Override
        public Void visitAnnotatedType( AnnotatedTypeTree node, Input input ) {
            System.out.println( "======visitAnnotatedType======" );
//            printTree( (JCTree)node, input );
            StringBuilder sb = new StringBuilder();
            for( AnnotationTree annotation : node.getAnnotations() ) {
                sb.append( input.stringifyTree( (JCTree)annotation ) );
                sb.append( SPACE );
//                printTree( (JCTree)annotation, input );
            }
            sb.append( input.stringifyTree( (JCTree)node.getUnderlyingType() ) );
//            printTree( (JCTree)node.getUnderlyingType(), input );

            int startIdx = input.getFirstTokenIndex( (JCTree)node );
            int endIdx = input.getLastTokenIndex( (JCTree)node );
            //TODO this screws up annotated arrays
//            createReplacement( input, startIdx, endIdx, sb ).ifPresent( this::addReplacement );

            return super.visitAnnotatedType( node, input );
        }

        @Override
        public Void visitAnnotation( AnnotationTree node, Input input ) {
            System.out.println( "======visitAnnotation======" );
//            printTree( (JCTree)node, input );
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

            createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );

            return super.visitAnnotation( node, input );
        }

        @Override
        public Void visitArrayAccess( ArrayAccessTree node, Input input ) {
            System.out.println( "======visitArrayAccess======" );
            if( node.getExpression().getKind() == Kind.IDENTIFIER ) {
                StringBuilder sb = new StringBuilder();
                sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
                sb.append( "[" );
                sb.append( input.stringifyTree( (JCTree)node.getIndex() ) );
                sb.append( "]" );

                int id = input.getFirstTokenIndex( (JCTree)node.getExpression() );
                int rightBracket = input.findNext( id, TokenType.RIGHT_BRACKET ).getAsInt();
                createReplacement( input, id, (rightBracket + 1), sb ).ifPresent( this::addReplacement );
            } else {
                int index = input.getFirstTokenIndex( (JCTree)node.getIndex() );
                int leftBracket = input.findPrev( index, TokenType.LEFT_BRACKET ).getAsInt();
                int prevRightBracket = input.findPrev( leftBracket, TokenType.RIGHT_BRACKET ).getAsInt();

                StringBuilder sb = new StringBuilder( "][" );
                createReplacement( input, prevRightBracket, (leftBracket + 1), sb ).ifPresent( this::addReplacement );
            }

            return super.visitArrayAccess( node, input );
        }

        @Override
        public Void visitArrayType( ArrayTypeTree node, Input input ) {
            System.out.println( "======visitArrayType======" );
//            printTree( (JCTree)node, input );
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

            createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );

            return super.visitArrayType( node, input );
        }

        @Override
        public Void visitAssert( AssertTree node, Input input ) {
            System.out.println( "======visitAssert======" );
            return super.visitAssert( node, input );
        }

        @Override
        public Void visitAssignment( AssignmentTree node, Input input ) {
            System.out.println( "======visitAssignment======" );
            StringBuilder sb = new StringBuilder();
            sb.append( input.stringifyTree( (JCTree)node.getVariable() ) );
            sb.append( " = " );
            sb.append( input.stringifyTree( (JCTree)node.getExpression() ) );
            createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );

            return super.visitAssignment( node, input );
        }

        @Override
        public Void visitBinary( BinaryTree node, Input input ) {
            System.out.println( "======visitBinary======" );
            return super.visitBinary( node, input );
        }

        @Override
        public Void visitBlock(BlockTree node, Input input){
            System.out.println( "======visitBlock======" );
            return super.visitBlock( node, input );
        }

        @Override
        public Void visitBreak( BreakTree node, Input input ) {
            System.out.println( "======visitBreak======" );
            return super.visitBreak( node, input );
        }

        @Override
        public Void visitCase( CaseTree node, Input input ) {
            System.out.println( "======visitCase======" );
            return super.visitCase( node, input );
        }

        @Override
        public Void visitCatch( CatchTree node, Input input ) {
            System.out.println( "======visitCatch======" );
            return super.visitCatch( node, input );
        }

        @Override
        public Void visitClass( ClassTree node, Input input ) {
            System.out.println( "======visitClass======" );
            //TODO put variables and methods in alphabetical/static order (Do this in a separate formatter: one for members and one for methods?)
//            for( Tree member : node.getMembers() ) {
//                System.out.println( "member: " + member.getKind() + "\n" + member );
//            }
            return super.visitClass( node, input );
        }

        @Override
        public Void visitCompilationUnit( CompilationUnitTree node, Input input ) {
            System.out.println( "======visitCompUnit======" );
//            return super.visitCompilationUnit( node, input );
            scan( node.getTypeDecls(), input );
            return null;
        }

        @Override
        public Void visitCompoundAssignment( CompoundAssignmentTree node, Input input ) {
            System.out.println( "======CompoundAssignment======" );
            return super.visitCompoundAssignment( node, input );
        }

        @Override
        public Void visitConditionalExpression( ConditionalExpressionTree node, Input input ) {
            System.out.println( "======visitConditionalExpression======" );
            return super.visitConditionalExpression( node, input );
        }

        @Override
        public Void visitContinue(ContinueTree node, Input input ) {
            System.out.println( "======visitContinue======" );
            return super.visitContinue( node, input );
        }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree node, Input input ) {
            System.out.println( "======visitDoWhileLoop======" );
            return super.visitDoWhileLoop( node, input );
        }

        @Override
        public Void visitEmptyStatement(EmptyStatementTree node, Input input ) {
            System.out.println( "======visitEmptyStatement======" );
            return super.visitEmptyStatement( node, input );
        }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree node, Input input ) {
            System.out.println( "======visitEnhancedForLoop======" );
            return super.visitEnhancedForLoop( node, input );
        }

        @Override
        public Void visitErroneous(ErroneousTree node, Input input ) {
            System.out.println( "======visitErroneous======" );
            return super.visitErroneous( node, input );
        }

        @Override
        public Void visitExpressionStatement(ExpressionStatementTree node, Input input ) {
            System.out.println( "======visitExpressionStatement======" );
            return super.visitExpressionStatement( node, input );
        }

        @Override
        public Void visitForLoop(ForLoopTree node, Input input ) {
            System.out.println( "======visitForLoop======" );
            return super.visitForLoop( node, input );
        }

        @Override
        public Void visitIdentifier(IdentifierTree node, Input input ) {
            System.out.println( "======visitIdentifier======" );
//            printTree( (JCTree)node, input );
            return super.visitIdentifier( node, input );
        }

        @Override
        public Void visitIntersectionType(IntersectionTypeTree node, Input input ) {
            System.out.println( "======visitIntersection======" );
            return super.visitIntersectionType( node, input );
        }

        @Override
        public Void visitIf(IfTree node, Input input ) {
            System.out.println( "======visitIf======" );
            return super.visitIf( node, input );
        }

        @Override
        public Void visitImport(ImportTree node, Input input ) {
            System.out.println( "======visitImport======" );
            return super.visitImport( node, input );
        }

        @Override
        public Void visitInstanceOf(InstanceOfTree node, Input input ) {
            System.out.println( "======visitInstanceOf======" );
            return super.visitInstanceOf( node, input );
        }

        @Override
        public Void visitLabeledStatement(LabeledStatementTree node, Input input ) {
            System.out.println( "======visitLabeledStatement======" );
            return super.visitLabeledStatement( node, input );
        }

        @Override
        public Void visitLambdaExpression(LambdaExpressionTree node, Input input ) {
            System.out.println( "======visitLambdaExpression======" );
            return super.visitLambdaExpression( node, input );
        }

        @Override
        public Void visitLiteral(LiteralTree node, Input input ) {
            System.out.println( "======visitLiteral======" );
            return super.visitLiteral( node, input );
        }

        @Override
        public Void visitMemberReference(MemberReferenceTree node, Input input ) {
            System.out.println( "======visitMemberReference======" );
            return super.visitMemberReference( node, input );
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, Input input ) {
            System.out.println( "======visitMemberSelect======" );
            return super.visitMemberSelect( node, input );
        }

        @Override
        public Void visitMethod(MethodTree node, Input input ) {
            System.out.println( "======visitMethod======" );
//            System.out.println( node );
//            int startIdx = input.getFirstTokenIndex( (JCTree)node );
//            int endIdx = input.getLastTokenIndex( (JCTree)node );
//            for( int pos=startIdx; pos<=endIdx; pos++ ) {
//                System.out.println( pos + ": " + input.tokens.get( pos ).getText() );
//            }

//            System.out.println( "modifiers: " + node.getModifiers() );
//            System.out.println( "params: " + node.getParameters() );
//            System.out.println( "return type: " + node.getReturnType() );
//            System.out.println( "throws: " + node.getThrows() );
//            System.out.println( "type params: " + node.getTypeParameters() );
//            System.out.println( "body: " + node.getBody() );
//            System.out.println( "name: " + node.getName() );
//            System.out.println( "default value: " + node.getDefaultValue() );
//            System.out.println( "-----------------------" );

            StringBuilder sb = new StringBuilder();
            addModifiers( node.getModifiers(), sb );
            addTypeParams( node.getTypeParameters(), sb );
            sb.append( node.getReturnType().toString() );
            sb.append( node.getName().toString() );
            sb.append( "(" );
            sb.append( SPACE );
//            for( VariableTree variable : node.getParameters() ) {
//                variable.get
//            }
//            sb.append( )

            //TODO endIdx will be one token after the semi-colon
            // find start of class declaration (eating any leading whitespace)
            int startIdx = input.getFirstTokenIndex( (JCTree)node );
//            OptionalInt leadingCode = input.findPrevByExclusion( startIdx, TokenType.WHITESPACE );
//            int replaceStartIdx = leadingCode.isPresent() ? (leadingCode.getAsInt() + 1) : 0;

//            // find start of class body
//            int leftBraceIdx = input.findNext( startIdx, TokenType.LEFT_BRACE ).getAsInt();
//            int bodyStartIdx = input.findNextByExclusion( (leftBraceIdx + 1), WS_OR_NEWLINE ).getAsInt();

            // find end of class body
            int endIdx = input.getLastTokenIndex( (JCTree)node );
//            int rightBraceIdx = input.findPrev( (endIdx + 1), TokenType.RIGHT_BRACE ).getAsInt();
//            int bodyEndIdx = input.findPrevByExclusion( rightBraceIdx, WS_OR_NEWLINE ).getAsInt();

//            createReplacement( input, startIdx, endIdx, sb.toString() ).ifPresent( this::addReplacement );

            return super.visitMethod( node, input );
//            System.out.println( sb.toString() );
//            return null;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Input input ) {
            System.out.println( "======visitMethodInvocation======" );
            return super.visitMethodInvocation( node, input );
        }

        @Override
        public Void visitModifiers( ModifiersTree node, Input input ) {
            System.out.println( "======visitModifiers======" );
//            printTree( (JCTree)node, input );
            StringBuilder sb = new StringBuilder();

            // we can ignore the return value of appendModifiers here
            appendModifiers( node, input, sb );
            createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );

            return super.visitModifiers( node, input );
        }

        @Override
        public Void visitNewArray( NewArrayTree node, Input input ) {
            System.out.println( "======visitNewArray======" );
//            printTree( (JCTree)node, input );
            //TODO what about top-level annotations?
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

                    createReplacement( input, firstExprStartIdx, lastExprEndIdx, sb ).ifPresent( this::addReplacement );
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
                createReplacement( input, startIdx, semicolonIdx, sb ).ifPresent( this::addReplacement );
            }

            return super.visitNewArray( node, input );
        }

        @Override
        public Void visitNewClass(NewClassTree node, Input input ) {
            System.out.println( "======visitNewClass======" );
            return super.visitNewClass( node, input );
        }

        @Override
        public Void visitOther(Tree node, Input input ) {
            System.out.println( "======visitOther======" );
            return super.visitOther( node, input );
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree node, Input input ) {
            System.out.println( "======visitParameterizedType======" );
            int endTypeIdx = input.getLastTokenIndex( (JCTree)node.getType() );
            int lessThanIdx = input.findNext( endTypeIdx, TokenType.LESS_THAN ).getAsInt();
            int endIdx = input.getLastTokenIndex( (JCTree)node );

            // remove any spaces or newlines between type token and less-than token
            StringBuilder sb = new StringBuilder();
            sb.append( input.stringifyTokens( lessThanIdx, (endIdx + 1) ) );
            createReplacement( input, endTypeIdx, endIdx, sb ).ifPresent( this::addReplacement );

            return super.visitParameterizedType( node, input );
        }

        @Override
        public Void visitParenthesized(ParenthesizedTree node, Input input ) {
            System.out.println( "======visitParenthesized======" );
            return super.visitParenthesized( node, input );
        }

        @Override
        public Void visitPrimitiveType(PrimitiveTypeTree node, Input input ) {
            System.out.println( "======visitPrimitiveType======" );
            return super.visitPrimitiveType( node, input );
        }

        @Override
        public Void visitReturn( ReturnTree node, Input input ) {
            System.out.println( "======visitReturn======" );
            return super.visitReturn( node, input );
        }

        @Override
        public Void visitSwitch(SwitchTree node, Input input ) {
            System.out.println( "======visitSwitch======" );
            return super.visitSwitch( node, input );
        }

        @Override
        public Void visitSynchronized(SynchronizedTree node, Input input ) {
            System.out.println( "======visitSynchronized======" );
            return super.visitSynchronized( node, input );
        }

        @Override
        public Void visitThrow(ThrowTree node, Input input ) {
            System.out.println( "======visitThrow======" );
            return super.visitThrow( node, input );
        }

        @Override
        public Void visitTry(TryTree node, Input input ) {
            System.out.println( "======visitTry" );
            return super.visitTry( node, input );
        }

        @Override
        public Void visitTypeCast(TypeCastTree node, Input input ) {
            System.out.println( "======visitTypeCast======" );
            return super.visitTypeCast( node, input );
        }

        @Override
        public Void visitTypeParameter(TypeParameterTree node, Input input ) {
            System.out.println( "======visitTypeParameter======" );
//            printTree( (JCTree)node, input );
            StringBuilder sb = new StringBuilder();
            for( AnnotationTree annotation : node.getAnnotations() ) {
                sb.append( input.stringifyTree( (JCTree)annotation ) );
                sb.append( SPACE );
            }

            sb.append( node.getName() );

            if( !node.getBounds().isEmpty() ) {
                sb.append( " extends " );
                sb.append( node.getBounds().stream().map( Tree::toString ).collect( Collectors.joining( ", " ) ) );
            }

            int startIdx = input.getFirstTokenIndex( (JCTree)node );
            int endIdx = input.getLastTokenIndex( (JCTree)node );
            createReplacement( input, startIdx, endIdx, sb ).ifPresent( this::addReplacement );

            return super.visitTypeParameter( node, input );
        }

        @Override
        public Void visitUnionType(UnionTypeTree node, Input input ) {
            System.out.println( "======visitUnionType======" );
            return super.visitUnionType( node, input );
        }

        @Override
        public Void visitUnary(UnaryTree node, Input input ) {
            System.out.println( "======visitUnary======" );
            return super.visitUnary( node, input );
        }

        @Override
        public Void visitVariable(VariableTree node, Input input ) {
            System.out.println( "======visitVariable======" );
//            printTree( (JCTree)node, input );
            StringBuilder sb = new StringBuilder();

            // We're actually appending two strings here because appendModifiers not
            // only appends any modifiers to the StringBuilder, but it also returns
            // the string that it expects to follow the modifiers
            sb.append( appendModifiers( node.getModifiers(), input, sb ) );

            sb.append( input.stringifyTree( (JCTree)node.getType() ) );
            sb.append( SPACE );
            sb.append( node.getName() );

            if( input.isValid( (JCTree)node.getInitializer() ) ) {
                sb.append( " = " );
                sb.append( input.stringifyTree( (JCTree)node.getInitializer() ) );
            }

            int startIdx = input.getFirstTokenIndex( (JCTree)node );
            int endIdx = input.getLastTokenIndex( (JCTree)node );
            OptionalInt semi = input.findPrev( startIdx, endIdx, TokenType.SEMICOLON );
            if( semi.isPresent() ) {
                sb.append( ";" );
            }

            createReplacement( input, (JCTree)node, sb ).ifPresent( this::addReplacement );

            return super.visitVariable( node, input );
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree node, Input input ) {
            System.out.println( "======visitWhileLoop======" );
            return super.visitWhileLoop( node, input );
        }

        // Modifiers can be tricky if they have annotations because we want to insert a
        // newline after each annotation.  If annotations exist, but modifiers do not, this
        // results in one-or-more annotations, following by a newline.  But the next time
        // the text is tokenized, the trailing newline will not be included in the ModifiersTree.
        // As a result, depending on the order of operations, we could wind of with duplicate
        // newlines, or no trailing newline at all unless we perform the special logic in this
        // method.  This method appends formatted modifiers to the given StringBuilder and also
        // returns a string that should be inserted after the modifers (either a space or newline)
        private String appendModifiers( ModifiersTree node, Input input, StringBuilder sb ) {
            if( input.isValid( (JCTree)node ) ) {
                if( !node.getAnnotations().isEmpty() ) {
                    // append annotations, delimited by newlines (final newline will not be appended)
                    sb.append(
                            node.getAnnotations().stream()
                                    .map( JCTree.class::cast )
                                    .map( input::stringifyTree )
                                    .collect( Collectors.joining( input.newline ) )
                    );

                    if( !node.getFlags().isEmpty() ) {
                        // modifier flags are about to be appended so insert a newline
                        sb.append( input.newline );
                    } else {
                        // no modifier flags exist so notify caller that trailing newline is expected
                        return input.newline;
                    }
                }

                // if modifier flags are present, append them, delimited by a single-space
                sb.append( node.getFlags().stream().map( Modifier::toString ).collect( Collectors.joining( SPACE ) ) );

                // notify caller of the string that is expected to follow these modifiers
                return node.getFlags().isEmpty() ? "" : SPACE;
            } else {
                return "";
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
                sb.append( "> " );
            }
        }

        private void printTree( JCTree tree, Input input ) {
            int startIdx = input.getFirstTokenIndex( tree );
            int endIdx = input.getLastTokenIndex( tree );
            for( int pos=startIdx; pos<endIdx; pos++ ) {
                System.out.println( pos + ": [" + input.tokens.get( pos ).getText() + "]" );
            }
        }
    }

}
