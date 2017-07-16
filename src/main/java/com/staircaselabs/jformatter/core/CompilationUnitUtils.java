package com.staircaselabs.jformatter.core;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;

public final class CompilationUnitUtils {

    public static JCCompilationUnit getCompilationUnit( String text ) throws FormatException {
        Context context = new Context();
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

}
