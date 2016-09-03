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

import com.example.will.sfclippy.models.CharacterPreference;
import com.google.api.client.util.Data;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class CharacterSelectActivity extends AppCompatActivity {
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
        public Button mButton;
        public CharacterPreference mCharacter;

        public ViewHolder( Activity activity, Button button ) {
            super(button);
            mActivity = activity;
            mButton = button;

            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent resultData = new Intent();
                    resultData.putExtra(GET_CHARACTER_PROPERTY, getCharacter().name );
                    mActivity.setResult(Activity.RESULT_OK, resultData);
                    mActivity.finish();
                }
            });
        }

        private CharacterPreference getCharacter() {
            return mCharacter;
        }
    }

    /**
     * Order characters by score (highest first).
     */
    public static class DescendingScore implements Comparator<CharacterPreference> {
        public int compare(CharacterPreference lhs, CharacterPreference rhs) {
            // higher rating goes first
            int diff = rhs.score - lhs.score;
            if ( 0 == diff ) {
                // then order by ascending alphabetical
                diff = lhs.name.compareTo( rhs.name );
            }

            return diff;
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

        public MySelectAdapter( Activity activity,
                                DatabaseReference preferences ) {
            mActivity = activity;
            mPreferences = preferences;
            defaultItemId = -1;

            orderer = new DescendingScore();
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

                // Sort appropriately
                Collections.sort( preferences, orderer );

                // Update and notify
                this.mDataset = preferences;
                notifyDataSetChanged();
            }
        }

        @Override
        public void onCancelled( DatabaseError dbError ) {
            Log.e( TAG, "Cancelled adapter", dbError.toException() );
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
            Button btn = (Button) LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.button_character, parent, false );
            ViewHolder vh = new ViewHolder( mActivity, btn );
            return vh;
        }

        @Override
        public void onBindViewHolder( ViewHolder holder, int position ) {
            CharacterPreference pref = mDataset.get(position);
            holder.mButton.setText(pref.name + "(" + pref.score + ")");
            holder.mCharacter = pref;
            if ( defaultItemId == position ) {
                holder.mButton.setBackgroundColor( mActivity.getColor(R.color.colorAccent ) );
            } else {
                holder.mButton.setBackgroundColor( Color.TRANSPARENT );
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
}
