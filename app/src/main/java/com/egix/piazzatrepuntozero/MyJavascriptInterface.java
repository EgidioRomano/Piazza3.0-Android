package com.egix.piazzatrepuntozero;

import android.content.Context;
import android.webkit.JavascriptInterface;

public class MyJavascriptInterface {

    Context mContext;

    MyJavascriptInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void onLogin(String groupId) {
        AuthenticationService.saveGroupId(groupId);
        MyFirebaseMessagingService.subscribeToTopics(groupId);
    }

    @JavascriptInterface
    public void onLogout() {
        String groupId = AuthenticationService.getGroupId();
        MyFirebaseMessagingService.unsubscribeFromTopics(groupId);
        AuthenticationService.saveGroupId(null);
    }
}