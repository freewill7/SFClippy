package com.example.will.sfclippy;

import android.util.Log;

import com.example.will.sfclippy.models.CharacterPreference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 08/10/2016.
 */

public class CharPrefWatcher implements ValueEventListener {
    private static final String TAG = "CharPrefWatcher";
    private List<CharacterPreference> preferences;
    private final RandomSelector selector;

    public CharPrefWatcher( ) {
        preferences = new ArrayList<>();
        selector = new RandomSelector(preferences);
    }

    public String getRandomCharacter( ) {
        int index = selector.randomCharacter();
        String ret = "unknown";
        if ( preferences.size() > index ) {
            ret = preferences.get(index).name;
        }
        return ret;
    }

    /* public String findCharacter( String input ) {

    } */

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
