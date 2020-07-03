package com.codepath.apps.restclienttemplate.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApplication;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;

import okhttp3.Headers;
// ...

public class TweetFragment extends DialogFragment {

    public static final int MAX_TWEET_LENGTH = 280;
    public static final String TAG = "TweetFragment";

    public interface TweetFragmentListener {
        void onFinishedTweet(Tweet tweet);
    }

    private TweetFragmentListener listener;

    EditText etComposeTweet;
    Button btnTweet;
    TwitterClient client;
    String replyHandle = "";
    long tweetReplyId;

    // Required default constructor
    public TweetFragment(){

    }

    // Instance constructors
    public static TweetFragment newInstance(String title) {
        TweetFragment frag = new TweetFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    public static TweetFragment newInstance(String title, String replyHandle, long tweetReplyId) {
        TweetFragment frag = new TweetFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.setReplyHandle(replyHandle);
        frag.setTweetReplyId(tweetReplyId);
        Log.i("TweetFragment", replyHandle);
        return frag;
    }

    // Set value functions
    public void setReplyHandle(String replyHandle){
        this.replyHandle = replyHandle;
    }

    public void setTweetReplyId(long tweetReplyId){
        this.tweetReplyId = tweetReplyId;
    }

    // For implementing TweetFragmentListener interface above
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TweetFragmentListener) {
            listener = (TweetFragmentListener) context;
        }
        else {
            throw new ClassCastException(context.toString() + " must implement TweetFragment.TweetFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose_tweet, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        client = TwitterApplication.getRestClient(getContext());
        etComposeTweet = getView().findViewById(R.id.etComposeTweet);
        etComposeTweet.setText(replyHandle);
        btnTweet = getView().findViewById(R.id.btnTweet);
        etComposeTweet.requestFocus();

        // Set click listener on button
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etComposeTweet.getText().toString();

                if (tweetContent.isEmpty()) {
                    Toast.makeText(getActivity(), "Sorry, your tweet cannot be empty.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(getActivity(), "Sorry, your tweet is too long.", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getActivity(), tweetContent, Toast.LENGTH_LONG).show();

                // Make an API call to Twitter to publish the tweet
                if (tweetReplyId != 0) {
                    client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to publish tweet");
                            try {
                                Tweet tweet = Tweet.fromJson(json.jsonObject);
                                Log.i(TAG, "Published tweet says: " + tweet.body);

                                if (listener != null) {
                                    listener.onFinishedTweet(tweet);
                                }

                                dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to publish tweet", throwable);
                        }
                    });
                }
                else {
                    client.publishReplyTweet(tweetContent, tweetReplyId, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to publish tweet");
                            try {
                                Tweet tweet = Tweet.fromJson(json.jsonObject);
                                Log.i(TAG, "Published tweet says: " + tweet.body);

                                if (listener != null) {
                                    listener.onFinishedTweet(tweet);
                                }

                                dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

                        }
                    });
                }
            }
        });
    }
}
