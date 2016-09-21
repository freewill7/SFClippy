package com.example.will.sfclippy;

import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CharacterRatingFragment.RatingInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CharacterRatingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CharacterRatingFragment extends DialogFragment
        implements RatingBar.OnRatingBarChangeListener, View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CHARACTER_NAME = "characterName";
    private static final String ARG_INITIAL_RATING = "initialRating";
    private static final String ARG_TOTAL_BATTLES = "totalBattles";
    private static final String ARG_TOTAL_WINS = "totalWins";

    private String mCharacterName;
    private int mInitialRating;
    private int mTotalBattles;
    private int mTotalWins;
    private RatingBar mRatingBar;
    private Button mButton;
    private TextView mLabelFirst;
    private TextView mLabelSecond;

    private RatingInteractionListener mListener;

    public CharacterRatingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param characterName The name of the character.
     * @param initialRating Initial rating for calendar.
     * @param totalBattles Total battles for this character.
     * @param totalWins Total wins for this character.
     * @return A new instance of fragment CharacterRatingFragment.
     */
    public static CharacterRatingFragment newInstance(String characterName, int initialRating,
                                                      int totalBattles, int totalWins ) {
        CharacterRatingFragment fragment = new CharacterRatingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHARACTER_NAME, characterName);
        args.putInt(ARG_INITIAL_RATING, initialRating);
        args.putInt(ARG_TOTAL_BATTLES, totalBattles);
        args.putInt(ARG_TOTAL_WINS, totalWins);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCharacterName = getArguments().getString(ARG_CHARACTER_NAME);
            mInitialRating = getArguments().getInt(ARG_INITIAL_RATING);
            mTotalBattles = getArguments().getInt(ARG_TOTAL_BATTLES);
            mTotalWins = getArguments().getInt(ARG_TOTAL_WINS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_character_rating, container, false);

        TextView textView = (TextView) view.findViewById(R.id.labelTitle);
        textView.setText( mCharacterName );

        mRatingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        mRatingBar.setRating( mInitialRating );
        mRatingBar.setOnRatingBarChangeListener(this);

        mButton  = (Button) view.findViewById(R.id.btnSelectCharacter);
        mButton.setOnClickListener(this);

        mLabelFirst = (TextView) view.findViewById(R.id.lblTotalBattles);
        mLabelSecond = (TextView) view.findViewById(R.id.lblTotalWins);

        if ( 0 != mTotalBattles ) {
            mLabelFirst.setText(
                    String.format(Locale.UK, "Played %d times",
                            mTotalBattles )
            );
            mLabelSecond.setText(
                    String.format(Locale.UK, "Won %d battles (%d%%)",
                            mTotalWins, (100 * mTotalWins) / mTotalBattles )
            );
        }

        return view;
    }

    @Override
    public void onRatingChanged( RatingBar ratingBar, float rating, boolean fromUser ) {
        if (mListener != null) {
            mListener.onRatingChange( mCharacterName, (int) rating);
            dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RatingInteractionListener) {
            mListener = (RatingInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick( View v ) {
        if ( v == mButton ) {
            mListener.onCharacterSelected( mCharacterName );
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface RatingInteractionListener {
        /**
         * Notify of a change in rating.
         * @param name The character whose rating changed.
         * @param rating The new rating.
         */
        void onRatingChange(String name, int rating);

        /**
         * Notify of a character choice.
         * @param name The character name.
         */
        void onCharacterSelected( String name );
    }
}
