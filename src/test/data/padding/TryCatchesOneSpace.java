public class SomeClass {

    private void doSimple( String var2 ) {
        try {
            System.out.println( "one" );
        } catch( RuntimeException ex ){   
            throw ex;
        }
    }

    private void doUnionType() {
        try {
            System.out.println( "two" );
        } catch( IOException | InterruptedException ex ){
            throw ex;
        }
    }

    private void doSimpleTryWithResources( String path ) throws IOException {
        try( BufferedReader br = new BufferedReader( new FileReader( path ) ) ) {
            return br.readLine();
        }
    }

    private void doComplexTryWithResources( String path ) throws IOException {
        try( ZipFile zf = new ZipFile( zipFileName ); java.io.BufferedWriter bw = java.nio.file.Files.newBufferedWriter( outputFilePath, charset ) ) {
            System.out.println( "three" );
        }
    }

    private void doMultiLineComplexTryWithResources( String path ) throws IOException {
        try( ZipFile zf = new ZipFile( zipFileName );
                java.io.BufferedWriter bw = java.nio.file.Files.newBufferedWriter( outputFilePath, charset ) ) {
            System.out.println( "three" );
        }
    }

    private void doNested( String path ) throws IOException {
        try {
            System.out.println( "two" );
            try( BufferedReader br = new BufferedReader( new FileReader( path ) ) ) {
                return br.readLine();
            }
        } catch( Exception ex ) {
            throw ex;
        }
    }

}
