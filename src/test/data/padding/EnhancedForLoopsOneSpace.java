package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    private void doUncuddled( List<String> list ) {
        for( String str : list )
        {
            System.out.println( "one" );
        }
    }

    private void doUncuddledWithComments( List<String> list ) {
        for( String str : list )// comment 1
        // comment 2
        { // comment 3
            // comment 4
            System.out.println( "two" ); // comment 5

        }
    }

    private void doUncuddledNestedWithComments( List<String> list ) {
        for( String str : list ) // comment 1
        { // comment 2
            System.out.println( "five" );
            for( String str : list )
            // comment 3
            {
                System.out.println( "six" );
            }
        }
    }

    private void doSingleLine( List<String> list ) {
        for( String str : list ){System.out.println( "one" );}
    }

}
