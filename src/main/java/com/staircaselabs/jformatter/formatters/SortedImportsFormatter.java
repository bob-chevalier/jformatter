package com.staircaselabs.jformatter.formatters;

import com.staircaselabs.jformatter.core.FormatException;
import com.staircaselabs.jformatter.core.ReplacementScanner;
import com.staircaselabs.jformatter.core.Input;
import com.staircaselabs.jformatter.core.Replacement;
import com.staircaselabs.jformatter.core.ReplacementFormatter;
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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.staircaselabs.jformatter.core.CompilationUnitUtils.getCompilationUnit;

/**
 *
 */
public class SortedImportsFormatter extends ReplacementFormatter {

    public SortedImportsFormatter() {
        super( new SortedImportsScanner() );
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
        ((SortedImportsScanner)scanner).setUsedNames( usedNames );
        String pkgName = unit.getPackageName() == null ? null : unit.getPackageName().toString();
        ((SortedImportsScanner)scanner).setPackageName( pkgName );

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

    private static class SortedImportsScanner extends ReplacementScanner {

        private static final boolean VERBOSE = false;
        private static final boolean ENABLED = true;
        private static final String NAME = "SortedImportsFormatter::";
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
            if (VERBOSE) System.out.println("======SortedImportsFormatter::visitCompUnit======");
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

                // filter out unused imports and sort all remaining imports
                SortedSet<ImportInfo> infos = node.getImports().stream()
                        .map( ImportInfo::new )
                        .filter( this::isUsed )
                        .collect( Collectors.toCollection( TreeSet::new ) );

                // separate imports from header or package by a blank line
                Replacement.Builder replacement = new Replacement.Builder( node, input, NAME + "CompilationUnit" )
                        .append( input.newline )
                        .append( input.newline );

                boolean prevImportWasStatic = false;
                for( ImportInfo info : infos ) {
                    // insert a blank line between static and non-static imports
                    if( !info.isStatic && prevImportWasStatic ) {
                        replacement.append( input.newline );
                    }
                    prevImportWasStatic = info.isStatic;

                    // insert import string, discarding any comments
                    int pos = input.getFirstTokenIndex( info.importTree );
                    int importStop = input.getLastTokenIndex( info.importTree );
                    while( pos <= importStop && input.tokens.get( pos ).getType() != TokenType.SEMICOLON ) {
                        replacement.append( input.tokens.get( pos++ ).getText() );
                    }
                    replacement.append( ";" ).append( input.newline );
                }

                // separate imports from the rest of the file by a blank line
                if( !infos.isEmpty() ) {
                    replacement.append(input.newline);
                }

                int importsStop = input.getLastTokenIndex( node.getImports().get( node.getImports().size() - 1 ) );
                int compUnitStop = input.getLastTokenIndex( node );
                int replaceStop = input.findNextByExclusion(
                        importsStop,
                        compUnitStop,
                        TokenType.WHITESPACE,
                        TokenType.NEWLINE
                ).orElse( compUnitStop );

                // only replace the imports, the rest of the compilation unit is formatted elsewhere
                if (ENABLED) replacement.build( replaceStart, replaceStop ).ifPresent( this::addReplacement );
            }

            return super.visitCompilationUnit( node, input );
        }

        private boolean isUsed( ImportInfo info ) {
            if( info.qualifier.equals( "java.lang" ) || info.qualifier.equals( pkgName ) ) {
                return false;
            } else if( info.simpleName.equals( "*" ) || usedNames.contains( info.simpleName ) ) {
                return true;
            } else {
                return false;
            }
        }

        private static class ImportInfo implements Comparable<ImportInfo> {

            public final ImportTree importTree;
            public final String qualifier;
            public final String simpleName;
            public final String fullName;
            public final boolean isStatic;

            public ImportInfo( ImportTree importTree ) {
                this.importTree = importTree;

                Tree id = importTree.getQualifiedIdentifier();

                qualifier = id.getKind() == Kind.MEMBER_SELECT
                        ? ((MemberSelectTree)id).getExpression().toString()
                        : null;

                simpleName = id.getKind() == Kind.IDENTIFIER
                        ? ((IdentifierTree)id).getName().toString()
                        : ((MemberSelectTree)id).getIdentifier().toString();

                fullName = qualifier + "." + simpleName;

                isStatic = importTree.isStatic();
            }

            @Override
            public int compareTo( ImportInfo other ) {
                if( isStatic != other.isStatic) {
                    return isStatic ? -1 : 1;
                }
                return fullName.compareTo( other.fullName );
            }

        }

    }

}
