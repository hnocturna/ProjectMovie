package com.example.nocturna.projectmovie.app;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hnoct on 10/10/2016.
 */

public class Movie implements Parcelable {
    private String title;
    private String backdropPath;
    private String userRating;
    private String posterPath;
    private String overview;
    private String releaseDate;
    private Bitmap poster;
    private Bitmap backdrop;

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

    public Movie(Parcel in) {
        this.title = in.readString();
        this.overview = in.readString();
        this.releaseDate = in.readString();
        this.userRating = in.readString();
        this.backdropPath = in.readString();
        this.posterPath = in.readString();
        // this.poster = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeString(userRating);
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
