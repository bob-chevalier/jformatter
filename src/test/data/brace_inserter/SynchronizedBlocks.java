package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    private void doUnbraced() {
        synchronized( this )
            System.out.println( "one" );
    }

    private void doUnbracedWithComments() {
        synchronized( this ) // comment 1
            // comment 2
            System.out.println( "two" ); // comment 3
    }

    private void doUnbracedNested() {
        synchronized( this )
            System.out.println( "three" );
            synchronized( mutex )
                System.out.println( "four" );
    }

}
