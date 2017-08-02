package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    private void doUnbraced( List<String> list ) {
        for( String str : list ) {
            System.out.println( "one" );
}
    }

    private void doUnbracedIncorrect( List<String> list ) {
        for( String str : list ) {
            System.out.println( "two" );
}
            System.out.println( "three" );
    }

    private void doUnbracedIncorrectWithComments( List<String> list ) {
        for( String str : list ) { // comment 1
            // comment 2
            System.out.println( "two" ); // comment 3
}
            // comment 4
            System.out.println( "three" );
    }

    private void doUnbracedNested( List<String> list ) {
        for( String str : list ) {
            System.out.println( "four" );
            for( String str : list ) {
                System.out.println( "five" );
}
        }
    }

    private void doUnbracedIncorrectNested( List<String> list ) {
        for( String str : list ) {
            System.out.println( "six" );
}
            for( String str : list ) {
                System.out.println( "seven" );
}
    }

    private void doBraced( List<String> list ) {
        for( String str : list ) {
            System.out.println( "eight" );
        }
    }

    private void doBracedWithComments( List<String> list ) {
        for( String str : list ) { // comment 1
            // comment 2
            System.out.println( "nine" ); // comment 3
        } // comment 4
        // comment 5
        System.out.println( "ten" );
    }

    private void doBracedNested( List<String> list ) {
        for( String str : list ) {
            System.out.println( "eleven" );
            for( String str : list ) {
                System.out.println( "twelve" );
            }
        }
    }

}
