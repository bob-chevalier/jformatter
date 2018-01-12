package com.staircaselabs.jformatter.core;

import java.util.UUID;
import com.staircaselabs.jformatter.core.LineWrapPriority.Strategy;

public class LineWrapTag {

    private static final boolean VERBOSE = false;

    private final LineWrap type;

    /** Unique ID of the group that this tag belongs to. */
    private String groupUuid;

    /** Used for debugging.  Identifies the method that created this tag. **/
    private String source;

    /**
     * This constructor should be called when creating the first tag in a group of tags
     * i.e. When creating a tag for the first argument in a method or when creating a tag
     *      for an assignment token, which will be a group of only one token
     */
    public LineWrapTag( LineWrap type, String source ) {
        // generate a new group ID
        this( UUID.randomUUID().toString(), type, source );
    }

    /**
     * This constructor should be called when creating subsequent tags in a group of tags.
     * The supplied groupUuid should be obtained from the first tag in the group.
     */
    public LineWrapTag( String groupUuid, LineWrap type, String source ) {
        this.groupUuid = groupUuid;
        this.type = type;
        this.source = source;
    }

    public LineWrap getType() {
        return type;
    }

    public String getGroupId() {
        return groupUuid;
    }

    public void setGroupId( String groupUuid ) {
        this.groupUuid = groupUuid;
    }

    public String getSource() {
        return source;
    }

    public int getPriority( Strategy strategy ) {
        return strategy == Strategy.PRIMARY ? type.getPrimaryPriority() : type.getSecondaryPriority();
    }

    @Override
    public String toString() {
        return type + (VERBOSE ? ":" + source : "");
    }

}

