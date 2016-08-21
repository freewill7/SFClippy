package com.example.will.sfclippy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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

import com.google.api.client.util.Data;
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
    private RandomSelector selector;
    private RecyclerView listView;
    private MyAdapter mAdapter;

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private DataProvider.CharacterPreference[] mDataset;
        private Activity mActivity;
        private int defaultItemId;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public Activity mActivity;
            public Button mButton;
            public DataProvider.CharacterPreference mCharacter;

            public ViewHolder( Activity activity, Button button ) {
                super(button);
                mActivity = activity;
                mButton = button;

                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent resultData = new Intent();
                        resultData.putExtra(GET_CHARACTER_PROPERTY, getCharacter().getCharacterName() );
                        mActivity.setResult(Activity.RESULT_OK, resultData);
                        mActivity.finish();
                    }
                });
            }

            private DataProvider.CharacterPreference getCharacter() {
                return mCharacter;
            }
        }

        public MyAdapter( Activity activity,
                          DataProvider.CharacterPreference[] myDataset ) {
            mActivity = activity;
            mDataset = myDataset;
            defaultItemId = -1;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
            Button btn = (Button) LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.button_character, parent, false );
            ViewHolder vh = new ViewHolder( mActivity, btn );
            return vh;
        }

        @Override
        public void onBindViewHolder( ViewHolder holder, int position ) {
            DataProvider.CharacterPreference pref = mDataset[position];
            holder.mButton.setText(pref.getCharacterName() + "(" + pref.getScore() + ")");
            holder.mCharacter = pref;
            if ( defaultItemId == position ) {
                holder.mButton.setBackgroundColor(Color.RED);
            } else {
                holder.mButton.setBackgroundColor( Color.TRANSPARENT );
            }
        }

        @Override
        public int getItemCount() {
            return mDataset.length;
        }

        /**
         * Set the default id item id.
         */
        public void setDefaultItemId( int idx ) {
            defaultItemId = idx;
            notifyDataSetChanged();
        }
    }

    private static class RandomSelector {
        List<DataProvider.CharacterPreference> chars;
        private Random randomGenerator;

        public RandomSelector( List<DataProvider.CharacterPreference> chars ) {
            this.chars = chars;
            this.randomGenerator = new Random( Calendar.getInstance().getTimeInMillis() );
        }

        public int randomCharacter( ) {
            int total = 0;
            for ( DataProvider.CharacterPreference character : chars ) {
                total += character.getScore();
            }
            Log.d( this.getClass().getName(), "Total is " + total);

            // hack to get round lack of randomness
            int choice = randomGenerator.nextInt(total);

            Log.d( this.getClass().getName(), "Score to match " + choice);
            Log.d( this.getClass().getName(), "Iterating through " + chars.size() );

            int tally = 0;
            int idx = -1;
            for ( DataProvider.CharacterPreference character : chars ) {
                idx++;

                tally += character.getScore();
                if ( tally > choice ) {
                    Log.d( getClass().getName(),
                            "Moving to " + idx + " (" + character.getCharacterName() + ")");
                    break;
                }
            }

            return idx;
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

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String playerId = intent.getStringExtra( PLAYER_ID );

        List<DataProvider.CharacterPreference> preferences =
                AppSingleton.getInstance().getDataProvider().getCharacterPreferences( playerId );

        List<DataProvider.CharacterPreference> choices = new ArrayList<>( preferences );

        Collections.sort(choices, new Comparator<DataProvider.CharacterPreference>() {
            @Override
            public int compare(DataProvider.CharacterPreference lhs,
                               DataProvider.CharacterPreference rhs) {
                // higher rating goes first
                int diff = rhs.getScore() - lhs.getScore();
                if ( 0 == diff ) {
                    // then order by ascending alphabetical
                    diff = lhs.getCharacterName().compareTo( rhs.getCharacterName() );
                }

                return diff;
            }
        });

        selector = new RandomSelector( choices );

        this.listView = (RecyclerView) findViewById( R.id.characterList );
        this.listView.setHasFixedSize(true);

        // use linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        this.listView.setLayoutManager(layoutManager);

        // specify an adapter
        DataProvider.CharacterPreference[] arr = choices.toArray(
                new DataProvider.CharacterPreference[choices.size()]);
        mAdapter = new MyAdapter( this, arr );
        this.listView.setAdapter(mAdapter);

    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case R.id.action_lucky:

                int idx = selector.randomCharacter();
                mAdapter.setDefaultItemId(idx);
                this.listView.scrollToPosition(idx);

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
