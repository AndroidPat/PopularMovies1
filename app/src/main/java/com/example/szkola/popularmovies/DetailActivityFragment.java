package com.example.szkola.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();

        ImageView imageView = (ImageView) rootView.findViewById(R.id.imgView);

        if (extras != null) {


            String title = extras.getString("EXTRA_TITLE");
            String user_rating = extras.getString("EXTRA_USER_RATING");
            String plot = extras.getString("EXTRA_PLOT");
            String r_date = extras.getString("EXTRA_R_DATE");
            String img_path = extras.getString("EXTRA_IMG");

            ((TextView) rootView.findViewById(R.id.txtTitle)).setText(title);
            ((TextView) rootView.findViewById(R.id.txtRating)).setText(getActivity().getString(R.string.format_rating, user_rating));
            ((TextView) rootView.findViewById(R.id.txtPlot)).setText(plot);
            if (r_date !=null){
            ((TextView) rootView.findViewById(R.id.txtDate)).setText(r_date.substring(0, 4));}

            Picasso.with(getActivity())
                    .load(img_path)
                    .placeholder(R.drawable.default_placeholder)
                    .resize(450,675)
                    .into(imageView);

        }


        return rootView;
    }
}
