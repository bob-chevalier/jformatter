package com.staircaselabs.test;

import java.util.List;

@ClassPreamble (
   author = "John Doe",
   date = "3/17/2002",
)
public class SomeClass
{

    int[] intArray = new int[3];
    Predicate<Integer> isOdd = n -> n % 2 != 0;

    public SomeClass()
    {
        intArray[0] = 5;
        int first = intArray[0];
    }

    private void doLabeledStatement()
     {
        search:
         while(true){
             for(int idx=0; idx<10; idx++) {
                 if( idx == 3 ) 
                 {
                     break search;
                 }
             }
         }
    }

    private static class NestedClass
    {
        int variable;
        public NestedClass()
        {
        }
    }

    public static void main( String[] args )
    {
        System.out.println( "main" );
    }

}
