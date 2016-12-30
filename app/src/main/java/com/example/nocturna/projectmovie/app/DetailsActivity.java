package com.example.nocturna.projectmovie.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.nocturna.projectmovie.app.R;

public class DetailsActivity extends AppCompatActivity {
    final String LOG_TAG = DetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Bundle args = new Bundle();
        args.putParcelable(DetailsActivityFragment.DETAILS_URI, getIntent().getData());

        DetailsActivityFragment detailfragment = new DetailsActivityFragment();
        detailfragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.movie_detail_container, detailfragment)
                .commit();
    }

}
