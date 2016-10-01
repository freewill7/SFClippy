package com.example.will.sfclippy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.will.sfclippy.models.BattleCounter;
import com.example.will.sfclippy.models.CharacterPreference;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatsBattleDistribution#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatsBattleDistribution extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ACCOUNT_ID = "accountId";
    private static final String ARG_PLAYER_ID = "playerId";
    private static final String TAG = "StatsBattleDistribution";

    private DatabaseHelper mHelper;
    private String mAccountId;
    private String mPlayerId;
    private PieChart mPieChart;
    private DatabaseReference mResultsDir;

    public StatsBattleDistribution() {
        // Required empty public constructor
    }

    private void setupData( DataSnapshot dataset ) {

        List<PieEntry> battles = new ArrayList<>();

        for ( DataSnapshot child : dataset.getChildren() ) {
            CharacterPreference result = child.getValue(CharacterPreference.class);
            BattleCounter statistics = result.statistics;
            if ( null != statistics && statistics.battles > 0 ) {
                battles.add( new PieEntry(statistics.battles, result.name));
            }
        }

        // set-up pie chart
        PieDataSet dataSet = new PieDataSet( battles, "Played" );
        dataSet.setColors( ColorTemplate.MATERIAL_COLORS );

        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.format(Locale.UK, "%d", (int) value);
            }
        });

        mPieChart.setData(data);
        mPieChart.invalidate();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param accountId The account id.
     * @param playerId The user id.
     * @return A new instance of fragment StatsBattleDistribution.
     */
    public static StatsBattleDistribution newInstance(String accountId, String playerId) {
        StatsBattleDistribution fragment = new StatsBattleDistribution();
        Bundle args = new Bundle();
        args.putString(ARG_ACCOUNT_ID, accountId);
        args.putString(ARG_PLAYER_ID, playerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccountId = getArguments().getString(ARG_ACCOUNT_ID);
            mPlayerId = getArguments().getString(ARG_PLAYER_ID);
        }

        mHelper = new DatabaseHelper( FirebaseDatabase.getInstance(), mAccountId );

        mResultsDir = mHelper.getPlayerPrefsRef(mPlayerId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d( TAG, "create overall statistics view");
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_stats_battle_distribution, container, false);
        mPieChart = (PieChart) v.findViewById(R.id.chart);

        // Update the view
        mResultsDir.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mResultsDir.removeEventListener(this);
                setupData( dataSnapshot );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e( TAG, "Fetching battle count", databaseError.toException() );
            }
        });

        return v;
    }
}
