package com.example.will.sfclippy.models;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Keeps track of results for a particular pairing.
 */

public class BattleCounter {
    public int wins;
    public int battles;
    public String lastVictory;
    public int winsSinceLoss;
    public String lastDefeat;
    public int lossesSinceLastWin;
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            Locale.UK );
    private static final String TAG = "BattleCounter";


    public BattleCounter( ) {
        wins = 0;
        battles = 0;
        lastVictory = null;
        winsSinceLoss = 0;
        lastDefeat = null;
        lossesSinceLastWin = 0;
    }

    public void recordWin( Date date ) {
        wins++;
        winsSinceLoss++;
        lastVictory = format.format(date);
        lossesSinceLastWin = 0;
        battles++;
    }

    public void recordLoss( Date date ) {
        battles++;
        winsSinceLoss = 0;
        lossesSinceLastWin++;
        lastDefeat = format.format(date);
    }

    public Date lastVictoryAsDate( ) {
        Date ret = null;
        if ( null != lastVictory ) {
            try {
                ret = format.parse(lastVictory);
            } catch ( ParseException parse ) {
                Log.e( TAG, "lastVictoryAsDate", parse);
            }
        }
        return ret;
    }

    public Date lastDefeatAsDate( ) {
        Date ret = null;
        if ( null != lastDefeat ) {
            try {
                ret = format.parse(lastDefeat);
            } catch (ParseException parse) {
                Log.e(TAG, "lastLossAsDate, parse");
            }
        }
        return ret;
    }

    public int getLosses( ) {
        return battles - wins;
    }

    public int getDifference( ) {
        return wins - (battles - wins);
    }

    public int getWinPercentage( ) {
        return (100 * wins) / battles;
    }

    public int getWinningRun( ) {
        return winsSinceLoss - lossesSinceLastWin;
    }
}
