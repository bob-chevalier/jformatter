package com.staircaselabs.jformatter.formatters;

import static com.staircaselabs.jformatter.core.CompilationUnitUtils.getCompilationUnit;

import com.staircaselabs.jformatter.core.*;
import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.tree.JCTree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.staircaselabs.jformatter.core.TextToken.TokenType;

/**
 *
 */
public class UnusedImportsFormatter extends ScanningFormatter {

    public UnusedImportsFormatter() {
        super( new UnusedImportsFormatterScanner() );
    }

    /**
     * This method is overridden so that this formatter can make use of multiple scanners
     */
    @Override
    public String format( String text ) throws FormatException {
        // collect all of the used names from the given text using the read-only scanner
        Set<String> usedNames = new HashSet<>();
        JCTree.JCCompilationUnit unit = getCompilationUnit( text );
        UsedNamesScanner usedNamesScanner = new UsedNamesScanner();
        usedNamesScanner.scan( unit, usedNames );

        // pass the used names and package name to the update scanner
        ((UnusedImportsFormatterScanner)scanner).setUsedNames( usedNames );
        String pkgName = unit.getPackageName() == null ? null : unit.getPackageName().toString();
        ((UnusedImportsFormatterScanner)scanner).setPackageName( pkgName );

        // now remove unused imports using the update scanner
        return super.format( text );
    }

    private static class UsedNamesScanner extends TreePathScanner<Void, Set<String>> {

        @Override
        public Void visitIdentifier( IdentifierTree node, Set<String> usedNames ) {
            if( node != null ) {
                usedNames.add( node.getName().toString() );
            }
            return super.visitIdentifier( node, usedNames );
        }

    }

    private static class UnusedImportsFormatterScanner extends FormatScanner {

        private static final boolean VERBOSE = false;
        private static final boolean ENABLED = true;
        private static final String NAME = "UnusedImportsFormatter::";
        private Set<String> usedNames;
        private String pkgName;

        public void setUsedNames(Set<String> usedNames) {
            this.usedNames = usedNames;
        }

        public void setPackageName(String pkgName) {
            this.pkgName = pkgName;
        }

        @Override
        public Void visitCompilationUnit( CompilationUnitTree node, Input input ) {
            if (VERBOSE) System.out.println("======UnusedImportsFormatter::visitCompUnit======");

            Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "CompilationUnit" )
                    .append( input.newline );

            List<Tree> newImports = node.getImports().stream().filter( this::isUsed ).collect( Collectors.toList() );
            if( !newImports.isEmpty() ) {
                replacement.append( input.newline )
                        .appendList( newImports, TokenType.NEWLINE )
                        .append( input.newline );
            }

            replacement.append( input.newline );

            int importsStart = input.getFirstTokenIndex( node.getImports().get( 0 ) );
            int importsStop = input.getLastTokenIndex( node.getImports().get( node.getImports().size() - 1 ) );

            int replaceStart = input.findPrevByExclusion(
                    input.getFirstTokenIndex( node ),
                    importsStart,
                    TokenType.WHITESPACE,
                    TokenType.NEWLINE
            ).orElse( importsStart - 1 );
            replaceStart++;

            int replaceStop = input.findNextByExclusion(
                    importsStop,
                    input.getLastTokenIndex( node ),
                    TokenType.WHITESPACE,
                    TokenType.NEWLINE
            ).orElse( importsStop );

            if( ENABLED ) replacement.build( replaceStart, replaceStop ).ifPresent( this::addReplacement );
            return super.visitCompilationUnit( node, input );
        }

        private boolean isUsed( ImportTree importTree ) {
            Tree id = importTree.getQualifiedIdentifier();

            String qualifier = id.getKind() == Kind.MEMBER_SELECT
                    ? ((MemberSelectTree)id).getExpression().toString()
                    : null;

            String simpleName = id.getKind() == Kind.IDENTIFIER
                    ? ((IdentifierTree)id).getName().toString()
                    : ((MemberSelectTree)id).getIdentifier().toString();

            if( qualifier.equals( "java.lang" ) || qualifier.equals( pkgName ) ) {
                return false;
            } else if( simpleName.equals( "*" ) || usedNames.contains( simpleName ) ) {
                return true;
            } else {
                return false;
            }
        }
        private void printTree( Tree tree, Input input ) {
            int startIdx = input.getFirstTokenIndex( tree );
            int endIdx = input.getLastTokenIndex( tree );
            for( int pos=startIdx; pos<endIdx; pos++ ) {
                System.out.println( pos + ": [" + input.tokens.get( pos ).getText() + "]" );
            }
        }
    }

}
