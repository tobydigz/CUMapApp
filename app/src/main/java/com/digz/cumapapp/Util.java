package com.digz.cumapapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Digz on 17/11/2016.
 */

public class Util {
    public static final class Operations {
        private Operations() throws InstantiationException {
            throw new InstantiationException("This class is not for instantiation");
        }

        /**
         * Checks to see if the device is online before carrying out any operations
         *
         * @return
         */

        public static boolean isOnline(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                return true;
            }
            return false;
        }
    }

    private Util() throws InstantiationException {
        throw  new InstantiationException("This class is not for instantiation");
    }
}
