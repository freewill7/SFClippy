package com.example.will.sfclippy;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by will on 31/07/2016.
 */
public class CharacterStatistics {
    /**
     * Represents a character.
     */
    private static class Character {
        private String name;
        private int p1Weight;
        private int p2Weight;

        public Character( String name, int p1Weight, int p2Weight ) {
            this.name = name;
            this.p1Weight = p1Weight;
            this.p2Weight = p2Weight;
        }
    }

    private Map<String,Character> nameToCharacter;

    public CharacterStatistics( ) {
        nameToCharacter = new HashMap<>();
    }

    public void addCharacter( String name, int p1Weight, int p2Weight ) {
        Character ch = new Character(name, p1Weight, p2Weight);
        nameToCharacter.put( name, ch );
    }

    /**
     * Fetch character preferences for player 1.
     */
    public PojoCharacterPreference[] getP1Preferences( ) {
        PojoCharacterPreference[] preferences = new PojoCharacterPreference[nameToCharacter.size()];
        int idx = 0;
        for ( Map.Entry<String,Character> entry : nameToCharacter.entrySet() ) {
            PojoCharacterPreference pref = new PojoCharacterPreference();
            pref.name = entry.getKey();
            pref.weighting = entry.getValue().p1Weight;

            preferences[idx] = pref;
            idx++;
        }

        return preferences;
    }

    /**
     * Fetch character preferences for player 2.
     */
    public PojoCharacterPreference[] getP2Preferences( ) {
        PojoCharacterPreference[] preferences = new PojoCharacterPreference[nameToCharacter.size()];
        int idx=0;
        for ( Map.Entry<String,Character> entry : nameToCharacter.entrySet() ) {
            PojoCharacterPreference pref = new PojoCharacterPreference();
            pref.name = entry.getKey();
            pref.weighting = entry.getValue().p2Weight;

            preferences[idx] = pref;
            idx++;
        }

        return preferences;
    }
}
