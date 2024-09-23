package com.egix.piazzatrepuntozero;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static int notificationId = 0;
    private static final String CHANNEL_ID = "Piazza_3_0_Channel";
    private static final String CHANNEL_NAME = "Notifiche Piazza 3.0";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifiche di nuove attività e aggiornamenti");

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(Context context, String title, String message, String url) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("url", url);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(formatBoldTags(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId++, builder.build());
        }
    }

    private static SpannableString formatBoldTags(String text) {
        String plainText = text.replace("[b]", "").replace("[/b]", "");
        SpannableString spannable = new SpannableString(plainText);
        int lastEndIndex = 0;

        while (true) {
            int startIndex = text.indexOf("[b]", lastEndIndex);
            if (startIndex == -1) break;

            int endIndex = text.indexOf("[/b]", startIndex);
            if (endIndex == -1) break;

            int start = plainText.indexOf(text.substring(startIndex + 3, endIndex));
            spannable.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    start,
                    start + (endIndex - (startIndex + 3)),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            lastEndIndex = endIndex + 4;
        }

        return spannable;
    }
}