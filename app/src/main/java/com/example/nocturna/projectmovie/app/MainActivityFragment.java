package com.example.nocturna.projectmovie.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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
    // private MoviePosterAdapter mMoviePosterAdapter;
    private Movie[] movieArray;                                 // Array of movies that need to be loaded --deprecated?
    private int mCursorPosition;                                // Holds the position of the Cursor to be used by the CursorLoader and the MoviePosterAdapter
    private GridView mGridView;                                 // GridView of the posters

    // Constants
    private static final int MOVIE_LOADER = 0;                  // ID of the CursorLoader for the movies
    private static final String SELECTED_KEY = "SELECTION";     // TODO: Fill this in

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

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
//            final Dialog dialog = new Dialog(getActivity());
//            dialog.setTitle(getString(R.string.sort_dialog_title));
//            dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//            dialog.setContentView(R.layout.sort_dialog);
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

                        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(getActivity());
                        fetchMoviesTask.execute();

                    } else if (sortRadioGroup.getCheckedRadioButtonId() == R.id.radio_top) {
                        editor.putString(
                                getString(R.string.pref_sort_key),
                                getString(R.string.pref_sort_top)
                        );
                        editor.commit();

                        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(getActivity());
                        fetchMoviesTask.execute();
                    }
                    dialog.dismiss();
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // First retrieve the rootView of the fragment so that other views can be selected
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Select the GridView and attach the MoviePosterAdapter custom made for the posters
        GridView gridView = (GridView) rootView.findViewById(R.id.movie_grid);
        // mMoviePosterAdapter = new MoviePosterAdapter(getActivity(), null, 0);
        // gridView.setAdapter(mMoviePosterAdapter);

        // Set onClickItemListener so clicking a poster will lead to DetailsActivity
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Start the DetailsActivity by passing the selected Movie object as an Extra in the
                // Intent
                Movie selectedMovie = movieArray[position];

                Intent intent = new Intent(getActivity(), DetailsActivity.class)
                    .putExtra(MainActivity.EXTRA_MOVIE, selectedMovie);
                startActivity(intent);
            }
        });

        // Run the AsyncTask to download and parse the movie data from TheMovieDB.org
        String sortMode = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(
                        getString(R.string.pref_sort_key),
                        getString(R.string.pref_sort_popular)
                );

        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(getActivity());
        fetchMoviesTask.execute();

        return rootView;
    }

    /**
     * Method called to create the CursorLoader that will cycle through the data being queried
     * @param id ID of the Loader
     * @param args arguments supplied by the caller
     * @return CursorLoader instance that is ready to start loading
     */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Uri moviesUri = MovieContract.MovieEntry.CONTENT_URI;
        return new CursorLoader(
                getActivity(),
                moviesUri,
                new String[] {MovieContract.MovieEntry.COLUMN_POSTER}, // The main screen only shows posters
                null,
                null,
                null
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
        // mMoviePosterAdapter.swapCursor(cursor);
        if (mCursorPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mCursorPosition);
        }
    }

    /**
     * Called when the Loader is reset, making its data unavailable. Points the MovieAdapter at null
     * data.
     * @param loader CursorLoader that has reset
     */
    @Override
    public void onLoaderReset(Loader loader) {
        // mMoviePosterAdapter.swapCursor(null);
    }

}
