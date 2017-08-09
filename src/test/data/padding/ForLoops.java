package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    private void doUnpadded() {
        for(int idx = 0; idx < 3; idx++)
            System.out.println( "one" );
    }

    private void doExtraPadding() {
        for(  int idx = 0; idx < 3;   idx++   )
        {
            System.out.println( "two" );  
            System.out.println( "three" );
        }
    }

    private void doUnpaddedNested() {
        for(int idx = 0; idx < 3; idx++){
            System.out.println( "four" );
            for(int pos = 0; pos < 5; pos++) {
                System.out.println( "five" );
            }
        }
    }

}
