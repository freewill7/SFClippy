package com.example.will.sfclippy.models;

/**
 * Created by will on 03/09/2016.
 */

public class PlayerInfo {
    public String playerId;
    public String playerName;

    public PlayerInfo( ) {
        // dummy constructor needed for json
    }

    public PlayerInfo( String playerId, String playerName ) {
        this.playerId = playerId;
        this.playerName = playerName;
    }
}
