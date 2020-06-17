package me.nakeeb.notes.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Patterns;

public class Utils {

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    // A placeholder username validation check
    public static boolean isUserNameValid(String username) {
        if (username == null || username.isEmpty() || !username.contains("@")) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    public static boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}
