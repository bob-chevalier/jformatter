package com.staircaselabs.jformatter.core;

import com.sun.istack.internal.NotNull;
import com.sun.source.tree.Tree.Kind;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LineWrapInfo {

    public final int maxLineWidth;
    public final boolean oneArrayElementPerLine;
    public final boolean oneMethodArgPerLine;
    public final boolean closingBracesOnNewLine;
    public final boolean closingParensOnNewLine;
    private final Map<LineWrap, Integer> numTabs = new HashMap<>();
    private final Set<Kind> wrappableMemberSelectTypes = new HashSet<>();

    private LineWrapInfo(
            @NotNull Integer maxLineWidth,
            @NotNull Boolean oneArrayElementPerLine,
            @NotNull Boolean oneMethodArgPerLine,
            @NotNull Boolean closingBracesOnNewLine,
            @NotNull Boolean closingParensOnNewLine,
            @NotNull Boolean allowLineWrapAtMethodInvocationMemberSelect,
            @NotNull Boolean allowLineWrapAtNewClassMemberSelect,
            @NotNull Boolean allowLineWrapAtIdentifierMemberSelect,
            @NotNull Integer arrayLineWrapTabs,
            @NotNull Integer assignmentLineWrapTabs,
            @NotNull Integer extendsLineWrapTabs,
            @NotNull Integer implementsLineWrapTabs,
            @NotNull Integer memberSelectLineWrapTabs,
            @NotNull Integer methodArgumentLineWrapTabs,
            @NotNull Integer ternaryLineWrapTabs,
            @NotNull Integer throwsLineWrapTabs,
            @NotNull Integer unionLineWrapTabs,
            @NotNull Integer unboundListItemLineWrapTabs
    ) {
        this.maxLineWidth = maxLineWidth;
        this.oneArrayElementPerLine = oneArrayElementPerLine;
        this.oneMethodArgPerLine = oneMethodArgPerLine;
        this.closingBracesOnNewLine = closingBracesOnNewLine ;
        this.closingParensOnNewLine = closingParensOnNewLine;

        numTabs.put( LineWrap.ARRAY, arrayLineWrapTabs );
        numTabs.put( LineWrap.ASSIGNMENT, assignmentLineWrapTabs );
        numTabs.put( LineWrap.EXTENDS, extendsLineWrapTabs );
        numTabs.put( LineWrap.IMPLEMENTS, implementsLineWrapTabs );
        numTabs.put( LineWrap.MEMBER_SELECT, memberSelectLineWrapTabs );
        numTabs.put( LineWrap.METHOD_ARG, methodArgumentLineWrapTabs );
        numTabs.put( LineWrap.TERNARY, ternaryLineWrapTabs );
        numTabs.put( LineWrap.THROWS, throwsLineWrapTabs );
        numTabs.put( LineWrap.UNION, unionLineWrapTabs );
        numTabs.put( LineWrap.UNBOUND_LIST_ITEM, unboundListItemLineWrapTabs );

        if( allowLineWrapAtMethodInvocationMemberSelect ) {
            wrappableMemberSelectTypes.add( Kind.METHOD_INVOCATION );
        }
        if( allowLineWrapAtNewClassMemberSelect ) {
            wrappableMemberSelectTypes.add( Kind.NEW_CLASS );
        }
        if( allowLineWrapAtIdentifierMemberSelect ) {
            wrappableMemberSelectTypes.add( Kind.IDENTIFIER );
        }
    }

    public int tabsToInsert( LineWrap type ) {
        return numTabs.get(type);
    }

    public boolean allowMemberSelectLineWrap( Kind memberSelectKind ) {
        return wrappableMemberSelectTypes.contains( memberSelectKind );
    }

    public static class Builder {
        private Integer maxLineWidth = null;
        private Boolean oneArrayElementPerLine = null;
        private Boolean oneMethodArgPerLine = null;
        private Boolean closingBracesOnNewLine = null;
        private Boolean closingParensOnNewLine = null;
        private Boolean allowLineWrapAtMethodInvocationMemberSelect = null;
        private Boolean allowLineWrapAtNewClassMemberSelect = null;
        private Boolean allowLineWrapAtIdentifierMemberSelect = null;
        private Integer arrayLineWrapTabs = null;
        private Integer assignmentLineWrapTabs = null;
        private Integer extendsLineWrapTabs = null;
        private Integer implementsLineWrapTabs = null;
        private Integer memberSelectLineWrapTabs = null;
        private Integer methodArgumentLineWrapTabs = null;
        private Integer ternaryLineWrapTabs = null;
        private Integer throwsLineWrapTabs = null;
        private Integer unionLineWrapTabs = null;
        private Integer unboundListItemLineWrapTabs = null;

        public Builder maxLineWidth( int maxLineWidth ) {
            this.maxLineWidth = maxLineWidth;
            return this;
        }

        public Builder oneArrayElementPerLine( boolean oneArrayElementPerLine ) {
            this.oneArrayElementPerLine = oneArrayElementPerLine;
            return this;
        }

        public Builder oneMethodArgPerLine( boolean oneMethodArgPerLine ) {
            this.oneMethodArgPerLine = oneMethodArgPerLine;
            return this;
        }

        public Builder closingBracesOnNewLine( boolean closingBracesOnNewLine ) {
            this.closingBracesOnNewLine = closingBracesOnNewLine;
            return this;
        }

        public Builder closingParensOnNewLine( boolean closingParensOnNewLine ) {
            this.closingParensOnNewLine = closingParensOnNewLine;
            return this;
        }

        public Builder allowLineWrapAtMethodInvocationMemberSelect( boolean isAllowed ) {
            this.allowLineWrapAtMethodInvocationMemberSelect = isAllowed;
            return this;
        }

        public Builder allowLineWrapAtNewClassMemberSelect( boolean isAllowed ) {
            this.allowLineWrapAtNewClassMemberSelect = isAllowed;
            return this;
        }

        public Builder allowLineWrapAtIdentifierMemberSelect( boolean isAllowed ) {
            this.allowLineWrapAtIdentifierMemberSelect = isAllowed;
            return this;
        }

        public Builder arrayLineWrapTabs( int numTabs ) {
            this.arrayLineWrapTabs = numTabs;
            return this;
        }

        public Builder assignmentLineWrapTabs( int numTabs ) {
            this.assignmentLineWrapTabs = numTabs;
            return this;
        }

        public Builder extendsLineWrapTabs( int numTabs ) {
            this.extendsLineWrapTabs = numTabs;
            return this;
        }

        public Builder implementsLineWrapTabs( int numTabs ) {
            this.implementsLineWrapTabs = numTabs;
            return this;
        }

        public Builder memberSelectLineWrapTabs( int numTabs ) {
            this.memberSelectLineWrapTabs = numTabs;
            return this;
        }

        public Builder methodArgumentLineWrapTabs( int numTabs ) {
            this.methodArgumentLineWrapTabs = numTabs;
            return this;
        }

        public Builder ternaryLineWrapTabs( int numTabs ) {
            this.ternaryLineWrapTabs = numTabs;
            return this;
        }

        public Builder throwsLineWrapTabs( int numTabs ) {
            this.throwsLineWrapTabs = numTabs;
            return this;
        }

        public Builder unionLineWrapTabs( int numTabs ) {
            this.unionLineWrapTabs = numTabs;
            return this;
        }

        public Builder unboundListItemLineWrapTabs( int numTabs ) {
            this.unboundListItemLineWrapTabs = numTabs;
            return this;
        }

        public LineWrapInfo build() {
            return new LineWrapInfo(
                maxLineWidth,
                oneArrayElementPerLine,
                oneMethodArgPerLine,
                closingBracesOnNewLine,
                closingParensOnNewLine,
                allowLineWrapAtMethodInvocationMemberSelect,
                allowLineWrapAtNewClassMemberSelect,
                allowLineWrapAtIdentifierMemberSelect,
                arrayLineWrapTabs,
                assignmentLineWrapTabs,
                extendsLineWrapTabs,
                implementsLineWrapTabs,
                memberSelectLineWrapTabs,
                methodArgumentLineWrapTabs,
                ternaryLineWrapTabs,
                throwsLineWrapTabs,
                unionLineWrapTabs,
                unboundListItemLineWrapTabs
            );
        }
    }

}
