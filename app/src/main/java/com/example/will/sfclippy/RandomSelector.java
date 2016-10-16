package com.example.will.sfclippy;

import android.util.Log;

import com.example.will.sfclippy.models.CharacterPreference;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Class for randomly selecting a character.
 */

public class RandomSelector {
    List<CharacterPreference> chars;
    private Random randomGenerator;
    private static final String TAG = "RandomSelector";

    private void init( ) {
        this.randomGenerator = new Random( Calendar.getInstance().getTimeInMillis() );
    }

    public RandomSelector( ) {
        init();
    }

    public RandomSelector( List<CharacterPreference> chars ) {
        init();
        this.chars = chars;
    }

    public void setCharacters( List<CharacterPreference> chars ) {
        this.chars = chars;
    }

    public int getTotal( ) {
        int total = 0;
        for ( CharacterPreference character : chars ) {
            total += getCharacterScore(character);
        }

        if ( 0 == total ) {
            total = 1;
        }

        return total;
    }

    public int getCharacterScore( CharacterPreference character ) {
        return character.score - 1;

    }
    public int percentageChance( CharacterPreference character ) {
        int total = getTotal();
        return (100 * getCharacterScore(character)) / total;
    }

    public int randomCharacter( ) {
        int total = getTotal();
        Log.d( TAG, "Total is " + total);

        // hack to get round lack of randomness
        int choice = randomGenerator.nextInt(total);

        Log.d( TAG, "Score to match " + choice);
        Log.d( TAG, "Iterating through " + chars.size() );

        int tally = 0;
        int idx = -1;
        for ( CharacterPreference character : chars ) {
            idx++;

            tally += getCharacterScore(character);
            if ( tally > choice ) {
                Log.d( TAG,
                        "Moving to " + idx + " (" + character.name + ")");
                break;
            }
        }

        return idx;
    }
}

