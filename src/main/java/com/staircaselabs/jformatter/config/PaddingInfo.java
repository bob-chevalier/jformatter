package com.staircaselabs.jformatter.config;

import com.sun.istack.internal.NotNull;

import java.util.Collections;

public class PaddingInfo {

    public final String methodName;
    public final String methodArg;
    public final String parenGrouping;
    public final String typeCast;
    public final String typeParam;
    public final String array;

    private PaddingInfo(
            @NotNull Integer methodNameSpaces,
            @NotNull Integer methodArgSpaces,
            @NotNull Integer parenGroupingSpaces,
            @NotNull Integer typeCastSpaces,
            @NotNull Integer typeParamSpaces,
            @NotNull Integer arraySpaces
    ) {
        methodName = String.join( "", Collections.nCopies( methodNameSpaces, " " ) );
        methodArg = String.join( "", Collections.nCopies( methodArgSpaces, " " ) );
        parenGrouping = String.join( "", Collections.nCopies( parenGroupingSpaces, " " ) );
        typeCast = String.join( "", Collections.nCopies( typeCastSpaces, " " ) );
        typeParam = String.join( "", Collections.nCopies( typeParamSpaces, " " ) );
        array = String.join( "", Collections.nCopies( arraySpaces, " " ) );
    }

    public static class Builder {
        private Integer methodNameSpaces = null;
        private Integer methodArgSpaces = null;
        private Integer parenGroupingSpaces = null;
        private Integer typeCastSpaces = null;
        private Integer typeParamSpaces = null;
        private Integer arraySpaces = null;

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

        public Builder array( int numSpaces ) {
            arraySpaces = numSpaces;
            return this;
        }

        public PaddingInfo build() {
            return new PaddingInfo(
                    methodNameSpaces,
                    methodArgSpaces,
                    parenGroupingSpaces,
                    typeCastSpaces,
                    typeParamSpaces,
                    arraySpaces
            );
        }
    }

}
