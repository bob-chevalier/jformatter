package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    private void doUnbraced() {
        do 
            System.out.println( "one" );
        while( true );
    }

    private void doUnbracedWithComments() {
        do // comment 1
            // comment 2
            System.out.println( "two" ); // comment 3
        // comment 4
        while( true ); // comment 5
    }

    private void doUnbracedIncorrectlyNested() {
        do 
            System.out.println( "three" );
            System.out.println( "four" );
        while( true );
    }

    private void doBraced() {
        do {
            System.out.println( "five" );
        } while( true );
    }

    private void doBracedWithComments() {
        do { // comment 1
            // comment 2
            System.out.println( "six" ); // comment 3
        // comment 4
        } while( true ); // comment 5
    }

    private void doBracedNested() {
        do {
            System.out.println( "seven" );
            System.out.println( "eight" );
        } while( true );
    }

}
