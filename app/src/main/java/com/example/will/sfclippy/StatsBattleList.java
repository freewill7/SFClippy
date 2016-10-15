package com.example.will.sfclippy;


import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.will.sfclippy.models.BattleResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatsBattleList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatsBattleList extends DialogFragment {
    private static final String ARG_ACCOUNT_ID = "accountId";
    private static final String ARG_PLAYER_ID = "playerId";
    private static final String ARG_CHARACTER_NAME = "characterName";

    private String mAccountId;
    private String mPlayerId;
    private String mCharacterName;

    private DatabaseReference mResultsDir;
    private CharResultsAdapter mCharResultsAdapter;


    public StatsBattleList() {
        // Required empty public constructor
    }

    /**
     * One of the views within the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDate;
        public TextView mOpponentChar;
        public TextView mResult;

        public ViewHolder( View view ) {
            super(view);
            mDate = (TextView) view.findViewById(R.id.lblDate);
            mOpponentChar = (TextView) view.findViewById(R.id.lblCharacter);
            mResult = (TextView) view.findViewById(R.id.lblWin);
        }
    }

    /**
     * Adapter between a FirebaseDb and RecyclerView.
     */
    public static class CharResultsAdapter extends RecyclerView.Adapter<ViewHolder>
            implements ValueEventListener {
        private List<BattleResult> mDataset = new ArrayList<>();
        private static final String TAG = "CharResultsAdapter";
        private String mPlayerId;
        private String mPlayerCharacter;
        private static SimpleDateFormat format = new SimpleDateFormat( "dd-MMM");

        public CharResultsAdapter( String playerId, String playerCharacter ) {
            mPlayerId = playerId;
            mPlayerCharacter = playerCharacter;
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            if ( null != snapshot.getValue()) {
                ArrayList<BattleResult> results = new ArrayList<>();
                for ( DataSnapshot snap : snapshot.getChildren() ) {
                    BattleResult result = snap.getValue(BattleResult.class);
                    String battleChar = result.characterFor(mPlayerId);
                    if ( null != battleChar && battleChar.equals(mPlayerCharacter)) {
                        results.add(result);
                    }
                }

                // Sort appropriately
                Log.d( TAG, "Collection size is " + results.size());
                Collections.sort(results, new Comparator<BattleResult>() {
                    @Override
                    public int compare(BattleResult lhs, BattleResult rhs) {
                        return rhs.date.compareTo(lhs.date);
                    }
                });

                // Update and notify
                this.mDataset = results;
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
                    .inflate( R.layout.layout_character_result, parent, false );
            ViewHolder vh = new ViewHolder( view );
            return vh;
        }

        @Override
        public void onBindViewHolder( ViewHolder holder, int position ) {
            BattleResult result = mDataset.get(position);
            holder.mDate.setText( format.format( result.dateAsDate() ) );
            holder.mOpponentChar.setText( result.opponentFor(mPlayerId) );
            if ( result.winnerId.equals(mPlayerId) ) {
                holder.mResult.setText( "WIN" );
                holder.mResult.setTextColor(Color.BLUE);
            } else {
                holder.mResult.setText( "LOSE" );
                holder.mResult.setTextColor(Color.BLACK);
            }
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param accountId The account id.
     * @param playerId The player id.
     * @param characterName The character chosen by the player.
     * @return A new instance of fragment StatsBattleList.
     */
    // TODO: Rename and change types and number of parameters
    public static StatsBattleList newInstance(String accountId,
                                              String playerId,
                                              String characterName) {
        StatsBattleList fragment = new StatsBattleList();
        Bundle args = new Bundle();
        args.putString(ARG_ACCOUNT_ID, accountId);
        args.putString(ARG_PLAYER_ID, playerId);
        args.putString(ARG_CHARACTER_NAME, characterName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccountId = getArguments().getString(ARG_ACCOUNT_ID);
            mPlayerId = getArguments().getString(ARG_PLAYER_ID);
            mCharacterName = getArguments().getString(ARG_CHARACTER_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_stats_battle_list, container, false);

        TextView title = (TextView) v.findViewById(R.id.lblCharacterName);
        title.setText( mCharacterName + " results");

        DatabaseHelper helper = new DatabaseHelper( FirebaseDatabase.getInstance(), mAccountId );
        mResultsDir = helper.getResultsDirReference();

        mCharResultsAdapter = new CharResultsAdapter( mPlayerId, mCharacterName );
        mResultsDir.addValueEventListener( mCharResultsAdapter );

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.listCharacterResults);
        recyclerView.setLayoutManager( new LinearLayoutManager( v.getContext() ));
        recyclerView.setAdapter(mCharResultsAdapter);

        return v;
    }

    @Override
    public void onDestroyView( ) {
        mResultsDir.removeEventListener(mCharResultsAdapter);

        super.onDestroyView();
    }

}
