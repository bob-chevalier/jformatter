package com.staircaselabs.jformatter.core;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigLoader {

    public static Config parse( String configPath ) {
        try {
            Path fullPath = Paths.get(configPath).toRealPath();
            YamlReader reader = new YamlReader(new FileReader(fullPath.toString()));
            return reader.read(Config.class);
        } catch( IOException ex ) {
            throw new RuntimeException( "Unable to parse config: " + configPath, ex );
        }
    }

}

