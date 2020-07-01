package com.codepath.apps.restclienttemplate.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApplication;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.TweetsAdapter;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    private final int  REQUEST_CODE = 20;

    TweetDao tweetDao;
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout scRefresh;
    EndlessRecyclerViewScrollListener scrollListener;
    //MenuItem miActionProgressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApplication.getRestClient(this);
        tweetDao = ((TwitterApplication) getApplicationContext()).getMyDatabase().tweetDao();

        // Used for toolbar manipulation programmatically
        Toolbar bar = findViewById(R.id.tbMain);

        // Setting click listener for toolbar menu
        bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.composeTweet) {
                    // Compose icon has been selected
                    // Navigate to the compose activity
                    Intent intent = new Intent(getApplicationContext(), ComposeActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                    return true;
                }
                return false;
            }
        });

        // Find SwipeRefreshLayout
        scRefresh = findViewById(R.id.scRefresh);

        // Configure the refreshing colors
        scRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        scRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "Fetching new data");
                populateHomeTimeline();
            }
        });

        // Find the recycler view
        rvTweets = findViewById(R.id.rvTweets);

        // Adds divider to RecyclerView to divide tweets
        rvTweets.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        // Init the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        // Init LinearLayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        // Recycler view setup: layout manager and the adapter
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.setAdapter(adapter);

        // Creates new scroll listener in order to implement Infinite Scroll
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                
                Log.i(TAG, "onLoadMore: " + page);
                loadMoreData();
            }
        };

        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Showing data from database");
                List<TweetWithUser> tweetWithUsers = tweetDao.recentItems();
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
                adapter.clear();
                adapter.addAll(tweetsFromDB);
            }
        });
        populateHomeTimeline();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            // Get data from the intent (tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

            // Update the RecyclerView with the tweet
            // Modify data source of tweets
            tweets.add(0, tweet);

            // Update the adapter
            adapter.notifyItemChanged(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadMoreData() {
        // 1. Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess for loadMoreData" + json.toString());

                // 2. Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);

                    // 3. Append the new data objects to the existing set of items inside the array of items
                    // 4. Notify the adapter of the new items made with 'notifyItemRangeInserted()'
                    adapter.addAll(tweets);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure for loadMoreData", throwable);
            }
        }, tweets.get(tweets.size() - 1).id);


    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    final List <Tweet> tweetsFromNetwork = Tweet.fromJsonArray(jsonArray);
                    adapter.clear();
                    adapter.addAll(tweetsFromNetwork);

                    // Now we call setRefreshing(false) to signal refresh has finished
                    scRefresh.setRefreshing(false);

                    //
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Saving data into database");
                            // Insert users first
                            List<User> usersFromNetwork = User.fromJsonTweetArray(tweetsFromNetwork);
                            tweetDao.insertModel(usersFromNetwork.toArray(new User[0]));

                            // Insert tweets next
                            tweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet[0]));
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure " + response, throwable);
            }
        });
    }
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        // Store instance of the menu item containing progress
//        miActionProgressItem = menu.findItem(R.id.miActionProgress);
//
//        // Return to finish
//        return super.onPrepareOptionsMenu(menu);
//    }
//    public void showProgressBar() {
//        // Show progress item
//        miActionProgressItem.setVisible(true);
//    }
//
//    public void hideProgressBar() {
//        // Hide progress item
//        miActionProgressItem.setVisible(false);
//    }
}

