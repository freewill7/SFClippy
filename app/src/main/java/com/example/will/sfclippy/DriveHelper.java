package com.example.will.sfclippy;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.api.client.util.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 10/08/2016.
 */
public class DriveHelper {
    private final GoogleApiClient apiClient;
    private final DriveFolder mAppFolder;
    private static String PLAYERS_DB = "players.json";
    private static String STATE_DB = "state.json";
    private static String RESULTS_DB = "results.json";

    public DriveHelper( GoogleApiClient client ) {
        this.apiClient = client;
        this.mAppFolder = Drive.DriveApi.getAppFolder(apiClient);
    }

    /**
     * Utility class for encoding/decoding player info.
     */
    protected static class PojoPlayer {
        public String playerId;
        public String playerName;

        public PojoPlayer(DataProvider.PlayerInfo player) {
            this.playerId = player.getPlayerId();
            this.playerName = player.getPlayerName();
        }

        DataProvider.PlayerInfo toPlayerInfo( ) {
            return new DataProvider.PlayerInfo( playerId, playerName );
        }
    };

    /**
     * Utility class for encoding/decoding state.
     */
    private static class PojoState {
        public String player1Id;
        public String player2Id;

        public PojoState(DataProvider.CurrentState state) {
            this.player1Id = state.getPlayer1Id();
            this.player2Id = state.getPlayer2Id();
        }

        public DataProvider.CurrentState toCurrentState( ) {
            return new DataProvider.CurrentState( player1Id, player2Id );
        }
    };

    /**
     * Utility class for encoding/decoding result.
     */
    private static class PojoResult {
        public String datetime;
        public String p1Id;
        public String p1Character;
        public String p2Id;
        public String p2Character;
        public String winnerId;
        final static private SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        public PojoResult(DataProvider.BattleResult result) {
            this.datetime = dateFormat.format( result.getDate() );
            this.p1Id = result.getP1Id();
            this.p1Character = result.getP1Character();
            this.p2Id = result.getP2Id();
            this.p2Character = result.getP2Character();
            this.winnerId = result.getWinnerId();
        }

        public DataProvider.BattleResult toBattleResult( ) throws ParseException {
            DataProvider.BattleResult ret = new DataProvider.BattleResult(
                    dateFormat.parse(datetime),
                    p1Id,
                    p1Character,
                    p2Id,
                    p2Character,
                    winnerId );
            return ret;
        }
    }

    public static class PojoCharacter {
        public String name;
        public int score;

        public PojoCharacter( String name, int score ) {
            this.name = name;
            this.score = score;
        }

        public DataProvider.CharacterPreference toCharacterPreference() {
            DataProvider.CharacterPreference pref = new DataProvider.CharacterPreference( name,
                    score );
            return pref;
        }
    }

    private DriveFile getFile( DriveFolder directory, String title ) {
        DriveFile ret = null;

        PendingResult<DriveApi.MetadataBufferResult> results = directory.listChildren(apiClient);
        MetadataBuffer buffer = results.await().getMetadataBuffer();
        for ( Metadata metadata : buffer ) {
            if ( 0 == metadata.getTitle().compareTo( title ) ) {
                ret = metadata.getDriveId().asDriveFile();
                break;
            }
        }
        buffer.release();

        return ret;
    }

    private <T> T fetchFromDrive( String title, Class<T> classOfT ) {
        DriveFile file = getFile(mAppFolder, title);
        if ( null != file ) {
            DriveApi.DriveContentsResult result =
                    file.open( apiClient, DriveFile.MODE_READ_ONLY, null ).await();
            if ( ! result.getStatus().isSuccess() ) {
                return null;
            }

            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader( result.getDriveContents().getInputStream() );
            T ret = gson.fromJson( reader, classOfT );
            return ret;
        }

        return null;
    }

