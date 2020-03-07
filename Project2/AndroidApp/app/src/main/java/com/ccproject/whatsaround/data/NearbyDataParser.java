package com.ccproject.whatsaround.data;

import com.ccproject.whatsaround.data.mode.ImageInfo;
import com.ccproject.whatsaround.http.IParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lei on 4/25/2018.
 */

public class NearbyDataParser implements IParser<List<ImageInfo>> {
    private List<ImageInfo> data;

    @Override
    public List<ImageInfo> parse(String str) {
        try {
            JSONArray jsonArray = new JSONArray(str);
            List<ImageInfo> list = new ArrayList<>();
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ImageInfo info = ImageInfo.fromJSON(jsonObject);
                list.add(info);
            }
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return null;
    }

}
