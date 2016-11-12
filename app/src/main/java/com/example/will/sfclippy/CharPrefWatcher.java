package com.example.will.sfclippy;

import android.util.Log;

import com.example.will.sfclippy.models.CharacterPreference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Watches for changes to character preferences.
 */

public class CharPrefWatcher implements ValueEventListener {
    private static final String TAG = "CharPrefWatcher";
    private List<CharacterPreference> preferences;
    private final RandomSelector selector;
    private static final String UNKNOWN = "unknown";

    public CharPrefWatcher( ) {
        preferences = new ArrayList<>();
        selector = new RandomSelector(preferences);
    }

    public String getRandomCharacter( ) {
        int index = selector.randomCharacter();
        String ret = UNKNOWN;
        if ( preferences.size() > index ) {
            ret = preferences.get(index).name;
        }
        return ret;
    }

    public String getDiscoverCharacter( ) {
        int index = selector.discoverCharacter();
        String ret = UNKNOWN;
        if ( preferences.size() > index ) {
            ret = preferences.get(index).name;
        }
        return ret;
    }

    public String matchCharacter( String input ) {
        String ret = UNKNOWN;
        Log.d( TAG, "Matching " + input );
        for ( CharacterPreference pref : preferences ) {
            if ( pref.name.equalsIgnoreCase(input) ) {
                Log.d( TAG, "Matched " + input + " to " + pref.name );
                ret = pref.name;
                break;
            }
        }
        return ret;
    }

    @Override
    public void onDataChange(DataSnapshot snapshot) {
        List<CharacterPreference> prefs = new ArrayList<>();
        for ( DataSnapshot snap : snapshot.getChildren() ) {
            CharacterPreference pref = snap.getValue( CharacterPreference.class );
            prefs.add(pref);
        }

        preferences = prefs;
        selector.setCharacters(prefs);
    }

    @Override
    public void onCancelled( DatabaseError err ) {
        Log.e( TAG, "Problem with watcher", err.toException() );
    }
}