    private <T> void storeIntoDrive( String title, T toStore )
            throws IOException {
        PendingResult<DriveApi.DriveContentsResult> result =
                Drive.DriveApi.newDriveContents( apiClient  );
        DriveApi.DriveContentsResult contentResult = result.await();

        Gson gson = new GsonBuilder().create();
        String str = gson.toJson( toStore );

        OutputStream stream = contentResult.getDriveContents().getOutputStream();
        stream.write(str.getBytes(Charsets.UTF_8));

        MetadataChangeSet metadata = new MetadataChangeSet.Builder()
                .setTitle(title)
                .setMimeType("application/json")
                .build();

        mAppFolder.createFile( apiClient, metadata, contentResult.getDriveContents() ).await();
    }

    protected void storePlayers( List<DataProvider.PlayerInfo> players )
            throws IOException {
        PojoPlayer[] pojoPlayers = new PojoPlayer[players.size()];

        int idx = 0;
        for (DataProvider.PlayerInfo player : players) {
            pojoPlayers[idx] = new PojoPlayer(player);
            idx++;
        }
        storeIntoDrive( PLAYERS_DB, players );
    }

    protected List<DataProvider.PlayerInfo> fetchPlayers(  ) {
        PojoPlayer[] players = fetchFromDrive( PLAYERS_DB, PojoPlayer[].class );
        if ( null == players ) {
            return null;
        }

        ArrayList<DataProvider.PlayerInfo> ret = new ArrayList<>( players.length );
        for ( PojoPlayer pojo : players ) {
            ret.add( pojo.toPlayerInfo() );
        }
        return ret;
    }

    protected void storeState( DataProvider.CurrentState state )
            throws IOException {

        storeIntoDrive( STATE_DB, new PojoState(state) );
    }

    protected DataProvider.CurrentState fetchState( ) {
        PojoState pojo = fetchFromDrive( STATE_DB, PojoState.class );
        if ( null == pojo ) {
            return null;
        }
        return pojo.toCurrentState();
    }

    protected void storeResults( List<DataProvider.BattleResult> results )
            throws IOException {
        PojoResult[] pojos = new PojoResult[results.size()];
        int idx = 0;
        for ( DataProvider.BattleResult result : results ) {
            pojos[idx] = new PojoResult(result);
            idx++;
        }
        storeIntoDrive( RESULTS_DB, pojos );
    }

    protected List<DataProvider.BattleResult> fetchResults( ) {
        PojoResult[] pojos = fetchFromDrive( RESULTS_DB, PojoResult[].class);
        if ( null == pojos ) {
            return null;
        }

        ArrayList<DataProvider.BattleResult> results = new ArrayList<>( pojos.length );
        for ( PojoResult pojo : pojos ) {
            try {
                results.add( pojo.toBattleResult() );
            } catch ( ParseException parseError ) {
                Log.e( getClass().getName(), "Problem parsing battle result", parseError);
            }
        }
        return results;
    }

    static String getCharsPath( String playerId ) {
        return "characters_" + playerId + ".json";
    }

    protected void storeCharacters( String playerId,
                                    List<DataProvider.CharacterPreference> characters )
            throws IOException {
        PojoCharacter[] pojos = new PojoCharacter[characters.size()];
        int idx = 0;
        for ( DataProvider.CharacterPreference pref : characters ) {
            pojos[idx] = new PojoCharacter( pref.getCharacterName(), pref.getScore() );
            idx++;
        }
        storeIntoDrive( getCharsPath(playerId), pojos );
    }

    protected List<DataProvider.CharacterPreference> fetchCharacters( String playerId ) {
        PojoCharacter[] pojos = fetchFromDrive( getCharsPath(playerId), PojoCharacter[].class);
        if ( null == pojos ) {
            return null;
        }
        
        List<DataProvider.CharacterPreference> ret = new ArrayList<>();
        for ( PojoCharacter pojo : pojos ) {
            ret.add( pojo.toCharacterPreference() );
        }
        return ret;
    }

}
