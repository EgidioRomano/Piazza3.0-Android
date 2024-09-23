package com.egix.piazzatrepuntozero;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null
                && remoteMessage.getData().get("text") != null
                && !Objects.equals(remoteMessage.getData().get("userId"), AuthenticationService.getUserId())
        ) {
            NotificationHelper.showNotification(
                    getApplicationContext(),
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getData().get("text"),
                    remoteMessage.getData().get("url"));
        }
    }

    public static void subscribeToTopics(String groupId) {
        FirebaseMessaging.getInstance().subscribeToTopic(groupId);
        FirebaseMessaging.getInstance().subscribeToTopic("piazzatrepuntozero");
    }

    public static void unsubscribeFromTopics(String groupId) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(groupId);
        FirebaseMessaging.getInstance().unsubscribeFromTopic("piazzatrepuntozero");
    }
}