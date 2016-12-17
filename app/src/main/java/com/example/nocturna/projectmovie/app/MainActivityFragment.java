package com.example.nocturna.projectmovie.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.MalformedJsonException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.nocturna.projectmovie.app.data.MovieContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    String LOG_TAG = MainActivityFragment.class.getSimpleName();
    // Member variables
    private MoviePosterAdapter mMoviePosterAdapter;
    private Movie[] movieArray;                                 // Array of movies that need to be loaded --deprecated?
    private int mCursorPosition;                                // Holds the position of the Cursor to be used by the CursorLoader and the MoviePosterAdapter
    private GridView mGridView;                                 // GridView of the posters
    private boolean runOnce = true;

    // Constants
    private static final int MOVIE_LOADER = 0;                  // ID of the CursorLoader for the movies
    private static final String SELECTED_KEY = "SELECTION";     // TODO: Fill this in

    // Column Projection
    String[] MOVIE_COLUMNS = new String[] {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_POSTER,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_POPULARITY
    };

    // Column indices. Tied to MOVIE_COLUMNS
    static final int COL_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_POSTER = 2;
    static final int COL_RATING = 3;
    static final int COL_POPULARITY = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mCursorPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_main_fragment, menu);
        MenuItem item = menu.findItem(R.id.action_sort);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            String sortMode = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(
                            getString(R.string.pref_sort_key),
                            getString(R.string.pref_sort_popular)
                    );

            final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.sort_dialog_title))
                    .setView(R.layout.sort_dialog)
                    .create();

            dialog.show();

            final RadioGroup sortRadioGroup = (RadioGroup) dialog.findViewById(R.id.sort_dialog_radiogroup);
            final RadioButton popularRadio = (RadioButton) dialog.findViewById(R.id.radio_popular);
            final RadioButton topRadio = (RadioButton) dialog.findViewById(R.id.radio_top);
            final Button sortButton = (Button) dialog.findViewById(R.id.sort_dialog_button);

            Log.v(LOG_TAG, "Sort mode: " + sortMode);
            if (sortMode.equals(getString(R.string.pref_sort_popular)) || sortMode == null) {
                sortRadioGroup.check(R.id.radio_popular);
            } else if (sortMode.equals(getString(R.string.pref_sort_top))) {
                sortRadioGroup.check(R.id.radio_top);
            }

            sortButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String sortMode = PreferenceManager.getDefaultSharedPreferences(getActivity())
                            .getString(
                                    getString(R.string.pref_sort_key),
                                    getString(R.string.pref_sort_popular)
                            );
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(getActivity()).edit();
                    if (sortRadioGroup.getCheckedRadioButtonId() == R.id.radio_popular &&
                            sortMode.equals(getString(R.string.pref_sort_popular))) {
                        dialog.dismiss();
                    } else if (sortRadioGroup.getCheckedRadioButtonId() == R.id.radio_top &&
                            sortMode.equals(getString(R.string.pref_sort_top))) {
                        dialog.dismiss();
                    } else if (sortRadioGroup.getCheckedRadioButtonId() == R.id.radio_popular) {
                        editor.putString(
                                getString(R.string.pref_sort_key),
                                getString(R.string.pref_sort_popular)
                        );
                        editor.commit();
                        mMoviePosterAdapter = new MoviePosterAdapter(getActivity(), null, 0);
                        mGridView.setAdapter(mMoviePosterAdapter);
                        onSortModeChanged();
                    } else if (sortRadioGroup.getCheckedRadioButtonId() == R.id.radio_top) {
                        editor.putString(
                                getString(R.string.pref_sort_key),
                                getString(R.string.pref_sort_top)
                        );
                        editor.commit();
                        mMoviePosterAdapter = new MoviePosterAdapter(getActivity(), null, 0);
                        mGridView.setAdapter(mMoviePosterAdapter);
                        onSortModeChanged();

                    }
                    dialog.dismiss();
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    public void onSortModeChanged() {
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // First retrieve the rootView of the fragment so that other views can be selected
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Select the GridView and attach the MoviePosterAdapter custom made for the posters
        mGridView = (GridView) rootView.findViewById(R.id.movie_grid);
        mMoviePosterAdapter = new MoviePosterAdapter(getActivity(), null, 0);
        mGridView.setAdapter(mMoviePosterAdapter);

        // Set onClickItemListener so clicking a poster will lead to DetailsActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the movieId
                Cursor cursor = (Cursor) mMoviePosterAdapter.getItem(position);
                long movieId = cursor.getLong(COL_MOVIE_ID);

                // Generate the URI for the row pointing to the movie
                Uri movieUri = MovieContract.MovieEntry.buildMovieUriFromId(movieId);

                // Start an intent with the URI attached to it to launch the DetailsActivity
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.setData(movieUri);
                startActivity(intent);
            }
        });



        return rootView;
    }
     private void runOnce() {
         // Run the AsyncTask to download and parse the movie data from TheMovieDB.org
         FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(getActivity());
         fetchMoviesTask.execute();
     }

    /**
     * Method called to create the CursorLoader that will cycle through the data being queried
     * @param id ID of the Loader
     * @param args arguments supplied by the caller
     * @return CursorLoader instance that is ready to start loading
     */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        // Constants
        Context context = getActivity();
        Uri moviesUri = MovieContract.MovieEntry.CONTENT_URI;

        // Sort mode: by popularity or rating
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortMode = prefs.getString(
                context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_popular)
        );

        // How to sort the rows of the cursor
        String sortOrder = null;
        if (sortMode.equals(context.getString(R.string.pref_sort_popular))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        } else if (sortMode.equals(context.getString(R.string.pref_sort_top))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_RATING + " DESC";
        } else {
            throw new UnsupportedOperationException("Unknown sort method: " + sortMode);
        }
        return new CursorLoader(
                getActivity(),
                moviesUri,
                MOVIE_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    /**
     * Swaps data the MovieAdapter is referencing for the new data after the CursorLoader has its
     * load.
     * @param loader CursorLoader that has finished loading its data
     * @param cursor data returned by the CursorLoader
     */
    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        mMoviePosterAdapter.swapCursor(cursor);
        if (mCursorPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mCursorPosition);
        }

        if (runOnce) {
            runOnce();
            runOnce = false;
        }
    }

    /**
     * Called when the Loader is reset, making its data unavailable. Points the MovieAdapter at null
     * data.
     * @param loader CursorLoader that has reset
     */
    @Override
    public void onLoaderReset(Loader loader) {
        mMoviePosterAdapter.swapCursor(null);
    }

}
