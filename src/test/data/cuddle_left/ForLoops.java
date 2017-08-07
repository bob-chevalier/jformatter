package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    private void doUnbraced() {
        for( int idx = 0; idx < 3; idx++ )
            System.out.println( "one" );
    }

    private void doUncuddled() {
        for( int idx = 0; idx < 3; idx++ )
        {
            System.out.println( "two" );  
            System.out.println( "three" );
        }
    }

    private void doUnuddledNested() {
        for( int idx = 0; idx < 3; idx++ )
        {
            System.out.println( "four" );
            for( int pos = 0; pos < 5; pos++ )
            {
                System.out.println( "five" );
            }
        }
    }

    private void doUncuddledWithComments() {
        for( int idx = 0; idx < 3; idx++ ) 
        { // comment 1
            System.out.println( "six" );
        }
    }

    private void doUncuddleNestedWithComments() {
        for( int idx = 0; idx < 3; idx++ ) // comment 1
        { // comment 2
            // comment 3
            System.out.println( "seven" ); // comment 3
        } // comment 4
        // comment 5
        System.out.println( "eight" );
    }

    private void doCuddledNested() {
        for( int idx = 0; idx < 3; idx++ ) {
            System.out.println( "nine" );
            for( int pos = 0; pos < 5; pos++ ) {
                System.out.println( "ten" );
            }
        }
    }

}
