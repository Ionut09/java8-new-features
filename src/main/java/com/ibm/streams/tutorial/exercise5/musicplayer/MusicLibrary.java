package com.ibm.streams.tutorial.exercise5.musicplayer;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public interface MusicLibrary {

    /**
     * @return every song in the collection, in no specified order.
     */
    Collection<Song> allSongs();

    /**
     * Will sort a given collection of Songs by artist.
     */
    static class SongByArtistSorter {
        public static List<Song> sort(Collection<Song> songs) {
            return songs.stream().sorted((song1, song2) -> song1.getArtist().compareTo(song2.getArtist()))
                        .collect(toList());
        }
    }

    /**
     * Provides a rating for this song, between 1-100, inclusive.
     *
     * Default implementation takes a rating by normalising the play count for the given song with the play count for
     * all songs in this MusicLibrary.
     */
    default Rating ratingOf(Song song) {
        int totalPlayCount = allSongs().stream().mapToInt(this::timesPlayed).sum();
        float score = (timesPlayed(song) / totalPlayCount) * 100.0f;
        return new Rating(Math.round(score));
    }

    int timesPlayed(Song song);
}
