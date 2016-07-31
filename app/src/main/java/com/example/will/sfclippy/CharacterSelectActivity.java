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

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class CharacterSelectActivity extends Activity {
    static public final String GET_CHARACTER_PROPERTY = "choice";
    static public final String PLAYER_PREFERENCES = "preferences";

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private PojoCharacterPreference[] mDataset;
        private Activity mActivity;
        private PojoCharacterPreference mRandomChar;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public Activity mActivity;
            public Button mButton;
            public PojoCharacterPreference mCharacter;

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

            private PojoCharacterPreference getCharacter() {
                return mCharacter;
            }
        }

        public MyAdapter( Activity activity,
                          PojoCharacterPreference[] myDataset,
                          PojoCharacterPreference randomChar ) {
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
                holder.mButton.setText("RANDOM (" + mRandomChar.name + ")");
                // TODO change to tint
                holder.mButton.setBackgroundColor(
                        mActivity.getResources().getColor( R.color.colorAccent ) );
                holder.mCharacter = mRandomChar;
            } else {
                PojoCharacterPreference pref = mDataset[position-1];
                holder.mButton.setText(pref.name + "(" + pref.weighting + ")");
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

    private PojoCharacterPreference randomCharacter( List<PojoCharacterPreference> chars ) {
        int total = 0;
        for ( PojoCharacterPreference character : chars ) {
            total += character.weighting;
        }

        Random rand = new Random(Calendar.getInstance().getTimeInMillis());
        int choice = rand.nextInt() % total;

        int tally = 0;
        PojoCharacterPreference ret = chars.get(0);
        for ( PojoCharacterPreference character : chars ) {
            ret = character;
            tally += ret.weighting;
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
        String preferences = intent.getStringExtra( PLAYER_PREFERENCES );
        Gson gson = new Gson();
        PojoCharacterPreference[] chars = gson.fromJson( preferences,
                PojoCharacterPreference[].class );

        LinkedList<PojoCharacterPreference> choices = new LinkedList<>();
        for ( PojoCharacterPreference choice : chars ) {
            Log.d( "CharacterSelectActivity", "Adding " + choice.name);
            choices.add(choice);
        }

        PojoCharacterPreference randomChar = randomCharacter( choices );

        Collections.sort(choices, new Comparator<PojoCharacterPreference>() {
            @Override
            public int compare(PojoCharacterPreference lhs, PojoCharacterPreference rhs) {
                // higher rating goes first
                int diff = rhs.weighting - lhs.weighting;
                if ( 0 == diff ) {
                    // then order by ascending alphabetical
                    diff = lhs.name.compareTo( rhs.name );
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
        PojoCharacterPreference[] arr = choices.toArray( new PojoCharacterPreference[choices.size()]);
        RecyclerView.Adapter mAdapter = new MyAdapter( this, arr, randomChar );
        listView.setAdapter(mAdapter);

    }
}
