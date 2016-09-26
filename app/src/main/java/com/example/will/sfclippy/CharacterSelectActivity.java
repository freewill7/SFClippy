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
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.will.sfclippy.models.CharacterPreference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CharacterSelectActivity extends AppCompatActivity
implements CharacterRatingFragment.RatingInteractionListener {
    static public final String GET_CHARACTER_PROPERTY = "choice";
    static public final String ACCOUNT_ID = "account_id";
    static public final String PLAYER_ID = "player_id";
    static public final String TITLE = "title";
    private String playerId;
    private DatabaseHelper helper;
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

        public ViewHolder( Activity activity,
                           View view,
                           final String accountId,
                           final String playerId ) {
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
                            accountId,
                            playerId,
                            mCharacter.name,
                            mCharacter.score );
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
        private Activity mActivity;
        private String mAccountId;
        private String mPlayerId;
        private DatabaseHelper mHelper;
        private int defaultItemId;
        private static final String TAG = "MySelectAdapter";
        private Comparator<CharacterPreference> orderer;
        private RandomSelector selector = new RandomSelector();

        public MySelectAdapter( Activity activity,
                                String accountId,
                                String playerId,
                                DatabaseHelper helper ) {
            mActivity = activity;
            mAccountId = accountId;
            mPlayerId = playerId;
            mHelper = helper;
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
                mHelper.addCharacterPref( mPlayerId, "Urien" );
            }
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            if ( null == snapshot.getValue()) {
                // bootstrap preferences
                mHelper.bootstrapCharacterPrefs( mPlayerId );
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
            ViewHolder vh = new ViewHolder( mActivity, view, mAccountId, mPlayerId );
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
            //defaultItemId = selector.randomCharacter();
            //notifyDataSetChanged();

            int itemId = selector.randomCharacter();
            CharacterPreference character = mDataset.get(itemId);

            // select item as a fragment
            CharacterRatingFragment frag = CharacterRatingFragment.newInstance(
                    mAccountId,
                    mPlayerId,
                    character.name,
                    character.score );
            frag.show( mActivity.getFragmentManager(), "frame name" );

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
        String accountId = intent.getStringExtra( ACCOUNT_ID );
        playerId = intent.getStringExtra( PLAYER_ID );
        String title = intent.getStringExtra( TITLE );

        helper = new DatabaseHelper( FirebaseDatabase.getInstance(), accountId );

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle( title );

        mReference = helper.getPlayerPrefsRef( playerId );
        mAdapter = new MySelectAdapter( this, accountId, playerId, helper );
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
        helper.updateCharacterScore( playerId, character, score );
    }

    @Override
    public void onCharacterSelected( String character ) {
        Intent resultData = new Intent();
        resultData.putExtra(GET_CHARACTER_PROPERTY, character );
        this.setResult(Activity.RESULT_OK, resultData);
        this.finish();
    }
}
