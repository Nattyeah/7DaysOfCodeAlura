package com.nataly.dayofcode;

import com.nataly.dayofcode.interfaces.Content;

// Marvel classes
public record Serie(String title, String url, String rating, String year) implements Content {

    @Override
    public int compareTo(Content c) {
        return this.rating().compareTo(c.rating());
    }
}
