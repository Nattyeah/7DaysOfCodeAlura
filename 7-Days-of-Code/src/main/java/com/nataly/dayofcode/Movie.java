package com.nataly.dayofcode;

import com.nataly.dayofcode.interfaces.Content;

// Imdb classes
public record Movie(String title, String url, String rating, String year) implements Content {

    @Override
    public int compareTo(Content c) {
        return this.rating().compareTo(c.rating());
    }
}
