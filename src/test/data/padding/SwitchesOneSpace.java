public class SomeClass {

    private void doCaseStatement() {
        switch( someInt )
        {
        case 1:
            System.out.println( "one" );
            break;
        default:
            System.out.println( "two" );
        }
    }

    private void doCaseStatement() {
        switch( getInt() ){
        case 1:
            System.out.println( "three" );
            break;
        default:
            System.out.println( "four" );
        }
    }

}
