package io.connection.bluetooth.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by songline on 27/08/16.
 */
public class ChatDataConversation {

    public static Map<String, String> nameUser = new HashMap<>();

    public static void putUserName(String deviceAddresskey, String Name) {
        nameUser.put(deviceAddresskey, Name);

    }

    public static String getUserName(String deviceAddressKey) {
        return nameUser.get(deviceAddressKey);
    }

    public static Map<String, List<String>> chatData = new HashMap<>();

    public static void putChatConversation(String key, String chat) {
        if (chatData.containsKey(key)) {
            List<String> stringList = chatData.get(key);
            stringList.add(chat);
            chatData.put(key, stringList);
        } else {
            List<String> stringList = new ArrayList<>();
            stringList.add(chat);
            chatData.put(key, stringList);
        }
    }

    public static List<String> getChatConversation(String Key) {
        return chatData.get(Key);
    }

    public static void removeLastMessage(String deviceAddress) {
        List<String> messageList = chatData.get(deviceAddress);
        if (messageList != null || !messageList.isEmpty()) {
            messageList.remove(messageList.size() - 1);
            chatData.put(deviceAddress, messageList);
        }

    }

}
