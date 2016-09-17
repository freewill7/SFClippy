package com.example.will.sfclippy;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.will.sfclippy.models.CharacterPreference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CharacterPreferenceActivity extends AppCompatActivity {
    public static final String PLAYER_ID_PROPERTY = "player_id";
    public static final String TITLE_PROPERY = "title";
    private DatabaseReference mReference;
    private PreferencesAdapter mAdapter;
    private DataProvider dataProvider;
    private String playerId;
    private static String TAG = "CharacterPreference";

    /**
     * Convenience class for updating visible score.
     */
    private static class IconUpdater {

        public IconUpdater( ) {
            // nothing to do
        }

        public void updateIcon(RatingBar rating, TextView textView,
                               CharacterPreference pref ) {
            textView.setText( pref.name + " (" + pref.score + ")");
            rating.setRating( (float) pref.score );
        }
    }

    /**
     * A view within the recycler view.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public RatingBar ratingBar;
        public TextView mTextView;
        public CharacterPreference mPreference;
        private IconUpdater mIconUpdater;

        public ViewHolder( View container, IconUpdater iconUpdater ) {
            super(container);
            ratingBar = (RatingBar) container.findViewById(R.id.characterRatingBar);
            //mButton.setOnClickListener( this );
            mTextView = (TextView) container.findViewById(R.id.textPrefText);
            mIconUpdater = iconUpdater;
        }

        @Override
        public void onClick( View v ) {
            // mPreference.cycleScore();
            mIconUpdater.updateIcon( ratingBar, mTextView, mPreference );
        }
    }

    /**
     * Adapter for preferences data.
     */
    private static class PreferencesAdapter
            extends RecyclerView.Adapter<ViewHolder>
            implements ValueEventListener {
        private DatabaseReference refPreferences;
        private IconUpdater iconUpdater;
        private ArrayList<CharacterPreference> mDataSet = new ArrayList<>();
        private Comparator<CharacterPreference> orderer = new CharacterPreference.DescendingScore();

        /**
         * Construct a preferences adapter.
         *
         * After construction you should register the object as a ValueEventListener
         * for the provided preferences object. The preferences object is only provided
         * for making updates to preferences.
         * @param preferences A preferences reference for update
         * @param iconUpdater For updating icon based on preference.
         */
        public PreferencesAdapter(DatabaseReference preferences,
                                  IconUpdater iconUpdater ) {
            this.refPreferences = preferences;
            this.iconUpdater = iconUpdater;
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            if ( null == snapshot.getValue() ) {
                // bootstrap preferences
                FirebaseHelper.initialisePreferences(refPreferences);
            } else {
                ArrayList<CharacterPreference> preferences = new ArrayList<>();
                for ( DataSnapshot child : snapshot.getChildren() ) {
                    preferences.add( child.getValue(CharacterPreference.class) );
                }

                // sort appropriately
                Collections.sort( preferences, orderer );

                // update and notify
                this.mDataSet = preferences;
                notifyDataSetChanged();
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            Log.e( TAG, "onCancelled", error.toException());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
            View layout = LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.layout_character_preference, parent, false );
            ViewHolder vh = new ViewHolder( layout, iconUpdater );
            return vh;
        }

        @Override
        public void onBindViewHolder( ViewHolder viewHolder, int index ) {
            CharacterPreference pref = mDataSet.get(index);
            viewHolder.mPreference = pref;
            iconUpdater.updateIcon( viewHolder.ratingBar, viewHolder.mTextView, pref );
        }

        @Override
        public int getItemCount( ) {
            return mDataSet.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_character_preference, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_preference);

        dataProvider = AppSingleton.getInstance().getDataProvider();

        Intent intent = getIntent();
        playerId = intent.getStringExtra( PLAYER_ID_PROPERTY );
        String title = intent.getStringExtra( TITLE_PROPERY );

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle( title );

        IconUpdater updater = new IconUpdater( );

        mReference = dataProvider.getPreferences( playerId );
        mAdapter = new PreferencesAdapter( mReference, updater );
        mReference.addValueEventListener( mAdapter );

        final RecyclerView listView = (RecyclerView) findViewById( R.id.characterPrefList );
        listView.setHasFixedSize(true);

        // use linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy( ) {
        mReference.removeEventListener(mAdapter);
        super.onDestroy();
    }
}
