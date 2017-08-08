package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    int[] intArray = new int[3];
    Predicate<Integer> isOdd = n -> n % 2 != 0;

    private void doCuddled() {
        try {
            System.out.println( "one" );
        } catch( RuntimeException ex ){
            throw ex;
        } finally {
            System.out.println( "two" );
        }
    }

    private void doUncuddledWithComments() {
        try {
            System.out.println( "three" );
        } catch( RuntimeException ex ) {
          // comment 1
        // comment 2
 // comment 3
            throw ex;
        } finally {
         // comment 4
            System.out.println( "four" );
        }
    }

    private void doUncuddledNested() {
        try {
            System.out.println( "five" );
        } catch( RuntimeException ex ) {         
        // comment 01
            try {
                System.out.println( "six" );
            } catch( IOException ioe ) {
                throw ioe
            }
            throw ex;
        } 
    }

}
