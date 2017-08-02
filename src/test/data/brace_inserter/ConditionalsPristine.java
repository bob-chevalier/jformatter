public class SomeClass {

    private void doUnbracedIf() {
        if( true ) {
            System.out.println( "unbraced if" );
}
    }

    private void doUnbracedIfWithNewline() {
        if( true ) {

            System.out.println( "braced if with newline" );
}
    }

    private void doUnbracedIfWithComments() {
        if( true ) { // comment 1
            // comment 2
            System.out.println( "unbraced if" );
}
    }

    private void doUnbracedIfElse() {
        if( true ) {
            System.out.println( "unbraced if" );
}
        else {
            System.out.println( "unbraced else" );
}
    }

    private void doUnbracedComplex() {
        if( true ) {
            System.out.println( "unbraced if" );
}
        else if( false ) {
            System.out.println( "unbraced else-if" );
}
        else {
            System.out.println( "unbraced else" );
}
    }

    private void doUnbracedComplexWithComments() {
        if( true ) { // comment 1
            System.out.println( "unbraced if" ); // comment 2
}
        // comment 3
        else if( false ) { // comment 4
            // comment 5
            System.out.println( "unbraced else-if" ); // comment 6
}
        // comment 7
        else { // comment 8
            // comment 9
            System.out.println( "unbraced else" );
}
    }

    private void doBracedIf() {
        if( true ) {
            System.out.println( "" )
        }
    }

    private void doBracedIfWithNewline() {
        if( true ) 
        
        {
            System.out.println( "braced if" );
        }
    }

    private void doBracedComplex() {
        if( true ) {
            System.out.println( "unbraced if" );
        } else if( false ) {
            System.out.println( "unbraced else-if" );
        } else {
            System.out.println( "unbraced else" )
        }
    }

}
