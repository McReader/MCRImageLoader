package by.grsu.mcreader.mcrimageloader.imageloader.utils;

import android.annotation.SuppressLint;
import android.content.Context;

@SuppressLint("ValidFragment")
public class NetworkHelper {

    private NetworkHelper() {
    }

    public static boolean checkConnection(Context context) {

        return context.getSystemService(Context.CONNECTIVITY_SERVICE) != null;

    }


}
