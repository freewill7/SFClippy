package com.example.will.sfclippy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.text.Line;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CharacterPreferenceActivity extends AppCompatActivity {
    public static final String PLAYER_ID_PROPERTY = "player_id";
    private List<DataProvider.CharacterPreference> characterPreferences;
    private Button btnSave;
    private DataProvider dataProvider;
    private String playerId;

    /**
     * Convenience class for updating button icons.
     */
    private static class IconUpdater {
        private Drawable good;
        private Drawable average;
        private Drawable bad;

        public IconUpdater( Drawable good, Drawable average, Drawable bad ) {
            this.good = good;
            this.average = average;
            this.bad = bad;
        }

        public void updateIcon( ImageButton btn, TextView textView,
                                DataProvider.CharacterPreference pref ) {
            textView.setText( pref.getCharacterName() + " (" + pref.getScore() + ")");
            if ( pref.getScore() == 0 ) {
                btn.setImageDrawable(bad);
            } else if ( pref.getScore() == 1 ) {
                btn.setImageDrawable(average);
            } else {
                btn.setImageDrawable(good);
            }
        }
    }

    /**
     * Adapter for preferences data.
     */
    private static class PreferencesAdapter
            extends RecyclerView.Adapter<PreferencesAdapter.ViewHolder> {
        private List<DataProvider.CharacterPreference> preferences;
        private IconUpdater iconUpdater;

        public static class ViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {
            public ImageButton mButton;
            public TextView mTextView;
            public DataProvider.CharacterPreference mPreference;
            private IconUpdater mIconUpdater;

            public ViewHolder( View container, IconUpdater iconUpdater ) {
                super(container);
                mButton = (ImageButton) container.findViewById(R.id.btnPrefModify);
                mButton.setOnClickListener( this );
                mTextView = (TextView) container.findViewById(R.id.textPrefText);
                mIconUpdater = iconUpdater;
            }

            @Override
            public void onClick( View v ) {
                mPreference.cycleScore();
                mIconUpdater.updateIcon( mButton, mTextView, mPreference );
            }
        }

        public PreferencesAdapter(List<DataProvider.CharacterPreference> preferences,
                                  IconUpdater iconUpdater ) {
            this.preferences = preferences;
            this.iconUpdater = iconUpdater;
        }

        @Override
        public PreferencesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
            View layout = LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.layout_character_preference, parent, false );
            ViewHolder vh = new ViewHolder( layout, iconUpdater );
            return vh;
        }

        @Override
        public void onBindViewHolder( ViewHolder viewHolder, int index ) {
            DataProvider.CharacterPreference pref = preferences.get(index);
            viewHolder.mPreference = pref;
            iconUpdater.updateIcon( viewHolder.mButton, viewHolder.mTextView, pref );
        }

        @Override
        public int getItemCount( ) {
            return preferences.size();
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

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dataProvider = AppSingleton.getInstance().getDataProvider();

        Intent intent = getIntent();
        playerId = intent.getStringExtra( PLAYER_ID_PROPERTY );

        final RecyclerView listView = (RecyclerView) findViewById( R.id.characterPrefList );
        listView.setHasFixedSize(true);

        // use linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        DataProvider provider = AppSingleton.getInstance().getDataProvider();

        // take a copy of preferences so we can modify
        List<DataProvider.CharacterPreference> prefs = provider.getCharacterPreferences(playerId);
        characterPreferences = new ArrayList<>();
        for ( DataProvider.CharacterPreference pref : prefs ) {
            characterPreferences.add( new DataProvider.CharacterPreference(pref) );
        }

        // sort the preferences so favourites are at the top
        Collections.sort(characterPreferences, new Comparator<DataProvider.CharacterPreference>() {
            @Override
            public int compare(DataProvider.CharacterPreference lhs, DataProvider.CharacterPreference rhs) {
                int ret = rhs.getScore() - lhs.getScore();
                if ( 0 == ret ) {
                    ret = lhs.getCharacterName().compareTo( rhs.getCharacterName() );
                }
                return ret;
            }
        });

        IconUpdater updater = new IconUpdater( getDrawable(R.drawable.ic_star_golden_24dp),
                getDrawable(R.drawable.ic_star_half_24dp),
                getDrawable(R.drawable.ic_star_empty_24dp1) );

        // specify an adapter
        Log.d( getLocalClassName(), "Creating adapter for " + characterPreferences.size());
        RecyclerView.Adapter mAdapter = new PreferencesAdapter( characterPreferences, updater );
        listView.setAdapter(mAdapter);
    }

    private static class UpdatePreferences extends AsyncTask<Void,String,Void> {
        private DataProvider dataProvider;
        private String playerId;
        private List<DataProvider.CharacterPreference> prefs;
        private Activity caller;

        public UpdatePreferences( DataProvider dataProvider,
                                  String playerId,
                                  List<DataProvider.CharacterPreference> prefs,
                                  Activity caller ) {
            this.dataProvider = dataProvider;
            this.playerId = playerId;
            this.prefs = prefs;
            this.caller = caller;
        }

        @Override
        public Void doInBackground( Void ... params ) {
            // TODO progress and animation
            dataProvider.replaceCharacterPreferences( playerId, prefs );

            return null;
        }

        @Override
        public void onPostExecute( Void result ) {
            caller.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case R.id.action_accept:
                UpdatePreferences update = new UpdatePreferences( dataProvider,
                        playerId,
                        characterPreferences,
                        this );
                update.execute();

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
