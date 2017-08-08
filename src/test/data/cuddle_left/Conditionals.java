package com.staircaselabs.test;

import java.util.List;

@ClassPreamble (
   author = "John Doe",
   date = "3/17/2002",
)
public class SomeClass
{

    private void doUncuddledIfElseIfElse( int value )
    {
        if( value == 1 )
        {
            System.out.println( "one" );
        } // comment 1
        else if( value == 2 ) // comment 2
        { // comment 3
            // comment 4
            System.out.println( "two" );
        }
        else
        {
            System.out.println( "three" );
        }

    }

    private void doUncuddledWhileNested( boolean condition ) {
        while( true )
        {
            if( condition )
            {
                continue;
            }
        }
        int count = status ? 1 : 2;
    };

    private void doCuddledIfElseIfElse( int value ) {
        if( value == 5 ) {
            System.out.println( "four" );
        } else if( value == 6 ){
            System.out.println( "five" );
        } else {
            System.out.println( "six" );
        }
    }

    private void doUncuddledIfElseWithComments( int value )
    {
        if( value == 1 )
        {
            System.out.println( "seven" );
        } // comment 5
        // comment 6
        else
        {
            System.out.println( "eight" );
        }
    }

    private void doUnbracedIf() {
        if( true )
            System.out.println( "nine" );
    }

    private void doUnbracedIfElseWithComments() {
        if( true )
            System.out.println( "ten" );
        // comment 7
        else // comment 8
            // comment 9
            System.out.println( "eleven" );
    }

    private void doUnuddledNested( int value ) {
        if( value == 5 ) {
            System.out.println( "twelve" );
            if( true ) 
            {
                System.out.println( "thirteen" );
            }
        } else if( value == 6 ){
            System.out.println( "fourteen" );
            if( true ) 
            {
                System.out.println( "fifteen" );
            }
        } else {
            System.out.println( "sixteen" );
            if( true ) 
            {
                System.out.println( "seventeen" );
            }
        }
    }

}
