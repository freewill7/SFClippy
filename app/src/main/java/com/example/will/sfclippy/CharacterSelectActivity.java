package com.example.will.sfclippy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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

public class CharacterSelectActivity extends Activity {
    static public final String GET_CHARACTER_PROPERTY = "choice";
    static public final String PLAYER_ID = "player_id";

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private DataProvider.CharacterPreference[] mDataset;
        private Activity mActivity;
        private DataProvider.CharacterPreference mRandomChar;

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
                          DataProvider.CharacterPreference[] myDataset,
                          DataProvider.CharacterPreference randomChar ) {
            mActivity = activity;
            mDataset = myDataset;
            mRandomChar = randomChar;
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

            if ( 0 == position ) {
                holder.mButton.setText("RANDOM (" + mRandomChar.getCharacterName() + ")");
                // TODO change to tint
                holder.mButton.setBackgroundColor(
                        mActivity.getResources().getColor( R.color.colorAccent ) );
                holder.mCharacter = mRandomChar;
            } else {
                DataProvider.CharacterPreference pref = mDataset[position-1];
                holder.mButton.setText(pref.getCharacterName() + "(" + pref.getScore() + ")");
                // TODO change to tint
                holder.mButton.setBackgroundColor(
                        mActivity.getResources().getColor( R.color.colorPrimary ) );
                holder.mCharacter = pref;
            }
        }

        @Override
        public int getItemCount() {
            return mDataset.length + 1;
        }
    }

    private DataProvider.CharacterPreference randomCharacter(
            List<DataProvider.CharacterPreference> chars ) {
        int total = 0;
        for ( DataProvider.CharacterPreference character : chars ) {
            total += character.getScore();
        }

        Random rand = new Random(Calendar.getInstance().getTimeInMillis());
        int choice = rand.nextInt() % total;

        int tally = 0;
        DataProvider.CharacterPreference ret = chars.get(0);
        for ( DataProvider.CharacterPreference character : chars ) {
            ret = character;
            tally += ret.getScore();
            if ( tally > choice ) {
                break;
            }
        }

        return ret;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_select);

        Intent intent = getIntent();
        String playerId = intent.getStringExtra( PLAYER_ID );

        List<DataProvider.CharacterPreference> preferences =
                AppSingleton.getInstance().getDataProvider().getCharacterPreferences( playerId );

        List<DataProvider.CharacterPreference> choices = new ArrayList<>( preferences );
        DataProvider.CharacterPreference randomChar = randomCharacter( choices );

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

        final RecyclerView listView = (RecyclerView) findViewById( R.id.characterList );
        listView.setHasFixedSize(true);

        // use linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        // specify an adapter
        DataProvider.CharacterPreference[] arr = choices.toArray(
                new DataProvider.CharacterPreference[choices.size()]);
        RecyclerView.Adapter mAdapter = new MyAdapter( this, arr, randomChar );
        listView.setAdapter(mAdapter);

    }
}
