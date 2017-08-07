package com.staircaselabs.test;

import java.util.List;

@ClassPreamble (
   author = "John Doe",
   date = "3/17/2002",
)
public class SomeClass {

    private void doUncuddled() {
        while( true ) {
            System.out.println( "one" );
        } 
    }

    private void doUncuddledNestedSingleLine() {
        while( true ) {
            int count = 0;
            while( count < 3 ) {
count++; }
        }
    }

}
