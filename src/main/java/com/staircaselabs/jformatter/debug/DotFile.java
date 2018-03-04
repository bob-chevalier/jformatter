package com.staircaselabs.jformatter.debug;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DotFile {

    private final Map<String, String> nodeLabels = new HashMap<>();
    private final Map<String, List<String>> edges = new HashMap<>();

    public void addNode( String nodeId, String nodeLabel ) {
        nodeLabels.put( nodeId, nodeLabel );
        edges.put( nodeId, new ArrayList<>() );
    }

    public void addEdge( String parentNodeId, String childNodeId ) {
        edges.get( parentNodeId ).add( childNodeId );
    }

    public void write( String path )  {
        List<String> lines = new ArrayList<>();
        lines.add( "graph segmented_line {" );
        lines.add( "    ordering = out;" );

        // add a label entry for each node
        nodeLabels.entrySet()
                .forEach( e -> lines.add( String.format( "    \"%s\" [label=\"%s\"]", e.getKey(), e.getValue() ) ) );

        // define edges
        for( Map.Entry<String, List<String>> entry : edges.entrySet() ) {
            entry.getValue().forEach( c -> lines.add( String.format( "    \"%s\" -- \"%s\"", entry.getKey(), c ) ) );
        }

        lines.add( "}" );

        // write out a dotfile that is capable of being read in by Graphviz
        try {
            Files.write(Paths.get(path), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
        } catch( IOException ex ) {
            throw new RuntimeException( "Unable to write dotfile to " + path, ex );
        }
    }
}
