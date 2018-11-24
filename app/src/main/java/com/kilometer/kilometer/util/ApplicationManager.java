package com.kilometer.kilometer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ApplicationManager {
    private static final String TAG = "ApplicationManager";

    public static boolean hasInternetConnection(Context context) {

        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            Log.e(TAG, "connectivity " + e.toString());
            return false;
        }
    }
}
