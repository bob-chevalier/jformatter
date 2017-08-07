package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    int[] intArray = new int[3];
    Predicate<Integer> isOdd = n -> n % 2 != 0;

    private void doTryCatch() {
        try {
            System.out.println( "try" );
        } 
        // catching some exception
        catch( RuntimeException ex ) {
            throw ex;
        } // comment a
        finally {
 // comment b
 // comment c
            System.out.println( "finally" );
        }
    }

    private void doTryCatchWithComments() {
        try {
 // first comment
        // second comment
 // third comment
            System.out.println( "try" );
        } 
        // comment 01
        catch( RuntimeException ex ) {
 // comment 02
 // comment 03
            // comment 04
            throw ex;
        } 
        finally {
            System.out.println( "finally" );
        }
    }

}
