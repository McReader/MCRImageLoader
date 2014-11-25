package by.mcreader.imageloader.request;

/**
 * Created by Dzianis_Roi on 20.11.2014.
 */
public enum KEYS {
    src("src"), err("err"), sync("sync"), size("size"), plhdr("plhdr"), memCache("memCache"), ldr("ldr"), fileCache("fileCache");

    private String key;

    public String getKey() {
        return key;
    }

    KEYS(String s) {
        this.key = new StringBuilder(s).append("_extr").toString();
    }
}