package com.staircaselabs.jformatter.formatters;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.FormatScanner;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.Replacement;
import com.staircaselabs.jformatter.core.ScanningFormatter;
import com.staircaselabs.jformatter.core.TextToken.TokenType;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.tree.JCTree;

import java.util.HashSet;
import java.util.Set;

import static com.staircaselabs.jformatter.core.CompilationUnitUtils.getCompilationUnit;

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
            if( !node.getImports().isEmpty() ) {
                int compUnitStart = input.getFirstTokenIndex( node );
                int importsStart = input.getFirstTokenIndex( node.getImports().get( 0 ) );
                int replaceStart = input.findPrevByExclusion(
                        compUnitStart,
                        importsStart,
                        TokenType.WHITESPACE,
                        TokenType.NEWLINE
                ).orElse( compUnitStart - 1 );
                replaceStart++;

                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "CompilationUnit" );

                boolean foundValidImport = false;
                for( ImportTree importTree : node.getImports() ) {
                    if( isUsed( importTree ) ) {
                        if( !foundValidImport ) {
                            // this is the first valid import so insert an extra newline
                            replacement.appendWithLeadingNewlines( importTree, 2 );
                            foundValidImport = true;
                        } else {
                            replacement.appendWithLeadingNewlines( importTree, 1 );
                        }
                    } else {
                        // skip this import and any comments that preceed it
                        replacement.setCurrentPositionInclusive( input.getLastTokenIndex( importTree ) + 1 );
                    }
                }
                replacement.append( input.newline ).append( input.newline );

                int compUnitStop = input.getLastTokenIndex( node );
                int replaceStop = input.findNextByExclusion(
                        replacement.getCurrentPosInclusive(),
                        compUnitStop,
                        TokenType.WHITESPACE,
                        TokenType.NEWLINE
                ).orElse( compUnitStop );

                if (ENABLED) replacement.build(replaceStart, replaceStop).ifPresent(this::addReplacement);
            }

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

    }

}
