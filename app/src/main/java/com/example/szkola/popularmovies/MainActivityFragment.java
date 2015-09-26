package com.example.szkola.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {


    public MainActivityFragment() {
    }


    public static String[] titlesArr;
    public static String[] plotArr;
    public static String[] userRatingArr;
    public static String[] releaseDateArr;
    private ImageAdapter mImageAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        sortMovies();
    }


    private void sortMovies() {

        String popularURL = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=" + getResources().getString(R.string.apiKey);
        String ratingsURL = "http://api.themoviedb.org/3/discover/movie?sort_by=vote_average.desc&vote_count.gte=1000&api_key=" +
                getResources().getString(R.string.apiKey);
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        String preferences = PreferenceManager.getDefaultSharedPreferences(getActivity()).
                getString("sort_list", popularURL);

        switch (preferences) {
            case ("popularity"):
                fetchMoviesTask.execute(popularURL);
                getActivity().setTitle("Popular Movies");
                break;
            case ("rating"):
                fetchMoviesTask.execute(ratingsURL);
                getActivity().setTitle("Highly-Rated Movies");
                break;
            default:
                //display popular movies by default
                getActivity().setTitle("Popular Movies App");
                fetchMoviesTask.execute(popularURL);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.moviesGridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = String.valueOf(titlesArr[position]);
                String user_rating = String.valueOf(userRatingArr[position]);
                String plot = String.valueOf(plotArr[position]);
                String r_date = String.valueOf(releaseDateArr[position]);
                String img_path = String.valueOf(mImageAdapter.getItem(position));
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                Bundle extras = new Bundle();
                extras.putString("EXTRA_TITLE", title);
                extras.putString("EXTRA_USER_RATING", user_rating);
                extras.putString("EXTRA_PLOT", plot);
                extras.putString("EXTRA_R_DATE", r_date);
                extras.putString("EXTRA_IMG", img_path);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });


        return rootView;
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private final String[] posterURLArr;


        public ImageAdapter(Context c, String[] posterURLArr) {
            mContext = c;
            this.posterURLArr = posterURLArr;
        }

        @Override
        public int getCount() {
            if (posterURLArr != null) {
                return posterURLArr.length;
            } else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            if (posterURLArr != null) {
                return posterURLArr[position];
            } else
                return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setAdjustViewBounds(true);
            } else {
                imageView = (ImageView) convertView;
            }


            if (mImageAdapter != null) {
                Picasso.with(mContext)
                        .load(String.valueOf(mImageAdapter.getItem(position)))
                        .placeholder(R.drawable.default_placeholder)
                        .into(imageView);
            }
            return imageView;

        }
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {


//        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private String[] getMovieMeta(String movieJsonStr) throws JSONException {

            JSONObject movieMetaJson = new JSONObject(movieJsonStr);
            JSONArray movieMetaJsonJSONArr = movieMetaJson.getJSONArray("results");

            String[] posterPathsArr = new String[movieMetaJsonJSONArr.length()];
            for (int i = movieMetaJsonJSONArr.length() - 1; i >= 0; i--) {
                posterPathsArr[i] = "http://image.tmdb.org/t/p/w185" + movieMetaJsonJSONArr.getJSONObject(i).getString("poster_path");
            }


            titlesArr = new String[movieMetaJsonJSONArr.length()];
            for (int i = movieMetaJsonJSONArr.length() - 1; i >= 0; i--) {
                titlesArr[i] = movieMetaJsonJSONArr.getJSONObject(i).getString("original_title");
            }

            plotArr = new String[movieMetaJsonJSONArr.length()];
            for (int i = movieMetaJsonJSONArr.length() - 1; i >= 0; i--) {
                plotArr[i] = movieMetaJsonJSONArr.getJSONObject(i).getString("overview");
            }

            userRatingArr = new String[movieMetaJsonJSONArr.length()];
            for (int i = movieMetaJsonJSONArr.length() - 1; i >= 0; i--) {
                userRatingArr[i] = movieMetaJsonJSONArr.getJSONObject(i).getString("vote_average");
            }

            releaseDateArr = new String[movieMetaJsonJSONArr.length()];
            for (int i = movieMetaJsonJSONArr.length() - 1; i >= 0; i--) {
                releaseDateArr[i] = movieMetaJsonJSONArr.getJSONObject(i).getString("release_date");
            }

            return posterPathsArr;
        }


        @Override
        protected String[] doInBackground(String... params) {


            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesMetaStr = null;
            try {
                // URL for movies API
                // More info available at:
                // https://www.themoviedb.org/documentation/api
                URL url = new URL(params[0]);

                // Create the request to themoviedb.org, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesMetaStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the movies data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieMeta(moviesMetaStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] results) {
            if (results == null) {
                Toast.makeText(getActivity(), "error loading movies", Toast.LENGTH_LONG).show();
            }

            mImageAdapter = new ImageAdapter(getActivity(), results);
            if (getView() != null) {
                GridView gridView = (GridView) getView().findViewById(R.id.moviesGridView);
                gridView.setAdapter(mImageAdapter);
            }
        }
    }


}





