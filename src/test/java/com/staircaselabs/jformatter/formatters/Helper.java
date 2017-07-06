package com.staircaselabs.jformatter.formatters;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Helper {

    public static String readFileToString( String filepath ) throws IOException {
        Path path = Paths.get( filepath );
        return new String( Files.readAllBytes( path ), UTF_8 );
    }

}
