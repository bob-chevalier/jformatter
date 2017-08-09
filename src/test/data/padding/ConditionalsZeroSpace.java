package com.staircaselabs.test;

import java.util.List;

public class SomeClass {

    private void doIfElseIfElseWithComments(int value){
        if(value == 1) {
            System.out.println("one");
        } else if(value == 2) // comment 1
        { // comment 2
            // comment 3
            System.out.println("two");
        } else {
            System.out.println("three");
        }

    }

    private void doUnbracedIf() {
        if(true)
            System.out.println("four");
    }

    private void doUnbracedIfElseWithComments() {
        if(true)
            System.out.println("five");
        // comment 1
        else // comment 2
            // comment 3
            System.out.println("six");
    }

    private void doUncuddledNested(int value) {
        if(value == 5) {
            System.out.println("seven");
            if(true) 
            {
                System.out.println("eight");
            }
        } else if(value == 6){
            System.out.println("nine");
            if(true) 
            {
                System.out.println("ten");
            }
        } else {
            System.out.println("eleven");
            if(true) 
            {
                System.out.println("twelve");
            }
        }
    }

}
