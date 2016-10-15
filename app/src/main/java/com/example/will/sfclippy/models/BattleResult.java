package com.example.will.sfclippy.models;

import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents the result of a battle.
 */

@IgnoreExtraProperties
public class BattleResult {
    private static final String TAG = "BattleResult";
    public String date;
    public String battleId;
    public String p1Id;
    public String p1Character;
    public String p2Id;
    public String p2Character;
    public String winnerId;
    final static private SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BattleResult( ) {
        // default constructor
    }

    public BattleResult( Date date,
                         String battleId,
                         String p1Id,
                         String p1Character,
                         String p2Id,
                         String p2Character,
                         String winnerId ) {
        this.date = dateFormat.format(date);
        this.battleId = battleId;
        this.p1Id = p1Id;
        this.p1Character = p1Character;
        this.p2Id = p2Id;
        this.p2Character = p2Character;
        this.winnerId = winnerId;
    }

    public String characterFor( String playerId ) {
        if ( playerId.equals(p1Id)) {
            return p1Character;
        } else if ( playerId.equals(p2Id)) {
            return p2Character;
        } else {
            return "unknown";
        }
    }

    public String opponentFor( String playerId ) {
        if ( playerId.equals(p1Id) ) {
            return p2Character;
        } else if ( playerId.equals(p2Id)) {
            return p1Character;
        } else {
            return null;
        }
    }

    public Date dateAsDate( ) {
        Date ret = null;
        try {
            ret = dateFormat.parse(date);
        } catch ( ParseException except ) {
            Log.e( TAG, "Problem parsing date", except);
        }
        return ret;
    }
}
