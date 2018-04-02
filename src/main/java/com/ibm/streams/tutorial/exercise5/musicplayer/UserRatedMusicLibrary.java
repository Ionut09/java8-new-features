package com.ibm.streams.tutorial.exercise5.musicplayer;

public interface UserRatedMusicLibrary extends MusicLibrary {

    StarRating userRatingOf(Song song);

    static class StarRatingConverter {
        public Rating convert(StarRating starRating) {
            return new Rating(starRating.numberOfStars * 20);
        }
    }
}
