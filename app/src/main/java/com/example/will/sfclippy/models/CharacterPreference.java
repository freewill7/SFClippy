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

    public int getMaximumWins( int totalBattles ) {
        int ret = totalBattles;
        if ( null != statistics ) {
            ret = statistics.getMaximumWins(totalBattles);
        }
        return ret;
    }

    public int getBattleCount( ) {
        int ret = 0;
        if ( null != statistics ) {
            ret = statistics.battles;
        }
        return ret;
    }

    public int getWinCount( ) {
        int ret = 0;
        if ( null != statistics ) {
            ret = statistics.wins;
        }
        return ret;
    }

    public int getLossCount( ) {
        int ret = 0;
        if ( null != statistics ) {
            ret = statistics.getLosses();
        }
        return ret;
    }

    public int getDifference( ) {
        int ret = 0;
        if ( null != statistics ) {
            ret = statistics.getDifference();
        }
        return ret;
    }

    public int getWinPercentage( ) {
        int ret = 0;
        if ( null != statistics ) {
            ret = statistics.getWinPercentage();
        }
        return ret;
    }

    public int getWinningRun( ) {
        int ret = 0;
        if ( null != statistics ) {
            ret = statistics.getWinningRun();
        }
        return ret;
    }

    public int getPredictedWins( int maxBattles ) {
        int ret = maxBattles / 2;
        if ( null != statistics ) {
            ret = statistics.getPredictedWins(maxBattles);
        }
        return ret;
    }
}
