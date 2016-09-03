package com.example.will.sfclippy.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents the result of a battle.
 */

@IgnoreExtraProperties
public class BattleResult {
    public String date;
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
                         String p1Id,
                         String p1Character,
                         String p2Id,
                         String p2Character,
                         String winnerId ) {
        this.date = dateFormat.format(date);
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

    public Date dateAsDate( ) throws ParseException {
        return dateFormat.parse(date);
    }
}
