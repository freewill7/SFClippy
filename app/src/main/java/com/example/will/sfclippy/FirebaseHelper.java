package com.example.will.sfclippy;

import android.util.Log;

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

    public static void storePreference( DatabaseReference preferences,
                                        String name, int score ) {
        CharacterPreference pref = new CharacterPreference( name, score );
        String key = name.toLowerCase().replaceAll( "[^\\p{Lower}]", "");
        preferences.child(key).setValue(pref);
    }

    public static void initialisePreferences( DatabaseReference reference ) {
        int defaultInt = 1;
        String[] characters = new String[] {
                "Ryu", "Chun-Li", "Nash", "M.Bison",
                "Cammy", "Birdie", "Ken", "Necalli",
                "Vega", "R.Mika", "Rashid", "Karin",
                "Zangief", "Laura", "Dhalsim", "F.A.N.G.",
                "Alex", "Guile", "Ibuki", "Balrog", "Juri" };
        for ( String character : characters ) {
            storePreference( reference, character, defaultInt );
        }
    }
}
