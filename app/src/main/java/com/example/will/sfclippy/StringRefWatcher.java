package com.example.will.sfclippy;

import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 24/09/2016.
 */

public class StringRefWatcher implements ValueEventListener {
    private String value;
    private static final String TAG = "StringRefWatcher";
    private List<TextViewFormatter> textViewFormatters = new ArrayList<>();

    private class TextViewFormatter {
        public String format;
        public TextView view;

        public TextViewFormatter( TextView view, String format ) {
            this.format = format;
            this.view = view;
        }
    }

    @Override
    public void onDataChange(DataSnapshot snapshot) {
        Log.d( "PlayerWatcher", "Received data change");
        if ( null == snapshot ) {
            Log.d(TAG, "Received player watcher null");
        } else {
            value = snapshot.getValue(String.class);
            Log.d(TAG, "Received text value " + value);

            for ( TextViewFormatter formatter : textViewFormatters ) {
                formatter.view.setText(
                        String.format( formatter.format, value )
                );
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError err) {
        Log.e( TAG, "Text watcher received cancel", err.toException());
    }

    public void registerTextView( TextView view, String format ) {
        textViewFormatters.add( new TextViewFormatter( view, format ) );
    }

    public String getValue( ) {
        return value;
    }
}
