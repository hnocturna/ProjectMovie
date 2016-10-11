package com.example.nocturna.projectmovie;

import android.graphics.Bitmap;

/**
 * Created by hnoct on 10/10/2016.
 */

public class Movie {
    String title;
    String backdropPath;
    String userRating;
    String posterPath;
    String overview;
    String releaseDate;
    Bitmap poster;
    Bitmap backdrop;

    public Movie(String title, String overview, String releaseDate, String userRating, String posterPath, String backdropPath) {
        this.title = title;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.userRating = userRating;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
    }

    public void addBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public void addUserRating(String userRating) {
        this.userRating = userRating;
    }

    public void addPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public void addOverview(String overview) {
        this.overview = overview;
    }

    public void addReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void addPoster(Bitmap poster) {
        this.poster = poster;
    }

    public void addBackdrop(Bitmap backdrop) {
        this.backdrop = backdrop;
    }

    public String getTitle() {
        return title;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public String getUserRating() {
        return userRating;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public Bitmap getPoster() {
        return poster;
    }

    public Bitmap getBackdrop() {
        return backdrop;
    }
}
