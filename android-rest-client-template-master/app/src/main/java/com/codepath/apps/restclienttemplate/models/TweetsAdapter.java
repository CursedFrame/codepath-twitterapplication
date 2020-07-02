package com.codepath.apps.restclienttemplate.models;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.ParseException;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.activities.ComposeActivity;
import com.codepath.apps.restclienttemplate.activities.TimelineActivity;
import com.codepath.apps.restclienttemplate.fragments.TweetDialogFragment;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    public static final String TAG = "TweetsAdapter";

    Context context;
    List<Tweet> tweets;
    TwitterClient client;
    FragmentManager fragmentManager;

    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets, TwitterClient client, FragmentManager fragmentManager) {
        this.context = context;
        this.tweets = tweets;
        this.client = client;
        this.fragmentManager = fragmentManager;
    }

    // For each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);

        // Bind the tweet with view holder
        holder.bind(tweet);
    }

    // Get number of Tweets
    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of Tweets
    public void addAll(List<Tweet> tweetList){
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }



    // Define a ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Define Views within each Tweet
        ImageView ivProfileImage;
        ImageView ivTweetImage;
        ImageView ivRetweet;
        ImageView ivLike;
        ImageView ivReply;
        TextView tvTweetText;
        TextView tvUserName;
        TextView tvTimeStamp;
        TextView tvName;
        TextView tvFavoriteCount;
        TextView tvRetweetCount;


        // ViewHolder constructor
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            ivTweetImage =  itemView.findViewById(R.id.ivTweetImage);
            tvTweetText = itemView.findViewById(R.id.tvTweetText);
            tvTimeStamp = itemView.findViewById(R.id.tvTimeStamp);
            ivRetweet = itemView.findViewById(R.id.ivRetweet);
            ivLike = itemView.findViewById(R.id.ivLike);
            ivReply = itemView.findViewById(R.id.ivReply);
            tvFavoriteCount = itemView.findViewById(R.id.tvFavoriteCount);
            tvRetweetCount = itemView.findViewById(R.id.tvRetweetCount);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showTweetDialog();
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.N) // For getting recommended timestamp function to correct version
        // Function to bind information to view
        public void bind(final Tweet tweet){

            // Values for rounded corners transformation
            int radius = 30; // corner radius, higher value = more rounded
            int margin = 10; // crop margin, set to 0 for corners with no crp

            // Set text values for TextViews
            tvTweetText.setText(tweet.body);
            tvName.setText(tweet.user.name);
            tvUserName.setText(tweet.user.userName);
            tvTimeStamp.setText(getRelativeTimeAgo(tweet.createdAt));

            if (tweet.favoriteCount != null) {
                tvFavoriteCount.setText(tweet.favoriteCount);
            }
            else {
                tvFavoriteCount.setText("");
            }
            if (tweet.retweetCount != null) {
                tvRetweetCount.setText(tweet.retweetCount);
            }
            else {
                tvFavoriteCount.setText("");
            }

            // Set image for ivProfileimage
            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .transform(new RoundedCornersTransformation(radius, margin))
                    .into(ivProfileImage);

            // Set image for ivTweetImage
            if(tweet.entities != null) {
                if (tweet.entities.media != null) {
                    Log.i(TAG, "bind: Attaching tweet image");

                    // Input and transform image through Glide
                    Glide.with(context)
                            .load(tweet.entities.media.get(0).mediaUrl)
                            .transform(new RoundedCornersTransformation(radius, margin))
                            .into(ivTweetImage);

                    Log.i(TAG, "URL:" + tweet.entities.media.get(0).mediaUrl);

                    // Set View to VISIBLE in the case that View is GONE (below else-if)
                    ivTweetImage.setVisibility(View.VISIBLE);
                }
            }
            else {
                ivTweetImage.setVisibility(View.GONE);
            }

            // Set image for ivLike
            if (tweet.favorited){
                ivLike.setImageResource(R.drawable.ic_vector_heart);
            }
            else {
                ivLike.setImageResource(R.drawable.ic_vector_heart_stroke);
            }

            // Set image for ivRetweet
            if (tweet.retweeted){
                ivRetweet.setImageResource(R.drawable.ic_vector_retweet);
            }
            else {
                ivRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
            }

            ivRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!tweet.retweeted){
                        client.retweetTweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                               if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                                   ((TimelineActivity) context).populateSingleTweet(tweet, getAdapterPosition());
                               }
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "onFailure to retweet",  throwable);
                            }
                        });
                    }
                    else {
                        client.unretweetTweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                                    ((TimelineActivity) context).populateSingleTweet(tweet, getAdapterPosition());
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "onFailure to unretweet",  throwable);
                            }
                        });
                    }
                }
            });

            // Setting like functionality for tweet
            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   if (!tweet.favorited){
                       // Execute API call for liking
                       client.favoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                           @Override
                           public void onSuccess(int statusCode, Headers headers, JSON json) {
                               if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                                   ((TimelineActivity) context).populateSingleTweet(tweet, getAdapterPosition());
                               }
                           }

                           @Override
                           public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                               Log.e(TAG, "onFailure to like",  throwable);
                           }
                       });

                   }
                   else {
                       // Execute API call for unliking
                       client.unfavoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                           @Override
                           public void onSuccess(int statusCode, Headers headers, JSON json) {
                               if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                                   ((TimelineActivity) context).populateSingleTweet(tweet, getAdapterPosition());
                               }
                           }

                           @Override
                           public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                               Log.e(TAG, "onFailure to unlike",  throwable);
                           }
                       });
                   }

                    // After liking, execute API call to return tweet and update tweet.favorited
                    client.showTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            JSONObject jsonObject = json.jsonObject;

                            try {
                                tweet.favorited = jsonObject.getBoolean("favorited");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to update tweet.favorited",  throwable);
                        }
                    });
                   Log.i("TweetsAdapter: ", "onClick: " + ivLike);
                }
            });

            ivReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ComposeActivity.class);
                    intent.putExtra("reply", "@" + tweet.user.name + ": ");
                    intent.putExtra("tweetId", tweet.id);
                    context.startActivity(intent);
                }
            });
        }
    }
    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        String twitterTimeStamp = "";
        try {
            // Get time passed in through "createdAt"
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE).toString();

            // Ex: "12 min. ago"
            // Find index of first space in "12 min. ago"
            int spaceIndex = relativeDate.indexOf(" ");

            // Get substring of amount of min/sec/hrs, etc.
            if (spaceIndex != -1) {
                twitterTimeStamp = relativeDate.substring(0, spaceIndex);
            }

            // Get substring for time char
            relativeDate = relativeDate
                    .substring(spaceIndex + 1)
                    .substring(0,1);

            // Concatenate for format "12m"
            twitterTimeStamp = twitterTimeStamp.concat(relativeDate);


        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
        return twitterTimeStamp;
    }
    private void showTweetDialog() {
        TweetDialogFragment tweetDialogFragment = TweetDialogFragment.newInstance("Some Title");
        tweetDialogFragment.show(fragmentManager, "fragment_tweet");
    }

}
