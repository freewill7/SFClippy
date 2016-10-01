package com.example.will.sfclippy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.will.sfclippy.models.BattleCounter;
import com.example.will.sfclippy.models.CharacterPreference;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
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

    // TODO: Rename and change types of parameters
    private String mAccountId;
    private String mPlayerId;
    private DatabaseHelper mHelper;
    private DatabaseReference mPreferencesDir;
    private BarChart mBarChart;
    private int mStatsType;
    private MyStatsAdapter mAdapter;

    public StatsCharList() {
        // Required empty public constructor
    }

    /**
     * One of the views within the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mCharName;
        public TextView mCharStat;

        public ViewHolder( View view ) {
            super(view);
            mCharName = (TextView) view.findViewById(R.id.lblCharacterName);
            mCharStat = (TextView) view.findViewById(R.id.lblCharacterStat);
        }
    }

    public interface CharFormatter {
        void bindValue( ViewHolder view, CharacterPreference value );
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

        public MyStatsAdapter( Comparator<CharacterPreference> orderer,
                               CharFormatter formatter ) {
            mOrderer = orderer;
            mFormatter = formatter;
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            if ( null != snapshot.getValue()) {
                ArrayList<CharacterPreference> preferences = new ArrayList<>();
                for ( DataSnapshot snap : snapshot.getChildren() ) {
                    CharacterPreference pref = snap.getValue(CharacterPreference.class);
                    if ( null != pref && null != pref.statistics ) {
                        preferences.add( pref );
                    }
                }

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
            View view = (View) LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.layout_char_stat, parent, false );
            ViewHolder vh = new ViewHolder( view );
            return vh;
        }

        @Override
        public void onBindViewHolder( ViewHolder holder, int position ) {
            CharacterPreference pref = mDataset.get(position);
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

        mHelper = new DatabaseHelper( FirebaseDatabase.getInstance(), mAccountId );
        mPreferencesDir = mHelper.getPlayerPrefsRef( mPlayerId );
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_stats_char_list, container, false);

        Comparator<CharacterPreference> order = new CompareWins();
        CharFormatter formatter = new FormatWins();

        if ( ORDER_BY_LOSSES == mStatsType ) {
            order = new CompareLosses();
            formatter = new FormatLosses();
        } else if ( ORDER_BY_DIFFERENCE == mStatsType ) {
            order = new CompareDifference();
            formatter = new FormatDifference();
        }

        mAdapter = new MyStatsAdapter( order, formatter );
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
