package com.staircaselabs.test;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.Truth8.assertThat;

import com.google.common.collect.Sets;
import com.staircaselabs.OtherClass;
import java.util.*;
import org.junit.AfterClass;  
import org.junit.BeforeClass;
import org.junit.Test;

public class SomeClass {

    @BeforeClass
    public static void loadExpectedOutputs() throws IOException {
        HashSet<String> stringSet = Sets.newHashSet( "one", "two" );
        HashSet<OtherClass> otherSet = Sets.newHashSet( new OtherClass( 1 ), new OtherClass( 2 ) );
    }

    @Test
    public void testMethod() {
        String hello = "hello";
        assertThat( hello ).isEqualTo( "hello" );
    }

    @Test
    public void anotherTestMethod() {
        String goodbye = "goodbye";
        assertThat( goodbye ).isEqualTo( "goodbye" );
    }

}
