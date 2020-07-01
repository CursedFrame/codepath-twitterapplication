package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
@Entity
public class Media {

    @Ignore
    public String mediaUrl;

    public Media(){

    }

    public static Media fromJson(JSONObject jsonObject) {
        Media media = new Media();
        // Deserialize json into object fields
        try {
            media.mediaUrl = jsonObject.getString("media_url_https");
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return media;
    }

    // Decodes array of Media json results into Media model objects
    public static ArrayList<Media> fromJson(JSONArray jsonArray) {
        JSONObject jsonObject;
        ArrayList<Media> medias = new ArrayList<>(jsonArray.length());

        // Process each result in json array, decode and convert to Media object
        for (int i = 0 ; i < jsonArray.length() ; i++) {
            try {
                jsonObject = jsonArray.getJSONObject(i);
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Media media = Media.fromJson(jsonObject);
            if (media != null) {
                medias.add(media);
            }
        }

        return medias;
    }
}
