package com.staircaselabs.test;

import java.util.List;

@ClassPreamble (
   author = "John Doe",
   date = "3/17/2002",
)
public class SomeClass {

    private void doDoWhileLoop() {
        do {
            System.out.println( "doing while" );
        } 
        while( true );
    }

    private void doEnhancedForLoop() {
        List<Integer> list = Arrays.asList( 1, 2, 3 );
        for( Integer num : list ) {
            System.out.println( num );
        }
    }

    private void doForLoop() {
        for( int idx = 0; idx < 3; idx++ ) {
            if( idx == 1 ) {
                break;
            }
        }
    }

    private void doWhileLoop() {
        while( true ) {
            System.out.println( "while" );
        }
    }

}
