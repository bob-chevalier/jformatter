public class SomeClass {

    public SomeClass(Integer var1) {
        setInt((var1 + 1));
        Integer var2 = new Integer(2);
        Integer var3 = new Integer(3);
        Integer var4 = new Integer(4);
        Integer var5 = new Integer(5);
        Integer var6 = new Integer(6);
    }

    private static void main(String[] args) {
        SomeClass inst1 = new SomeClass();
        SomeClass inst2 = new SomeClass(new Integer(2));
        SomeClass inst3 = new SomeClass(new Integer(3));
        SomeClass inst4 = new SomeClass(new Integer(4));
        SomeClass inst5 = new SomeClass(new Integer(5));
        SomeClass inst6 = new SomeClass(new Integer(6));
        SomeClass inst7 = new SomeClass(new Integer(7));
    }

}
