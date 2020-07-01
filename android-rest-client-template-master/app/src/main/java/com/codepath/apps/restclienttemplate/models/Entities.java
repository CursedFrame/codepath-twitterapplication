package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

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
public class Entities {

    @Ignore
    public List<Media> media;

    // Default constructor for Parcel
    public Entities(){

    }

    // Decodes Entities json into Entities model object
    public static Entities fromJson(JSONObject jsonObject) {
        Entities entities = new Entities();
        // Deserialize json into object fields
        try {
            entities.media = Media.fromJson(jsonObject.getJSONArray("media"));
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return entities;
    }
}
