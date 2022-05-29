package com.nataly.dayofcode.interfaces;

public interface Content extends Comparable<Content> {

    String title();

    String url();

    String rating();

    String year();
}
