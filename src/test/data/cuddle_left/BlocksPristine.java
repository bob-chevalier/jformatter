package com.staircaselabs.test;

import java.util.List;

@ClassPreamble (
   author = "John Doe",
   date = "3/17/2002",
)
public class SomeClass {

    static {
        System.out.println( "one" );
    }

    int[] intArray = new int[3];
    Predicate<Integer> isOdd = n -> n % 2 != 0;

    private void doCompoundStatement() {
        {
            System.out.println( "two" );
        }
    }

    private void doSynchronizedBlock() {
        System.out.println( "three" );
        synchronized( this ) {
            System.out.println( "four" );
            for( String str : list ) {
                System.out.println( "five" );
            }
        }
        System.out.println( "six" );
    }

}
