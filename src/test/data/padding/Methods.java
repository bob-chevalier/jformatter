public class SomeClass {

    public SomeClass(  ) {
    }

    public SomeClass(Integer var1) {
        setInt((var1 + 1));
        doCuddled("two", new Integer(2));
        doCuddled(new String("three"), new Integer(3));
        doCuddled(new String("four"), new Integer(4) );
    }

    private void doComplex(  String var2, Integer var3 ) {
        System.out.println(var2, var3.toString(  ));
    }

    private void doPrinting(String var4) {
        System.out.println("four");
        System.out.println( "five" );
        System.out.println( "six");
    }

}
