public class SomeClass {

    private void doUnbracedIf() {
        if( true )
            System.out.println( "one" );
    }

    private void doUnbracedIfWithNewline() {
        if( true ) 

            System.out.println( "two" );
    }

    private void doUnbracedIfWithComments() {
        if( true ) // comment 1
            // comment 2
            System.out.println( "three" );
    }

    private void doUnbracedIfElse() {
        if( true )
            System.out.println( "four" );
        else
            System.out.println( "five" );
    }

    private void doUnbracedComplex() {
        if( true )
            System.out.println( "six" );
        else if( false )
            System.out.println( "seven" );
        else
            System.out.println( "eight" );
    }

    private void doUnbracedComplexWithComments() {
        if( true ) // comment 1
            System.out.println( "nine" ); // comment 2
        // comment 3
        else if( false ) // comment 4
            // comment 5
            System.out.println( "ten" ); // comment 6
        // comment 7
        else // comment 8
            // comment 9
            System.out.println( "eleven" );
    }

    private void doBracedIf() {
        if( true ) {
            System.out.println( "twelve" )
        }
    }

    private void doBracedIfWithNewline() {
        if( true ) 
        
        {
            System.out.println( "thirteen" );
        }
    }

    private void doBracedComplex() {
        if( true ) {
            System.out.println( "fourteen" );
        } else if( false ) {
            System.out.println( "fifteen" );
        } else {
            System.out.println( "sixteen" )
        }
    }

    private void doUnbracedNested() {
        if( true ) {
            System.out.println( "seventeen" );
            if( true )
                System.out.println( "eighteen" );
        } else if( false ) {
            System.out.println( "nineteen" );
            if( false )
                System.out.println( "twenty" );
        } else {
            System.out.println( "twentyone" )
            if( true )
                System.out.println( "twentytwo" );
        }
    }

}
