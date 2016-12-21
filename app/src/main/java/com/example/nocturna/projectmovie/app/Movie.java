package com.example.nocturna.projectmovie.app;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hnoct on 10/10/2016.
 */

public class Movie implements Parcelable {
    private long movieId;

    private String title;
    private String backdropPath;
    private String posterPath;
    private String overview;
    private String trailerPath;

    private long releaseDate;

    private double popularity;
    private double userRating;

    private int[] genreIds;

    private Bitmap poster;
    private Bitmap backdrop;

    public Movie(long movieId, String title, String overview, long releaseDate, double userRating, double popularity, String posterPath, String backdropPath, int[] genreIds) {
        this.movieId = movieId;
        this.title = title;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.userRating = userRating;
        this.popularity = popularity;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.genreIds = genreIds;
    }

    public void addBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public void addUserRating(double userRating) {
        this.userRating = userRating;
    }

    public void addPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public void addOverview(String overview) {
        this.overview = overview;
    }

    public void addReleaseDate(long releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void addPoster(Bitmap poster) {
        this.poster = poster;
    }

    public void addBackdrop(Bitmap backdrop) {
        this.backdrop = backdrop;
    }

    public void addTrailerPath(String trailerPath) {
        this.trailerPath = trailerPath;
    }

    public long getId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public String getTrailerPath() {
        return trailerPath;
    }

    public double getUserRating() {
        return userRating;
    }

    public double getPopularity() {
        return popularity;
    }

    public int[] getGenreIds() {
        return genreIds;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public long getReleaseDate() {
        return releaseDate;
    }

    public Bitmap getPoster() {
        return poster;
    }

    public Bitmap getBackdrop() {
        return backdrop;
    }

    public Movie(Parcel in) {
        this.title = in.readString();
        this.overview = in.readString();
        this.releaseDate = in.readLong();
        this.userRating = in.readDouble();
        this.backdropPath = in.readString();
        this.posterPath = in.readString();
        // this.poster = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(overview);
        dest.writeLong(releaseDate);
        dest.writeDouble(userRating);
        dest.writeString(backdropPath);
        dest.writeString(posterPath);
        // dest.writeValue(poster);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
