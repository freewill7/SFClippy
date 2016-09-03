package com.example.will.sfclippy;

import android.util.Log;

import com.example.will.sfclippy.models.CharacterPreference;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Created by will on 03/09/2016.
 */

public class RandomSelector {
    List<CharacterPreference> chars;
    private Random randomGenerator;
    private static final String TAG = "RandomSelector";

    public RandomSelector( List<CharacterPreference> chars ) {
        this.chars = chars;
        this.randomGenerator = new Random( Calendar.getInstance().getTimeInMillis() );
    }

    public int randomCharacter( ) {
        int total = 0;
        for ( CharacterPreference character : chars ) {
            total += character.score;
        }
        Log.d( TAG, "Total is " + total);

        // hack to get round lack of randomness
        int choice = randomGenerator.nextInt(total);

        Log.d( TAG, "Score to match " + choice);
        Log.d( TAG, "Iterating through " + chars.size() );

        int tally = 0;
        int idx = -1;
        for ( CharacterPreference character : chars ) {
            idx++;

            tally += character.score;
            if ( tally > choice ) {
                Log.d( TAG,
                        "Moving to " + idx + " (" + character.name + ")");
                break;
            }
        }

        return idx;
    }
}

