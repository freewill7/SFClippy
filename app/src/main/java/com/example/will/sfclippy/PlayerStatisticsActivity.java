package com.example.will.sfclippy;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class PlayerStatisticsActivity extends AppCompatActivity
implements StatsCharList.ViewCharacterResults {
    private String mAccountId;
    private String mPlayerId;
    private String mPlayerName;

    public static final String ACCOUNT_ID = "account_id";
    public static final String PLAYER_ID = "player_id";
    public static final String PLAYER_NAME = "player_name";
    private static final int CHOICE_WINS = 1;
    private static final int CHOICE_LOSSES = 2;
    private static final int CHOICE_DIFF = 3;
    private static final int CHOICE_RUN = 4;
    private static final int CHOICE_PERCENT = 5;
    private static final int NUM_ITEMS = 6;
    private static final String TAG = "PlayerStatistics";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_statistics);

        Intent intent = getIntent();
        mAccountId = intent.getStringExtra(ACCOUNT_ID);
        mPlayerId = intent.getStringExtra(PLAYER_ID);
        mPlayerName = intent.getStringExtra(PLAYER_NAME);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarStats);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(mPlayerName + " statistics");

        MyAdapter adapter = new MyAdapter( getSupportFragmentManager(), mAccountId, mPlayerId );

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter( adapter );

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode( TabLayout.MODE_SCROLLABLE );
    }

    public static class MyAdapter extends FragmentPagerAdapter {
        private String mAccountId;
        private String mPlayerId;

        public MyAdapter(FragmentManager fm,
                         String accountId,
                         String playerId ) {
            super(fm);
            mAccountId = accountId;
            mPlayerId = playerId;
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            if ( 0 == position) {
                return StatsBattleDistribution.newInstance(mAccountId, mPlayerId);
            } else if ( CHOICE_WINS == position ){
                return StatsCharList.newInstance(mAccountId, mPlayerId, StatsCharList.ORDER_BY_WINS);
            } else if ( CHOICE_LOSSES == position ) {
                return StatsCharList.newInstance(mAccountId, mPlayerId, StatsCharList.ORDER_BY_LOSSES);
            } else if ( CHOICE_DIFF == position ) {
                return StatsCharList.newInstance(mAccountId, mPlayerId, StatsCharList.ORDER_BY_DIFFERENCE);
            } else if ( CHOICE_RUN == position ) {
                return StatsCharList.newInstance(mAccountId, mPlayerId, StatsCharList.ORDER_BY_RUN);
            } else if ( CHOICE_PERCENT == position ) {
                return StatsCharList.newInstance(mAccountId, mPlayerId, StatsCharList.ORDER_BY_PERCENT);
            } else {
                Log.e( TAG, "No such item at position " + position);
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if ( 0 == position ) {
                return "Choices";
            } else if ( CHOICE_WINS == position ) {
                return "Wins";
            } else if ( CHOICE_LOSSES == position ) {
                return "Losses";
            } else if ( CHOICE_DIFF == position ) {
                return "Diff";
            } else if ( CHOICE_RUN == position ) {
                return "Run";
            } else if ( CHOICE_PERCENT == position ) {
                return "Percent";
            } else {
                return "Unknown";
            }
        }
    }

    @Override
    public void viewCharacterResults( String character ) {
        StatsBattleList frag = StatsBattleList.newInstance(
                mAccountId,
                mPlayerId,
                character );
        frag.show( getFragmentManager(), "frame name" );
    }
}
