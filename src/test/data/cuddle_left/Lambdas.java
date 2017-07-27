package com.staircaselabs.test;

import java.util.List;

@ClassPreamble (
   author = "John Doe",
   date = "3/17/2002",
)
public class SomeClass {

    private boolean doLambda()
    {
        StateListener.addListener( (oldState, newState) ->
                {
            System.out.println("old: " + oldState);
            System.out.println("new: " + newState);
        }
        )
    }

}
