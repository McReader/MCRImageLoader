package by.mcreader.imageloader.utils;

import android.os.Build;

public class AndroidVersions {

    private AndroidVersions() {
    }

    public static boolean hasFroyo() {
        return has(Build.VERSION_CODES.FROYO);
    }

    public static boolean hasGingerbread() {
        return has(Build.VERSION_CODES.GINGERBREAD);
    }

    public static boolean hasHoneycomb() {
        return has(Build.VERSION_CODES.HONEYCOMB);
    }

    public static boolean hasHoneycombMR1() {
        return has(Build.VERSION_CODES.HONEYCOMB_MR1);
    }

    public static boolean hasIceCreamSandwich() {
        return has(Build.VERSION_CODES.ICE_CREAM_SANDWICH);
    }

    public static boolean hasIceCreamSandwichMR1() {
        return has(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1);
    }

    public static boolean hasJellyBean() {
        return has(Build.VERSION_CODES.JELLY_BEAN);
    }

    public static boolean hasKitKat() {
        return has(Build.VERSION_CODES.KITKAT);
    }

    private static boolean has(int version) {
        return Build.VERSION.SDK_INT >= version;
    }
}
