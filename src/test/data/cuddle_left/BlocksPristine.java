package com.staircaselabs.test;

import java.util.List;

@ClassPreamble (
   author = "John Doe",
   date = "3/17/2002",
)
public class SomeClass {

    static {
        System.out.println( "Instance initializer" );
    }

    int[] intArray = new int[3];
    Predicate<Integer> isOdd = n -> n % 2 != 0;

    private void doCompoundStatement() {
        {
            System.out.println( "compound statement" );
        }
    }

    private void doSynchronizedBlock() {
        System.out.println( "before synchronized block" );
        synchronized( this ) {
            System.out.println( "synchronized block" );
        }
        System.out.println( "after synchronized block" );
    }

}
