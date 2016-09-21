package com.example.will.sfclippy;

import android.util.Log;

import com.example.will.sfclippy.models.BattleResult;
import com.example.will.sfclippy.models.CharacterPreference;
import com.example.will.sfclippy.models.PlayerInfo;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by will on 29/08/2016.
 */

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    public static final String TAG_USERNAME = "username";

    public static class FirebaseUser {
        private DatabaseReference reference;

        public FirebaseUser( DatabaseReference reference ) {
            this.reference = reference;
        }

        public DatabaseReference getUsername( ) {
            return reference.child(TAG_USERNAME);
        }

        public DatabaseReference getReference( ) {
            return reference;
        }
    }

    public static FirebaseUser getUser( DatabaseReference users, String userId ) {
        return new FirebaseUser( users.child( userId ) );
    }

    public static void initialiseUser(DatabaseReference reference, String username ) {
        Log.d( TAG, "Creating user " + username + " (" + reference.getKey() + ")");
        PlayerInfo info = new PlayerInfo( reference.getKey(), username );
        reference.setValue( info );
    }

    public static DatabaseReference getPreferences( DatabaseReference reference,
                                                    String username ) {
        return reference.child( username );
    }

    public static DatabaseReference getCharacterPreference( DatabaseReference preferences,
                                                            String character ) {
        String key = characterKey(character);
        return preferences.child(key);
    }

    private static String characterKey( String name ) {
        String key = name.toLowerCase().replaceAll( "[^\\p{Lower}]", "");
        return key;
    }

    public static void storePreference( DatabaseReference preferences,
                                        String name, int score,
                                        int battles, int wins ) {
        CharacterPreference pref = new CharacterPreference( name, score, battles, wins );
        DatabaseReference character = getCharacterPreference(preferences, name);
        character.setValue(pref);
    }

    public static void updateCharacterPreference( DatabaseReference preferences,
                                                  String name,
                                                  int score ) {
        DatabaseReference character = getCharacterPreference(preferences, name);
        DatabaseReference scoreRef = character.child("score");
        scoreRef.setValue(score);
    }

    public static void initialisePreferences( DatabaseReference reference ) {
        int defaultInt = 2;
        String[] characters = new String[] {
                "Ryu", "Chun-Li", "Nash", "M.Bison",
                "Cammy", "Birdie", "Ken", "Necalli",
                "Vega", "R.Mika", "Rashid", "Karin",
                "Zangief", "Laura", "Dhalsim", "F.A.N.G.",
                "Alex", "Guile", "Ibuki", "Balrog", "Juri" };
        for ( String character : characters ) {
            storePreference( reference, character, defaultInt, 0, 0 );
        }
    }

    public static void storeCharBattles(DatabaseReference reference,
                                       HistoricalTrends.BattleCounter counter ) {
        reference.child( "battles_fought" ).setValue( counter.getTotalBattles() );
        reference.child( "battles_won" ).setValue( counter.getWonBattles() );
    }
}
