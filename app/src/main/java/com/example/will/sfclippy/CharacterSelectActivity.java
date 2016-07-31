package com.example.will.sfclippy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class CharacterSelectActivity extends Activity {
    static public final String GET_CHARACTER_PROPERTY = "choice";
    static public final String PLAYER_PREFERENCES = "preferences";

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private PojoCharacterPreference[] mDataset;
        private Activity mActivity;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public Activity mActivity;
            public Button mButton;
            public String mCharacter;
            public ViewHolder( Activity activity, Button button ) {
                super(button);
                mActivity = activity;
                mButton = button;

                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent resultData = new Intent();
                        resultData.putExtra(GET_CHARACTER_PROPERTY, getCharacter());
                        mActivity.setResult(Activity.RESULT_OK, resultData);
                        mActivity.finish();
                    }
                });
            }

            private String getCharacter() {
                return mCharacter;
            }
        }

        public MyAdapter( Activity activity, PojoCharacterPreference[] myDataset ) {
            mActivity = activity;
            mDataset = myDataset;
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
            PojoCharacterPreference pref = mDataset[position];
            holder.mButton.setText( pref.name + "(" + pref.weighting + ")" );
            holder.mCharacter = pref.name;
        }

        @Override
        public int getItemCount() {
            return mDataset.length;
        }
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
        RecyclerView.Adapter mAdapter = new MyAdapter( this, arr );
        listView.setAdapter(mAdapter);

    }
}
