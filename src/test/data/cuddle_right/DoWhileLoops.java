package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    private void doUncuddled() {
        do {
            System.out.println( "one" );
}
        while( true );
    }

    private void doUncuddledSingleLine() {
        do {
            System.out.println( "one" ); }
        // comment 1
        while( true );
    }

    private void doUncuddledWithComments() {
        do {
            System.out.println( "two" ); // comment 1
            // comment 2
        } // comment 3
        // comment 4
        while( true );
    }

    private void doCuddledWithComments() {
        do {
 // comment 1
            // comment 2
            System.out.println( "four" );
        // comment 3
        } while( true ); // comment 4
    }

}
