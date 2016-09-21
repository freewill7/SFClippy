package com.example.will.sfclippy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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
import android.widget.LinearLayout;
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
import java.util.List;

public class CharacterSelectActivity extends AppCompatActivity
implements CharacterRatingFragment.RatingInteractionListener {
    static public final String GET_CHARACTER_PROPERTY = "choice";
    static public final String PLAYER_ID = "player_id";
    static public final String TITLE = "title";
    private RecyclerView listView;
    private MySelectAdapter mAdapter;
    private DatabaseReference mReference;

    /**
     * One of the views within the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Activity mActivity;
        public View mView;
        public RatingBar mRatingBar;
        public TextView mTextView;
        public CharacterPreference mCharacter;

        public ViewHolder( Activity activity, View view ) {
            super(view);
            mActivity = activity;
            mView = view;
            mRatingBar = (RatingBar) mView.findViewById(R.id.characterRatingBar);
            mTextView = (TextView) mView.findViewById(R.id.characterLabel);

            // click to select
            mView.setClickable(true);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CharacterRatingFragment frag = CharacterRatingFragment.newInstance(
                            mCharacter.name, mCharacter.score, mCharacter.battles_fought, mCharacter.battles_won );
                    frag.show( mActivity.getFragmentManager(), "frame name" );
                }
            });
        }

        private CharacterPreference getCharacter() {
            return mCharacter;
        }
    }

    /**
     * Adapter between a FirebaseDb and RecyclerView.
     */
    public static class MySelectAdapter extends RecyclerView.Adapter<ViewHolder>
    implements ValueEventListener {
        private List<CharacterPreference> mDataset = new ArrayList<>();
        private DatabaseReference mPreferences;
        private Activity mActivity;
        private int defaultItemId;
        private static final String TAG = "MySelectAdapter";
        private Comparator<CharacterPreference> orderer;
        private RandomSelector selector = new RandomSelector();

        public MySelectAdapter( Activity activity,
                                DatabaseReference preferences ) {
            mActivity = activity;
            mPreferences = preferences;
            defaultItemId = -1;

            orderer = new CharacterPreference.DescendingScore();
        }

        void upgradeIfRequired( ArrayList<CharacterPreference> preferences ) {
            boolean hasUrien = false;

            for ( CharacterPreference pref : preferences ) {
                if ( pref.name.equals("Urien") ) {
                    hasUrien = true;
                }
            }

            if ( ! hasUrien ) {
                FirebaseHelper.storePreference(mPreferences, "Urien", 2, 0, 0 );
            }
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            if ( null == snapshot.getValue()) {
                // bootstrap preferences
                FirebaseHelper.initialisePreferences(mPreferences);
            } else {
                ArrayList<CharacterPreference> preferences = new ArrayList<>();
                for ( DataSnapshot snap : snapshot.getChildren() ) {
                    preferences.add( snap.getValue(CharacterPreference.class) );
                }

                // perform upgrades (e.g. new character)
                upgradeIfRequired( preferences );

                // Sort appropriately
                Collections.sort( preferences, orderer );

                // Update and notify
                this.mDataset = preferences;
                selector.setCharacters(preferences);
                notifyDataSetChanged();
            }
        }

        @Override
        public void onCancelled( DatabaseError dbError ) {
            Log.e( TAG, "Cancelled adapter", dbError.toException() );
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
            View view = (View) LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.layout_character_choice, parent, false );
            ViewHolder vh = new ViewHolder( mActivity, view );
            return vh;
        }

        @Override
        public void onBindViewHolder( ViewHolder holder, int position ) {
            CharacterPreference pref = mDataset.get(position);
            int chance = selector.percentageChance(pref);
            holder.mTextView.setText(pref.name + " (" + chance + "%)");
            holder.mRatingBar.setRating( (int) pref.score );
            holder.mCharacter = pref;
            if ( defaultItemId == position ) {
                holder.mTextView.setBackgroundColor( mActivity.getColor(R.color.colorAccent ) );
            } else {
                holder.mTextView.setBackgroundColor( Color.TRANSPARENT );
            }
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        /**
         * Select a random character.
         */
        public int selectRandom( ) {
            RandomSelector selector = new RandomSelector( mDataset );
            defaultItemId = selector.randomCharacter();
            notifyDataSetChanged();
            return defaultItemId;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_character_select, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_select);

        Intent intent = getIntent();
        String playerId = intent.getStringExtra( PLAYER_ID );
        String title = intent.getStringExtra( TITLE );

        DataProvider dataProvider = AppSingleton.getInstance().getDataProvider();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle( title );

        mReference = dataProvider.getPreferences( playerId );
        mAdapter = new MySelectAdapter( this, mReference );
        mReference.addValueEventListener( mAdapter );

        this.listView = (RecyclerView) findViewById( R.id.characterList );
        this.listView.setHasFixedSize(true);

        // use linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        this.listView.setLayoutManager(layoutManager);
        this.listView.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case R.id.action_lucky:

                int idx = mAdapter.selectRandom();
                this.listView.scrollToPosition(idx);

                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onDestroy(  ) {
        mReference.removeEventListener(mAdapter);
        super.onDestroy();
    }

    @Override
    public void onRatingChange( String character, int score ) {
        // just update the score
        FirebaseHelper.updateCharacterPreference( mReference, character, score );
    }

    @Override
    public void onCharacterSelected( String character ) {
        Intent resultData = new Intent();
        resultData.putExtra(GET_CHARACTER_PROPERTY, character );
        this.setResult(Activity.RESULT_OK, resultData);
        this.finish();
    }
}
