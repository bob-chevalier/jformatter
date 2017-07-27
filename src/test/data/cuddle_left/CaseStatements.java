package com.staircaselabs.test;

import java.util.List;

@ClassPreamble (
   author = "John Doe",
   date = "3/17/2002",
)
public class SomeClass
{

    private void doCaseStatement() {
        switch( someInt ) 
        {
        case 1:
            System.out.println( "one" );
            break;
        case 2:
            {
            System.out.println( "two" );
            break;
            }
        default:
            System.out.println( "default" );
        }
    }

}
