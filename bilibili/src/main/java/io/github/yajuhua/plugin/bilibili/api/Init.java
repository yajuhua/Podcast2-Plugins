package io.github.yajuhua.plugin.bilibili.api;

import io.github.yajuhua.plugin.bilibili.api.downloader.DefaultDownloader;
import io.github.yajuhua.plugin.bilibili.api.downloader.Downloader;

public class Init {
    public static String cookie;
    public static Downloader downloader;

    public static Downloader getDownloader(){
        if (downloader == null){
            return new DefaultDownloader();
        }
        return downloader;
    }
}
