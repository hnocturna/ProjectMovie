package com.example.nocturna.projectmovie.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {
    // Constants
    public static final String EXTRA_MOVIE = "movie";
    private final String DETAILFRAGMENT_TAG = "DFTAG";
    boolean DEVELOPER_MODE = false;

    // Member Variables
    static boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.movie_detail_container) == null) {
            // Container is not found, therefore phone mode does not require any extra actions
            mTwoPane = false;
//            getSupportActionBar().setElevation(0f);
        } else {
            // Layout contains movie_detail_container and therefore there are two panes in view
            mTwoPane = true;
            if (savedInstanceState == null) {
                // Screen not rotated, activity created for first time. Load new
                // DetailsActivityFragment into container
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailsActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        MainActivityFragment mainFragment = (MainActivityFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_fragment);
        if (mainFragment != null) {
            // Start the service for downloading movies
            mainFragment.refreshMovies();
        }
        super.onResume();
    }

    @Override
    public void onItemSelected(Uri movieUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailsActivityFragment.DETAILS_URI, movieUri);

            DetailsActivityFragment detailFragment = new DetailsActivityFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, detailFragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailsActivity.class)
                .setData(movieUri);
            startActivity(intent);
        }
    }
}
