package com.staircaselabs.jformatter.core;

import java.util.Collections;

public class Padding {

    public final String methodName;
    public final String methodArg;
    public final String parenGrouping;
    public final String typeCast;
    public final String typeParam;

    private Padding(
            int methodNameSpaces,
            int methodArgSpaces,
            int parenGroupingSpaces,
            int typeCastSpaces,
            int typeParamSpaces
    ) {
        methodName = String.join( "", Collections.nCopies( methodNameSpaces, " " ) );
        methodArg = String.join( "", Collections.nCopies( methodArgSpaces, " " ) );
        parenGrouping = String.join( "", Collections.nCopies( parenGroupingSpaces, " " ) );
        typeCast = String.join( "", Collections.nCopies( typeCastSpaces, " " ) );
        typeParam = String.join( "", Collections.nCopies( typeParamSpaces, " " ) );
    }

    public static class Builder {
        private int methodNameSpaces = 0;
        private int methodArgSpaces = 1;
        private int parenGroupingSpaces = 0;
        private int typeCastSpaces = 0;
        private int typeParamSpaces = 0;

        public Builder methodName( int numSpaces ) {
            methodNameSpaces = numSpaces;
            return this;
        }

        public Builder methodArg( int numSpaces ) {
            methodArgSpaces = numSpaces;
            return this;
        }

        public Builder parenGrouping( int numSpaces ) {
            parenGroupingSpaces = numSpaces;
            return this;
        }

        public Builder typeCast( int numSpaces ) {
            typeCastSpaces = numSpaces;
            return this;
        }

        public Builder typeParam( int numSpaces ) {
            typeParamSpaces = numSpaces;
            return this;
        }

        public Padding build() {
            return new Padding(
                    methodNameSpaces,
                    methodArgSpaces,
                    parenGroupingSpaces,
                    typeCastSpaces,
                    typeParamSpaces
            );
        }
    }

}
