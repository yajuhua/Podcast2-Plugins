package io.github.yajuhua.plugin.bilibili.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.yajuhua.plugin.bilibili.api.downloader.Downloader;
import io.github.yajuhua.plugin.bilibili.api.downloader.Request;
import io.github.yajuhua.plugin.bilibili.api.downloader.Response;
import io.github.yajuhua.plugin.bilibili.api.dto.VideoDTO;

import java.net.URL;
import java.util.regex.Pattern;

/**
 * 接口 https://api.bilibili.com/x/web-interface/view?bvid=BV1iV411z7Nj
 * 需要cookie
 */
public class Video {

    private static final String api = "https://api.bilibili.com/x/web-interface/view";
    private static final Gson gson = new Gson();
    /**
     * 获取视频详细信息
     * @param bvid
     * 例子：https://api.bilibili.com/x/web-interface/view?bvid=BV1iV411z7Nj
     * 需要cookie
     * @return
     */
    public static VideoDTO getByBvid(String bvid) throws Exception {
        String url = api + "?bvid=" + bvid;
        JsonObject object = get(url);
        return gson.fromJson(object,VideoDTO.class);
    }

    /**
     * 通过链接进行获取
     * @param url
     * @return
     * @throws Exception
     */
    public static VideoDTO getByUrl(String url) throws Exception{
        String path = new URL(url).getPath();
        if (path != null && !path.isEmpty() && path.startsWith("/video/")){
            String bvid = path.replace("/video/", "").replace("/", "");
            return getByBvid(bvid);
        }
        throw new Exception("链接不合法: " + url);
    }

    /**
     * 通过aid获取视频详细信息
     * @param aid
     * @return
     */
    public static VideoDTO getByAid(String aid) throws Exception {
        String url = api + "?aid=" + aid;
        JsonObject object = get(url);
        return gson.fromJson(object,VideoDTO.class);
    }

    /**
     * 方式get请求
     * @param url
     * @return
     * @throws Exception
     */
    private static JsonObject get(String url) throws Exception {
        Downloader downloader = Init.getDownloader();
        Request request = Request.builder()
                .httpMethod("GET")
                .url(url)
                .build();
        Response response = downloader.execute(request);
        Integer responseCode = response.getResponseCode();
        if (200 != responseCode){
            throw new Exception(response.getResponseMessage());
        }
        String json = response.getResponseBody();
        return gson.fromJson(json, JsonObject.class);
    }

    /**
     * 判断该链接是否是space链接
     * @param url https://www.bilibili.com/video/BV1w84y127rP/?spm_id_from=333.999.0.0
     * @return
     */
    public static boolean isVaildUrl(String url){
        String regex = "https\\:\\/\\/www\\.bilibili\\.com\\/video\\/\\w{3,}\\/.*";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url).find();
    }

    /**
     * 获取链接中的bvid
     * @param url
     * @return
     */
    public static String getBvid(String url) throws Exception {
        String path = new URL(url).getPath();
        if (path != null && !path.isEmpty() && path.startsWith("/video/")){
            String bvid = path.replace("/video/", "").replace("/", "");
            return bvid;
        }
        throw new Exception("链接不合法: " + url);
    }
}
