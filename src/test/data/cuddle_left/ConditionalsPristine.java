package com.staircaselabs.test;

import java.util.List;

@ClassPreamble (
   author = "John Doe",
   date = "3/17/2002",
)
public class SomeClass {

    private void doCompoundIfElse( int value ) {
        if( value == 1 ) {
            System.out.println( "incorrect compound if" );
        } // comment 1
        else if( value == 2 ) {
 // comment 2
         // comment 3
            // comment 4
            System.out.println( "incorrect compound else-if" );
        }
        else {
            System.out.println( "incorrect compound else" );
        }

    }

    private void doConditionalExpression( boolean condition ) {
        while( true ) {
            if( condition ) {
                continue;
            }
        }
        int count = status ? 1 : 2;
    };

    private void doCorrectCompoundIfElse( int value ) {
        if( value == 5 ) {
            System.out.println( "correct if" );
        } else if( value == 6 ) {
            System.out.println( "correct else-if" );
        } else {
            System.out.println( "correct else" );
        }
    }

    private void doIfElse( int value ) {
        if( value == 1 ) {
            System.out.println( "incorrect if" );
        } // first comment
        // second comment
        else {
            System.out.println( "incorrect else" );
        }
    }

    private void doUnbracedIf() {
        if( true )
            System.out.println( "it's true" );
    }

    private void doUnbracedCompoundIfElse() {
        if( true )
            System.out.println( "it's true" );
        // comment line
        else // comment B
            // comment C
            System.out.println( "it's not true" );
    }

}
