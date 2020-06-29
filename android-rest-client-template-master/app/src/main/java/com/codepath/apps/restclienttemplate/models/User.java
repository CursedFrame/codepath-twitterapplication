package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

    public String name; // Name of user
    public String userName; // User handle
    public String profileImageUrl; // Profile image of user

    public static User fromJson(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.name = jsonObject.getString("name");
        user.userName = jsonObject.getString("screen_name");
        user.profileImageUrl= jsonObject.getString("profile_image_url_https");
        return user;
    }
}
