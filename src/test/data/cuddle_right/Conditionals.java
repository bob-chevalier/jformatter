package com.staircaselabs.test;

import java.util.List;

@ClassPreamble (
   author = "John Doe",
   date = "3/17/2002",
)
public class SomeClass {

    private void doUncuddledIfElseIfElse( int value ) {
        if( value == 1 ) {
            System.out.println( "one" );
        } // comment 1
        else if( value == 2 ) { // comment 2
 // comment 3
 // comment 4
            // comment 5
            System.out.println( "two" );
        }
        else {
            System.out.println( "three" );
        }

    }

    private void doCuddledIfElseIfElse( int value ) {
        if( value == 5 ){
            System.out.println( "four" );
        } else if( value == 6 ) {
            System.out.println( "five" );
        } else {
            System.out.println( "six" );
        }
    }

    private void doUncuddledIfElseIfWithComments( int value ) {
        if( value == 1 ) {
            System.out.println( "seven" );
        } // comment 1
        else if( value == 2 ){  // comment 2
            System.out.println( "eight" );
        }
    }

    private void doUncuddledIfElseWithComments( int value ) {
        if( value == 1 ) {
            System.out.println( "nine" );
        } // comment 5
        // comment 6
        else {
            System.out.println( "ten" );
        }
    }

    private void doUnbracedIfElseWithComments() {
        if( true ) {
            System.out.println( "eleven" );
}
        // comment 7
        else {
 // comment 8
            // comment 9
            System.out.println( "twelve" );
}
    }

    private void doUncuddledNested( int value ) {
        if( value == 1 ) {
            System.out.println( "thirteen" );
            if( true ) {
                System.out.println( "fourteen" );
            }
            else {
                System.out.println( "fifteen" );
            }
        }
        else if( value == 2 ) {
            System.out.println( "sixteen" );
            if( true ) {
                System.out.println( "seventeen" );
            }
            else {
                System.out.println( "eighteen" );
            }
        }
        else {
            System.out.println( "nineteen" );
            if( true ) {
                System.out.println( "twenty" );
            }
            else {
                System.out.println( "twentyone" );
            }
        }
    }

}
