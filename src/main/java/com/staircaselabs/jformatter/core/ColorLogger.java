package com.staircaselabs.jformatter.core;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public final class ColorLogger extends SimpleFormatter {

    protected static final String COLOR_RESET = "\u001b[0m";

    protected static final String COLOR_SEVERE = "\u001b[1;31m";
    protected static final String COLOR_WARNING = "\u001b[1;33m";
    protected static final String COLOR_INFO = "\u001b[1;32m";
    protected static final String COLOR_CONFIG = "\u001b[1;34m";
    protected static final String COLOR_FINE = "\u001b[1;36m";
    protected static final String COLOR_FINER = "\u001b[1;35m";
    protected static final String COLOR_FINEST = "\u001b[1;30m";

    public String formatMessage(LogRecord record) {
        return getPrefix(record.getLevel()) + record.getMessage() + COLOR_RESET;
    }

    private String getPrefix(Level level) {
        if( level == Level.SEVERE ) {
            return COLOR_SEVERE;
        } else if( level == Level.WARNING ) {
            return COLOR_WARNING;
        } else if( level == Level.INFO ) {
            return COLOR_INFO;
        } else if( level == Level.CONFIG ) {
            return COLOR_CONFIG;
        } else if( level == Level.FINE ) {
            return COLOR_FINE;
        } else if( level == Level.FINER ) {
            return COLOR_FINER;
        } else if( level == Level.FINEST ) {
            return COLOR_FINEST;
        } else {
            return COLOR_RESET;
        }
    }
}
