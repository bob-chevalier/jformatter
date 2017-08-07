package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    private void doUnbraced() {
        do {
            System.out.println( "one" );
}
        while( true );
    }

    private void doUncuddled() {
        do {
            System.out.println( "two" );
        } while( true );
    }

    private void doUncuddledWithComments() {
        do {
 // comment 1
        // comment 2
 // comment 3
            // comment 4
            System.out.println( "three" ); // comment 5
        // comment 6
        } while( true ); // comment 7
    }

    private void doCuddledWithComments() {
        do {
 // comment 1
            // comment 2
            System.out.println( "four" ); // comment 3
        // comment 4
        } while( true ); // comment 5
    }

}
