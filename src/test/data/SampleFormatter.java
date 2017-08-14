package com.staircaselabs.jformatter.formatters;

import com.staircaselabs.jformatter.core.FormatScanner;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.ScanningFormatter;
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
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.tools.javac.tree.JCTree;

public class SampleFormatter extends ScanningFormatter {

    private static boolean VERBOSE = false;

    public SampleFormatter() {
        super( new SampleFormatterScanner() );
    }

    private static class SampleFormatterScanner extends FormatScanner {

        @Override
        public Void visitAnnotatedType( AnnotatedTypeTree node, Input input ) {
            System.out.println( "======visitAnnotatedType======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitAnnotatedType( node, input );
        }

        @Override
        public Void visitAnnotation( AnnotationTree node, Input input ) {
            System.out.println( "======visitAnnotation======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitAnnotation( node, input );
        }

        @Override
        public Void visitArrayAccess( ArrayAccessTree node, Input input ) {
            System.out.println( "======visitArrayAccess======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitArrayAccess( node, input );
        }

        @Override
        public Void visitArrayType( ArrayTypeTree node, Input input ) {
            System.out.println( "======visitArrayType======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitArrayType( node, input );
        }

        @Override
        public Void visitAssert( AssertTree node, Input input ) {
            System.out.println( "======visitAssert======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitAssert( node, input );
        }

        @Override
        public Void visitAssignment( AssignmentTree node, Input input ) {
            System.out.println( "======visitAssignment======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitAssignment( node, input );
        }

        @Override
        public Void visitBinary( BinaryTree node, Input input ) {
            System.out.println( "======visitBinary======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitBinary( node, input );
        }

        @Override
        public Void visitBlock(BlockTree node, Input input){
            System.out.println( "======visitBlock======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitBlock( node, input );
        }

        @Override
        public Void visitBreak( BreakTree node, Input input ) {
            System.out.println( "======visitBreak======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitBreak( node, input );
        }

        @Override
        public Void visitCase( CaseTree node, Input input ) {
            System.out.println( "======visitCase======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitCase( node, input );
        }

        @Override
        public Void visitCatch( CatchTree node, Input input ) {
            System.out.println( "======visitCatch======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitCatch( node, input );
        }

        @Override
        public Void visitClass( ClassTree node, Input input ) {
            System.out.println( "======visitClass======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitClass( node, input );
        }

        @Override
        public Void visitCompilationUnit( CompilationUnitTree node, Input input ) {
            System.out.println( "======visitCompUnit======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitCompilationUnit( node, input );
        }

        @Override
        public Void visitCompoundAssignment( CompoundAssignmentTree node, Input input ) {
            System.out.println( "======visitCompoundAssignment======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitCompoundAssignment( node, input );
        }

        @Override
        public Void visitConditionalExpression( ConditionalExpressionTree node, Input input ) {
            System.out.println( "======visitConditionalExpression======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitConditionalExpression( node, input );
        }

        @Override
        public Void visitContinue(ContinueTree node, Input input ) {
            System.out.println( "======visitContinue======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitContinue( node, input );
        }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree node, Input input ) {
            System.out.println( "======visitDoWhileLoop======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitDoWhileLoop( node, input );
        }

        @Override
        public Void visitEmptyStatement(EmptyStatementTree node, Input input ) {
            System.out.println( "======visitEmptyStatement======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitEmptyStatement( node, input );
        }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree node, Input input ) {
            System.out.println( "======visitEnhancedForLoop======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitEnhancedForLoop( node, input );
        }

        @Override
        public Void visitErroneous(ErroneousTree node, Input input ) {
            System.out.println( "======visitErroneous======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitErroneous( node, input );
        }

        @Override
        public Void visitExpressionStatement(ExpressionStatementTree node, Input input ) {
            System.out.println( "======visitExpressionStatement======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitExpressionStatement( node, input );
        }

        @Override
        public Void visitForLoop(ForLoopTree node, Input input ) {
            System.out.println( "======visitForLoop======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitForLoop( node, input );
        }

        @Override
        public Void visitIdentifier(IdentifierTree node, Input input ) {
            System.out.println( "======visitIdentifier======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitIdentifier( node, input );
        }

        @Override
        public Void visitIntersectionType(IntersectionTypeTree node, Input input ) {
            System.out.println( "======visitIntersection======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitIntersectionType( node, input );
        }

        @Override
        public Void visitIf(IfTree node, Input input ) {
            System.out.println( "======visitIf======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitIf( node, input );
        }

        @Override
        public Void visitImport(ImportTree node, Input input ) {
            System.out.println( "======visitImport======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitImport( node, input );
        }

        @Override
        public Void visitInstanceOf(InstanceOfTree node, Input input ) {
            System.out.println( "======visitInstanceOf======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitInstanceOf( node, input );
        }

        @Override
        public Void visitLabeledStatement(LabeledStatementTree node, Input input ) {
            System.out.println( "======visitLabeledStatement======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitLabeledStatement( node, input );
        }

        @Override
        public Void visitLambdaExpression(LambdaExpressionTree node, Input input ) {
            System.out.println( "======visitLambdaExpression======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitLambdaExpression( node, input );
        }

        @Override
        public Void visitLiteral(LiteralTree node, Input input ) {
            System.out.println( "======visitLiteral======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitLiteral( node, input );
        }

        @Override
        public Void visitMemberReference(MemberReferenceTree node, Input input ) {
            System.out.println( "======visitMemberReference======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitMemberReference( node, input );
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, Input input ) {
            System.out.println( "======visitMemberSelect======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitMemberSelect( node, input );
        }

        @Override
        public Void visitMethod(MethodTree node, Input input ) {
            System.out.println( "======visitMethod======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitMethod( node, input );
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Input input ) {
            System.out.println( "======visitMethodInvocation======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitMethodInvocation( node, input );
        }

        @Override
        public Void visitModifiers(ModifiersTree node, Input input ) {
            System.out.println( "======visitModifiers======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitModifiers( node, input );
        }

        @Override
        public Void visitNewArray(NewArrayTree node, Input input ) {
            System.out.println( "======visitNewArray======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitNewArray( node, input );
        }

        @Override
        public Void visitNewClass(NewClassTree node, Input input ) {
            System.out.println( "======visitNewClass======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitNewClass( node, input );
        }

        @Override
        public Void visitOther(Tree node, Input input ) {
            System.out.println( "======visitOther======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitOther( node, input );
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree node, Input input ) {
            System.out.println( "======visitParameterizedType======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitParameterizedType( node, input );
        }

        @Override
        public Void visitParenthesized(ParenthesizedTree node, Input input ) {
            System.out.println( "======visitParenthesized======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitParenthesized( node, input );
        }

        @Override
        public Void visitPrimitiveType(PrimitiveTypeTree node, Input input ) {
            System.out.println( "======visitPrimitiveType======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitPrimitiveType( node, input );
        }

        @Override
        public Void visitReturn( ReturnTree node, Input input ) {
            System.out.println( "======visitReturn======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitReturn( node, input );
        }

        @Override
        public Void visitSwitch(SwitchTree node, Input input ) {
            System.out.println( "======visitSwitch======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitSwitch( node, input );
        }

        @Override
        public Void visitSynchronized(SynchronizedTree node, Input input ) {
            System.out.println( "======visitSynchronized======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitSynchronized( node, input );
        }

        @Override
        public Void visitThrow(ThrowTree node, Input input ) {
            System.out.println( "======visitThrow======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitThrow( node, input );
        }

        @Override
        public Void visitTry(TryTree node, Input input ) {
            System.out.println( "======visitTry" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitTry( node, input );
        }

        @Override
        public Void visitTypeCast(TypeCastTree node, Input input ) {
            System.out.println( "======visitTypeCast======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitTypeCast( node, input );
        }

        @Override
        public Void visitTypeParameter(TypeParameterTree node, Input input ) {
            System.out.println( "======visitTypeParameter======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitTypeParameter( node, input );
        }

        @Override
        public Void visitUnionType(UnionTypeTree node, Input input ) {
            System.out.println( "======visitUnionType======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitUnionType( node, input );
        }

        @Override
        public Void visitUnary(UnaryTree node, Input input ) {
            System.out.println( "======visitUnary======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitUnary( node, input );
        }

        @Override
        public Void visitVariable(VariableTree node, Input input ) {
            System.out.println( "======visitVariable======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitVariable( node, input );
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree node, Input input ) {
            System.out.println( "======visitWhileLoop======" );
            if( VERBOSE ) {
                printTree( (JCTree)node, input );
                System.out.println( "------------" );
            }
            return super.visitWhileLoop( node, input );
        }

        private printTree( JCTree tree, Input input ) {
            int startIdx = input.getFirstTokenIndex( tree );
            int endIdx = input.getLastTokenIndex( tree );
            for( int pos=startIdx; pos<=endIdx; pos++ ) {
                System.out.println( pos + ": [" + input.tokens.get( pos ).getText() + "]" );
            }
        }

    }

}
