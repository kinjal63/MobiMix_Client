package io.connection.bluetooth.Database;

/**
 * Created by kp49107 on 26-10-2017.
 */
public enum DBUri {
    URI_NEARBY_PLAYERS,
    URI_NEARBY_PLAYERS_BY_ID,
    URI_PLAYER_GAMES,
    URI_PLAYER_GAMES_BY_ID,
    URI_GAME_PARTICIPANTS,
    URI_GAME_PARTICIPANTS_BY_ID;

    public static DBUri getDBUri(int uriCode) {
        for (DBUri uri : DBUri.values()) {
            if (uri.ordinal() == uriCode) {
                return uri;
            }
        }
        return URI_NEARBY_PLAYERS;
    }
}
