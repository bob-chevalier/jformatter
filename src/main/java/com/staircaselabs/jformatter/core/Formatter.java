package com.staircaselabs.jformatter.core;

import com.staircaselabs.jformatter.debug.FormatException;

public interface Formatter {

    public String format(final String originalText) throws FormatException;

}
