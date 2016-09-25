package com.example.will.sfclippy.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Comparator;

/**
 * Represents a users preference for a character (higher score is more favoured).
 */

@IgnoreExtraProperties
public class CharacterPreference {
    public String name;
    public int score;
    public BattleCounter statistics;

    public CharacterPreference( ) {
        // default constructor
    }

    public CharacterPreference( String name, int score ) {
        this.name = name;
        this.score = score;
        statistics = new BattleCounter();
    }

    /**
     * Order characters by score (highest first).
     */
    public static class DescendingScore implements Comparator<CharacterPreference> {
        public int compare(CharacterPreference lhs, CharacterPreference rhs) {
            // higher rating goes first
            int diff = rhs.score - lhs.score;
            if ( 0 == diff ) {
                // then order by ascending alphabetical
                diff = lhs.name.compareTo( rhs.name );
            }

            return diff;
        }
    }
}
