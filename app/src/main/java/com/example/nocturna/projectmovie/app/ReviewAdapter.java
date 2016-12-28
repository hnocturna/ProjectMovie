package com.example.nocturna.projectmovie.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by hnoct on 12/22/2016.
 */

public class ReviewAdapter extends BaseAdapter {
    // Member variables
    private Map<String, String> mReviewMap;             // Map<Author, Review>
    private List<String> mAuthorList;                   // List of Authors in order of entry to correlate with position
    private LayoutInflater mInflater;                   // For inflating the view
    private Context mContext;                           // Interface for global context
    private List<Integer> mExpandedList;                // List containing all positions that should show expanded review content
    private float mPixels;                              // Set to 1 dip

    // ViewType constants
    private static final int VIEW_TYPE_CONTRACTED = 0;
    private static final int VIEW_TYPE_EXPANDED = 1;

    /**
     * Constructor for ReviewAdapter
     * @param reviewMap A LinkedHashMap containing author-review pairs to be displayed
     * @param context Interface for global context
     */
    public ReviewAdapter(LinkedHashMap<String, String> reviewMap, Context context) {
        this.mReviewMap = reviewMap;
        this.mContext = context;

        // Initialize the list holding all positions that should have an expanded content
        mExpandedList = new ArrayList<>();

        // Initialize the mPixel value set to 1 dip
        mPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mContext.getResources().getDisplayMetrics());

        // Initialize the LayoutInflater so it can be used in getView()
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getItem(int position) {
        if (mAuthorList == null) {
            // Create the list filled with author names if it doesn't exist
            mAuthorList = new LinkedList<>();

            for (String author : mReviewMap.keySet()) {
                mAuthorList.add(author);
            }
        }

        // Return the author (key) at the position
        String author = mAuthorList.get(position);
        Review review = new Review(author, mReviewMap.get(author));
        return review;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Adds the clicked item to a list of content that has been expanded
     * @param position position of the item that has been clicked
     */
    public void expandContent(int position) {
        if (!mExpandedList.contains(position)) {
            mExpandedList.add(position);
            notifyDataSetChanged();
        }
    }

    /**
     *
     * @param position position of the item that has been clicked
     */
    public void contractContent (int position) {
        if (mExpandedList.contains(position)) {
            mExpandedList.remove(position);
            notifyDataSetChanged();
        }
    }

    public boolean isExpanded(int position) {
        if (mExpandedList.contains(position)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mExpandedList.contains(position)) {
            return VIEW_TYPE_EXPANDED;
        } else {
            return VIEW_TYPE_CONTRACTED;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Variables
        int layoutId = -1;

        // Create the author list if it does not exist
        if (mAuthorList == null) {
            mAuthorList = new LinkedList<>();
            for (String author : mReviewMap.keySet()) {
                mAuthorList.add(author);
            }
        }

        // Set the ViewHolder as a tag so it can be retrieved instead of generating a new View
        // each time
        String author = mAuthorList.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_review, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Populate the views
        if (getItemViewType(position) == VIEW_TYPE_EXPANDED) {
            // If view is expanded, max height is set to max integer to allow for full content of
            // review
            holder.reviewContent.setMaxHeight(2147483647);
        } else {
            // If view is contracted content, then max height is set to only allow preview of content
            holder.reviewContent.setMaxHeight(Math.round(118 * mPixels));
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
