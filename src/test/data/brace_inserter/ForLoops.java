package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    private void doUnbraced() {
        for( int idx = 0; idx < 3; idx++ )
            System.out.println( "one" );
    }

    private void doUnbracedIncorrect() {
        for( int idx = 0; idx < 3; idx++ )
            System.out.println( "two" );
            System.out.println( "three" );
    }

    private void doUnbracedIncorrectWithComments() {
        for( int idx = 0; idx < 3; idx++ ) // comment 1
            // comment 2
            System.out.println( "two" ); // comment 3
            // comment 4
            System.out.println( "three" );
    }

    private void doUnbracedNested() {
        for( int idx = 0; idx < 3; idx++ ) {
            System.out.println( "four" );
            for( int pos = 0; pos < 5; pos++ )
                System.out.println( "five" );
        }
    }

    private void doUnbracedIncorrectNested() {
        for( int idx = 0; idx < 3; idx++ )
            System.out.println( "six" );
            for( int pos = 0; pos < 5; pos++ )
                System.out.println( "seven" );
    }

    private void doBraced() {
        for( int idx = 0; idx < 3; idx++ ) {
            System.out.println( "eight" );
        }
    }

    private void doBracedWithComments() {
        for( int idx = 0; idx < 3; idx++ ) { // comment 1
            // comment 2
            System.out.println( "nine" ); // comment 3
        } // comment 4
        // comment 5
        System.out.println( "ten" );
    }

    private void doBracedNested() {
        for( int idx = 0; idx < 3; idx++ ) {
            System.out.println( "eleven" );
            for( int pos = 0; pos < 5; pos++ ) {
                System.out.println( "twelve" );
            }
        }
    }

}
