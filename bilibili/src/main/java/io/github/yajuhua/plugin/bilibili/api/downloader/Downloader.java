package io.github.yajuhua.plugin.bilibili.api.downloader;

import java.io.IOException;

/**
 * http请求获取数据的下载器
 */
public abstract class Downloader {
    /**
     * 执行http下载请求
     * @param request
     * @return
     * @throws IOException
     */
    public abstract Response execute(Request request)throws Exception;
}
