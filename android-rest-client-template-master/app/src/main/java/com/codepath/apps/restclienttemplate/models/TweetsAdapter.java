package com.codepath.apps.restclienttemplate.models;

import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;

import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{
    Context context;
    List<Tweet> tweets;

    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
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
        TextView tvTweetText;
        TextView tvUserName;
        TextView tvTimeStamp;
        TextView tvName;

        // ViewHolder constructor
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            ivTweetImage =  itemView.findViewById(R.id.ivTweetImage);
            tvTweetText = itemView.findViewById(R.id.tvTweetText);
            tvTimeStamp = itemView.findViewById(R.id.tvTimeStamp);

        }

        // Function to bind information to view
        @RequiresApi(api = Build.VERSION_CODES.N) // For getting recommended timestamp function to correct version
        public void bind(Tweet tweet){
            // Set text values for TextViews
            tvTweetText.setText(tweet.body);
            tvName.setText(tweet.user.name);
            tvUserName.setText(tweet.user.userName);
            tvTimeStamp.setText(getRelativeTimeAgo(tweet.createdAt));

            // Values for rounded corners transformation
            int radius = 30; // corner radius, higher value = more rounded
            int margin = 10; // crop margin, set to 0 for corners with no crp

            // Set image for ImageView
            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .transform(new RoundedCornersTransformation(radius, margin))
                    .into(ivProfileImage);

            // Set image for ivTweetImage
            if(tweet.entities != null) {
                if (tweet.entities.media != null) {
                    Log.i("TweetsAdapter", "bind: Attaching tweet image");

                    // Input and transform image through Glide
                    Glide.with(context)
                            .load(tweet.entities.media.get(0).mediaUrl)
                            .transform(new RoundedCornersTransformation(radius, margin))
                            .into(ivTweetImage);

                    Log.i("TweetsAdapter", "URL:" + tweet.entities.media.get(0).mediaUrl);

                    // Set View to VISIBLE in the case that View is GONE (below else-if)
                    ivTweetImage.setVisibility(View.VISIBLE);
                }
            }
            // If View is Visible and if mediaUrl doesn't exist, set Visibility to GONE
            else {
                ivTweetImage.setVisibility(View.GONE);
            }
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
}
