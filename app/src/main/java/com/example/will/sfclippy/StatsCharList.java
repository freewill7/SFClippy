package com.example.will.sfclippy;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link StatsCharList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatsCharList extends Fragment {
    private static final String ARG_ACCOUNT_ID = "accountId";
    private static final String ARG_PLAYER_ID = "playerId";
    private static final String ARG_STATS_TYPE = "statsType";
    private static final String TAG = "StatsCharList";

    public static final int ORDER_BY_WINS = 1;
    public static final int ORDER_BY_LOSSES = 2;
    public static final int ORDER_BY_DIFFERENCE = 3;
    public static final int ORDER_BY_RUN = 4;
    public static final int ORDER_BY_PERCENT = 5;
    public static final int ORDER_BY_PLAYED = 6;
    public static final int ORDER_BY_PREDICTED = 7;

    // TODO: Rename and change types of parameters
    private String mAccountId;
    private String mPlayerId;
    private DatabaseReference mPreferencesDir;
    private int mStatsType;
    private MyStatsAdapter mAdapter;
    private ViewCharacterResults mController;

    public StatsCharList() {
        // Required empty public constructor
    }

    public interface ViewCharacterResults {
        void viewCharacterResults( String characterName );
    }

    /**
     * One of the views within the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewCharacterResults mParent;
        public String mCharacterName;
        public final TextView mCharName;
        public final TextView mCharStat;

        public ViewHolder(ViewCharacterResults parent, View view ) {
            super(view);
            mParent = parent;
            mCharName = (TextView) view.findViewById(R.id.lblCharacterName);
            mCharStat = (TextView) view.findViewById(R.id.lblCharacterStat);

            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mParent.viewCharacterResults(mCharacterName);
                }
            });
        }
    }

    public interface CharFormatter {
        void bindValue( ViewHolder view, CharacterPreference value );
    }

    class SharedStatistics {
        public int maxCharBattles = 0;
    }

    /**
     * Adapter between a FirebaseDb and RecyclerView.
     */
    public static class MyStatsAdapter extends RecyclerView.Adapter<ViewHolder>
            implements ValueEventListener {
        private List<CharacterPreference> mDataset = new ArrayList<>();
        private static final String TAG = "MySelectAdapter";
        private final Comparator<CharacterPreference> mOrderer;
        private final CharFormatter mFormatter;
        private final ViewCharacterResults mController;
        private final SharedStatistics mStatistics;

        public MyStatsAdapter( ViewCharacterResults controller,
                               Comparator<CharacterPreference> orderer,
                               CharFormatter formatter,
                               SharedStatistics statistics) {
            mController = controller;
            mOrderer = orderer;
            mFormatter = formatter;
            mStatistics = statistics;
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            if ( null != snapshot.getValue()) {
                int maxBattles = 0;

                ArrayList<CharacterPreference> preferences = new ArrayList<>();
                for ( DataSnapshot snap : snapshot.getChildren() ) {
                    CharacterPreference pref = snap.getValue(CharacterPreference.class);
                    if ( null != pref && null != pref.statistics ) {
                        // update max statistics
                        if ( pref.statistics.battles > maxBattles ) {
                            maxBattles = pref.statistics.battles;
                        }
                        preferences.add( pref );
                    }
                }

                // update shared statistics
                mStatistics.maxCharBattles = maxBattles;

                // Sort appropriately
                Log.d( TAG, "Collection size is " + preferences.size());
                Collections.sort( preferences, mOrderer );

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
            View view = LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.layout_char_stat, parent, false );
            return new ViewHolder( mController, view );
        }

        @Override
        public void onBindViewHolder( ViewHolder holder, int position ) {
            CharacterPreference pref = mDataset.get(position);
            holder.mCharacterName = pref.name;
            mFormatter.bindValue( holder, pref );
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
     * @param accountId The firebase account being used.
     * @param playerId The identity of the player.
     * @param orderType The type of stats presentation.
     * @return A new instance of fragment StatsCharList.
     */
    public static StatsCharList newInstance(String accountId, String playerId, int orderType ) {
        StatsCharList fragment = new StatsCharList();
        Bundle args = new Bundle();
        args.putString(ARG_ACCOUNT_ID, accountId);
        args.putString(ARG_PLAYER_ID, playerId);
        args.putInt(ARG_STATS_TYPE, orderType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccountId = getArguments().getString(ARG_ACCOUNT_ID);
            mPlayerId = getArguments().getString(ARG_PLAYER_ID);
            mStatsType = getArguments().getInt(ARG_STATS_TYPE);
        }

        DatabaseHelper helper = new DatabaseHelper( FirebaseDatabase.getInstance(), mAccountId );
        mPreferencesDir = helper.getPlayerPrefsRef( mPlayerId );
    }

    private static class CompareWins implements Comparator<CharacterPreference> {
        public int compare( CharacterPreference a, CharacterPreference b ) {
            // put most wins first
            return b.statistics.wins - a.statistics.wins;
        }
    }

    private static class FormatWins implements CharFormatter {
        public void bindValue( ViewHolder vh, CharacterPreference pref ) {
            vh.mCharName.setText( pref.name );
            vh.mCharStat.setText(
                    String.format(Locale.UK, "%d wins", pref.statistics.wins )
            );
        }
    }

    private static class CompareLosses implements  Comparator<CharacterPreference> {
        public int compare( CharacterPreference a, CharacterPreference b ) {
            // put most losses first
            return b.statistics.getLosses() - a.statistics.getLosses();
        }
    }

    private static class FormatLosses implements CharFormatter {
        public void bindValue( ViewHolder vh, CharacterPreference pref ) {
            vh.mCharName.setText( pref.name );
            vh.mCharStat.setText(
                    String.format(Locale.UK, "%d losses", pref.statistics.getLosses() )
            );
        }
    }

    private static class CompareDifference implements  Comparator<CharacterPreference> {
        public int compare( CharacterPreference a, CharacterPreference b ) {
            // put best difference first
            return b.statistics.getDifference() - a.statistics.getDifference();
        }
    }

    private static class FormatDifference implements CharFormatter {
        public void bindValue( ViewHolder vh, CharacterPreference pref ) {
            vh.mCharName.setText( pref.name );
            vh.mCharStat.setText(
                    String.format(Locale.UK, "%d (win-lose)", pref.statistics.getDifference())
            );
        }
    }

    private static class ComparePercent implements  Comparator<CharacterPreference> {
        public int compare( CharacterPreference a, CharacterPreference b ) {
            // put best difference first
            return b.statistics.getWinPercentage() - a.statistics.getWinPercentage();
        }
    }

    private static class FormatPercent implements CharFormatter {
        public void bindValue( ViewHolder vh, CharacterPreference pref ) {
            vh.mCharName.setText( pref.name );
            vh.mCharStat.setText(
                    String.format(Locale.UK, "%d%% (of %d)",
                            pref.statistics.getWinPercentage(),
                            pref.statistics.battles )
            );
        }
    }

    private static class CompareRun implements  Comparator<CharacterPreference> {
        public int compare( CharacterPreference a, CharacterPreference b ) {
            // put best difference first
            return b.statistics.getWinningRun() - a.statistics.getWinningRun();
        }
    }

    private static class FormatRun implements CharFormatter {
        public void bindValue( ViewHolder vh, CharacterPreference pref ) {
            vh.mCharName.setText( pref.name );
            vh.mCharStat.setText(
                    String.format(Locale.UK, "%d win run", pref.statistics.getWinningRun())
            );
        }
    }

    private static class ComparePlayed implements  Comparator<CharacterPreference> {
        public int compare( CharacterPreference a, CharacterPreference b ) {
            // put best difference first
            return b.statistics.battles - a.statistics.battles;
        }
    }

    private static class FormatPlayed implements CharFormatter {
        public void bindValue( ViewHolder vh, CharacterPreference pref ) {
            vh.mCharName.setText( pref.name );
            vh.mCharStat.setText(
                    String.format(Locale.UK, "%d outings", pref.statistics.battles)
            );
        }
    }

    private static class ComparePredicted implements  Comparator<CharacterPreference> {
        private final SharedStatistics mStatistics;

        public ComparePredicted( SharedStatistics statistics ) {
            mStatistics = statistics;
        }

        public int compare( CharacterPreference a, CharacterPreference b ) {
            int diff = b.statistics.getPredictedWins( mStatistics.maxCharBattles ) -
                    a.statistics.getPredictedWins( mStatistics.maxCharBattles );
            if ( 0 == diff ) {
                diff = b.statistics.wins - a.statistics.wins;
            }
            if ( 0 == diff ) {
                diff = b.name.compareTo(a.name);
            }
            return diff;
        }
    }

    private static class FormatPredicted implements CharFormatter {
        private final SharedStatistics mStatistics;

        FormatPredicted( SharedStatistics statistics ) {
            mStatistics = statistics;
        }

        public void bindValue( ViewHolder vh, CharacterPreference pref ) {
            int maxBattles = mStatistics.maxCharBattles;
            int predicted = pref.statistics.getPredictedWins(maxBattles);
            int margin = predicted - pref.statistics.wins;

            vh.mCharName.setText( pref.name );
            vh.mCharStat.setText(
                    String.format(Locale.UK, "%d%% (+/- %d%%)",
                            (100 * predicted) / maxBattles,
                            (100 * margin) / maxBattles ) );
        }
    }

    @Override
    public void onAttach( Context context ) {
        super.onAttach(context);
        try {
            mController = (ViewCharacterResults) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    context.toString() + " must implement ViewCharacterResults");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_stats_char_list, container, false);

        Comparator<CharacterPreference> order = new CompareWins();
        CharFormatter formatter = new FormatWins();
        SharedStatistics statistics = new SharedStatistics();

        if ( ORDER_BY_LOSSES == mStatsType ) {
            order = new CompareLosses();
            formatter = new FormatLosses();
        } else if ( ORDER_BY_DIFFERENCE == mStatsType ) {
            order = new CompareDifference();
            formatter = new FormatDifference();
        } else if ( ORDER_BY_RUN == mStatsType ) {
            order = new CompareRun();
            formatter = new FormatRun();
        } else if ( ORDER_BY_PERCENT == mStatsType ) {
            order = new ComparePercent();
            formatter = new FormatPercent();
        } else if ( ORDER_BY_PLAYED == mStatsType ) {
            order = new ComparePlayed();
            formatter = new FormatPlayed();
        } else if ( ORDER_BY_PREDICTED == mStatsType ) {
            order = new ComparePredicted( statistics );
            formatter = new FormatPredicted( statistics );
        }

        mAdapter = new MyStatsAdapter( mController, order, formatter, statistics );
        mPreferencesDir.addValueEventListener( mAdapter );

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.listStats);
        recyclerView.setLayoutManager( new LinearLayoutManager( v.getContext() ));
        recyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onDestroyView( ) {
        mPreferencesDir.removeEventListener(mAdapter);
        super.onDestroyView();
    }
}
