package com.example.will.sfclippy.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Represents a users preference for a character (higher score is more favoured).
 */

@IgnoreExtraProperties
public class CharacterPreference {
    public String name;
    public int score;

    public CharacterPreference( ) {
        // default constructor
    }

    public CharacterPreference( String name, int score ) {
        this.name = name;
        this.score = score;
    }
}
