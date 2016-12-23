package com.example.nocturna.projectmovie.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by hnoct on 12/22/2016.
 */

public class ReviewAdapter extends BaseAdapter {
    // Member variables
    private Map<String, String> mReviewMap;     // Map<Author, Review>
    private String[] mAuthorArray;              // Array of Authors in order of entry
    private LayoutInflater mInflater;           // For inflating the view
    private Context mContext;                   // Interface for global context

    /**
     * Constructor for ReviewAdapter
     * @param reviewMap A LinkedHashMap containing author-review pairs to be displayed
     * @param context Interface for global context
     */
    public ReviewAdapter(LinkedHashMap<String, String> reviewMap, Context context) {
        this.mReviewMap = reviewMap;
        this.mContext = context;

        // Initialize the LayoutInflater so it can be used in getView()
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getItem(int position) {
        if (mAuthorArray == null) {
            // Create the array filled with author names if it doesn't exist
            mAuthorArray = (String[]) mReviewMap.entrySet().toArray();  // Use entrySet to ensure ordering
        }

        // Return the author (key) at the position
        String author = mAuthorArray[position];
        Review review = new Review(author, mReviewMap.get(author));
        return review;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String author = mAuthorArray[position];
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_review, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.reviewAuthor.setText(author);
        holder.reviewContent.setText(mReviewMap.get(author));
        return convertView;
    }

    @Override
    public int getCount() {
        return mReviewMap.size();
    }

    private static class ViewHolder {
        final TextView reviewContent;
        final TextView reviewAuthor;

        public ViewHolder(View view) {
            this.reviewContent = (TextView) view.findViewById(R.id.list_review_content);
            this.reviewAuthor = (TextView) view.findViewById(R.id.list_review_author);
        }
    }

    private static class Review {
        String reviewAuthor;
        String reviewContent;

        Review(String reviewAuthor, String reviewContent) {
            this.reviewAuthor = reviewAuthor;
            this.reviewContent = reviewContent;
        }
    }
}
