package com.projectgoth.fusion.data;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONSerializable<T> {
   JSONObject toJSONObject() throws JSONException;

   T fromJSONObject(JSONObject var1) throws JSONException;
}
