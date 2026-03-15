package com.projectgoth.fusion.rewardsystem.stateprocessors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map.Entry;

public class StringBagJSONSerde {
   private static final Type stringBagType = (new TypeToken<Bag<String>>() {
   }).getType();
   private static final StringBagJSONSerde.JsonSerDe<Bag<String>> bagSerDe = new StringBagJSONSerde.JsonSerDe<Bag<String>>() {
      public Bag<String> deserialize(JsonElement element, Type t, JsonDeserializationContext ctx) throws JsonParseException {
         Bag<String> bag = new Bag();
         if (!element.isJsonObject()) {
            throw new JsonParseException("Element:[" + element + "] not a jsonObject");
         } else {
            JsonObject jsonObject = element.getAsJsonObject();
            Iterator i$ = jsonObject.entrySet().iterator();

            while(i$.hasNext()) {
               Entry<String, JsonElement> entry = (Entry)i$.next();
               bag.setCount(entry.getKey(), ((JsonElement)entry.getValue()).getAsInt());
            }

            return bag;
         }
      }

      public JsonElement serialize(Bag<String> bag, Type t, JsonSerializationContext ctx) {
         JsonObject jsonObject = new JsonObject();
         Iterator i$ = bag.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, Integer> e = (Entry)i$.next();
            jsonObject.addProperty((String)e.getKey(), (Number)e.getValue());
         }

         return jsonObject;
      }
   };

   public static Bag<String> fromString(String serializedFormat) {
      GsonBuilder gsonBuilder = new GsonBuilder();
      gsonBuilder.registerTypeAdapter(stringBagType, bagSerDe);
      Gson gson = gsonBuilder.create();
      return (Bag)gson.fromJson(serializedFormat, stringBagType);
   }

   public static String toString(Bag<String> items) {
      GsonBuilder gsonBuilder = new GsonBuilder();
      gsonBuilder.registerTypeAdapter(stringBagType, bagSerDe);
      Gson gson = gsonBuilder.create();
      return gson.toJson(items, stringBagType);
   }

   private interface JsonSerDe<T> extends JsonDeserializer<T>, JsonSerializer<T> {
   }
}
