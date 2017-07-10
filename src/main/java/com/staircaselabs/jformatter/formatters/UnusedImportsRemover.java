package com.staircaselabs.jformatter.formatters;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

import com.staircaselabs.jformatter.core.FormatException;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;

public class UnusedImportsRemover {

    // starts with zero or more whitespace characters and/or a newline
    private static final Pattern LEADING_WHITESPACE_PATTERN = Pattern.compile( "^(\\s*\\R|\\s)" );

    public static String format( String text ) throws FormatException {
        Context context = new Context();
        JCCompilationUnit unit = parse( context, text );
        String pkgName = unit.getPackageName() != null ? unit.getPackageName().toString() : null;

        UnusedImportScanner scanner = new UnusedImportScanner();
        Set<String> usedNames = new HashSet<>();
        scanner.scan( unit, usedNames );

        StringBuilder sb = new StringBuilder( text );

        // remove imports in reverse order to maintain integrity of character positions
        ListIterator<JCImport> iter = unit.getImports().listIterator( unit.getImports().size() );
        while( iter.hasPrevious() ) {
            JCImport importTree = iter.previous();
            if( isUnused( importTree, pkgName, usedNames ) ) {
                int startPos = importTree.getStartPosition();
                int endPos = importTree.getEndPosition( unit.endPositions );

                // include any trailing whitespace and/or newline
                endPos += getTrailingWhitespaceCount( text, endPos );

                sb.replace( startPos, endPos, "" );
            }
        }

        return sb.toString();
    }

    private static int getTrailingWhitespaceCount( String text, int importEndPos ) {
        String textAfterImport = text.substring( importEndPos );
        Matcher matcher = LEADING_WHITESPACE_PATTERN.matcher( textAfterImport );
        return matcher.find() ? matcher.end() : 0;
    }

    private static JCCompilationUnit parse( Context context, String text ) throws FormatException {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        context.put( DiagnosticListener.class, diagnostics );
        Options.instance( context ).put( "allowStringFolding", "false" );

        try( JavacFileManager fileManager = new JavacFileManager( context, true, UTF_8 ) ) {
            fileManager.setLocation(
                    StandardLocation.PLATFORM_CLASS_PATH,
                    Collections.emptyList()
            );
        } catch( IOException ex ) {
            throw new FormatException( ex.getMessage() );
        }

        SimpleJavaFileObject source =
                new SimpleJavaFileObject( URI.create( "source" ), JavaFileObject.Kind.SOURCE ) {
            @Override
            public CharSequence getCharContent( boolean ignoreEncodingErrors ) throws IOException {
                return text;
            }
        };
        Log.instance( context ).useSource( source );

        ParserFactory parserFactory = ParserFactory.instance( context );
        JavacParser parser = parserFactory.newParser(
                text,
                true, // keepDocComments
                true, // keepEndPos
                true // keepLineMap
        );
        JCCompilationUnit unit = parser.parseCompilationUnit();
        unit.sourcefile = source;

        List<Diagnostic<? extends JavaFileObject>> errs = diagnostics.getDiagnostics().stream()
                .filter( p -> p.getKind() != Diagnostic.Kind.ERROR
                        && p.getCode() != "compiler.err.invalid.meth.decl.ret.type.req" )
                .collect( Collectors.toList() );
        if( !errs.isEmpty() ) {
            throw new FormatException( errs );
        }

        return unit;
    }

    private static String getImportQualifier( JCImport importTree ) {
        return importTree.getQualifiedIdentifier() instanceof JCFieldAccess
                ? ((JCFieldAccess)importTree.getQualifiedIdentifier()).getExpression().toString()
                : null;
    }

    private static String getImportSimpleName( JCImport importTree ) {
        return importTree.getQualifiedIdentifier() instanceof JCIdent
            ? ((JCIdent)importTree.getQualifiedIdentifier()).getName().toString()
            : ((JCFieldAccess) importTree.getQualifiedIdentifier()).getIdentifier().toString();
    }

    private static boolean isUnused( JCImport importTree, String pkgName, Set<String> usedNames ) {
        String qualifier = getImportQualifier( importTree );
        String simpleName = getImportSimpleName( importTree );

        if( qualifier.equals( "java.lang" ) || qualifier.equals( pkgName ) ) {
            return true;
        } else if( simpleName.equals( "*" ) || usedNames.contains( simpleName ) ) {
            return false;
        } else {
            return true;
        }
    }

    // Visits an AST, recording all simple names that could refer to imported
    // types and also any javadoc references that could refer to imported
    // types (`@link`, `@see`, `@throws`, etc.)
    //
    // No attempt is made to determine whether simple names occur in contexts
    // where they are type names, so there will be false positives. For example,
    // `List` is not identified as unused import below:
    //
    // ```
    // import java.util.List;
    // class List {}
    // ```
    //
    // This is still reasonably effective in practice because type names differ
    // from other kinds of names in casing convention, and simple name
    // clashes between imported and declared types are rare.
    private static class UnusedImportScanner extends TreePathScanner<Void, Set<String>> {

        /** Skip the imports themselves when checking for usage. */
        @Override
        public Void visitImport( ImportTree importTree, Set<String> usedNames ) {
            return null;
        }

        @Override
        public Void visitIdentifier( IdentifierTree tree, Set<String> usedNames ) {
            if( tree != null ) {
                usedNames.add( tree.getName().toString() );
            }
            return null;
        }

    }

}
