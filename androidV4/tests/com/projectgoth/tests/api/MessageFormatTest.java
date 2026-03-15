package com.projectgoth.tests.api;

import android.test.AndroidTestCase;

import com.google.gson.Gson;
import com.projectgoth.model.Message;

/**
 * Created by freddie on 15/6/1.
 */
public class MessageFormatTest extends AndroidTestCase {

    public void testMessageAssertFailed() throws Exception {
        String jsonStr = "{\n" +
                "    \"contactID\": -1,\n" +
                "    \"contentType\": \"EMOTE\",\n" +
                "    \"deliveryStatus\": \"UNKNOWN\",\n" +
                "    \"destination\": \"ashc111\",\n" +
                "    \"destinationType\": \"PRIVATE\",\n" +
                "    \"displayName\": \"ougf8hjm\",\n" +
                "    \"emoteContentType\": \"PLAIN\",\n" +
                "    \"fromAdministrator\": 0,\n" +
                "    \"hasMentions\": false,\n" +
                "    \"hotkeysIdentified\": true,\n" +
                "    \"internalTimestamp\": 0,\n" +
                "    \"isLoadedFromCache\": false,\n" +
                "    \"isLoadingPreviousMessages\": false,\n" +
                "    \"isPrevMessageFound\": false,\n" +
                "    \"isServerInfo\": false,\n" +
                "    \"message\": \"ougf8hjm \uFEFFكس ام لأسد ع ام كل اسدي بأيري\",\n" +
                "    \"messageChatIdentifier\": \"ougf8hjm\",\n" +
                "    \"messageColor\": -1,\n" +
                "    \"messageDirection\": \"INCOMING\",\n" +
                "    \"messageId\": \"61efce7d-91ad-4dbd-b78f-de3d66653a4d\",\n" +
                "    \"messageType\": \"FUSION\",\n" +
                "    \"pinnedType\": \"NONE\",\n" +
                "    \"prevMessageId\": \"3c7057a5-ff99-4b70-98e2-ec6b21797cd2\",\n" +
                "    \"source\": \"ougf8hjm\",\n" +
                "    \"sourceColor\": -1,\n" +
                "    \"timestamp\": 1432161751569\n" +
                "}";
        String jsonStr2 = "{\"contentType\":\"TEXT\",\"deliveryStatus\":\"SENT_TO_SERVER\",\"destination\":\"mhammmmd-2011\",\"destinationType\":\"PRIVATE\",\"displayName\":\"mhammmmd-2011\",\"emoteContentType\":\"PLAIN\",\"message\":\"طيب رح حاول كمان\",\"messageChatIdentifier\":\"mhammmmd-2011\",\"messageDirection\":\"OUTGOING\",\"messageId\":\"c4f63ed7-4802-4a8c-af5d-6c500ebebcaf\",\"messageType\":\"FUSION\",\"pinnedType\":\"NONE\",\"prevMessageId\":\"849681f4-e0be-469d-82d2-02abf9fa3d50\",\"source\":\"king_ojalan\",\"contactID\":0,\"internalTimestamp\":1433106569390,\"timestamp\":1433106569717,\"fromAdministrator\":0,\"hasMentions\":false,\"hotkeysIdentified\":true,\"isLoadedFromCache\":false,\"isLoadingPreviousMessages\":false,\"isPrevMessageFound\":false,\"isServerInfo\":false,\"messageColor\":-1,\"sourceColor\":-1}";

        Message message = new Gson().fromJson(jsonStr, Message.class);
        Message message2 = new Gson().fromJson(jsonStr2, Message.class);
        assertNotNull(message);
        assertNotNull(message2);
    }
}
