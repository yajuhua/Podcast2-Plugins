package io.github.yajuhua.plugin.bilibili.api;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.yajuhua.plugin.bilibili.Utils.Http;
import io.github.yajuhua.plugin.bilibili.api.downloader.Downloader;
import io.github.yajuhua.plugin.bilibili.api.downloader.Request;
import io.github.yajuhua.plugin.bilibili.api.downloader.Response;
import io.github.yajuhua.plugin.bilibili.api.dto.SpaceVideosDTO;
import io.github.yajuhua.plugin.bilibili.api.dto.UpCardDTO;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 *
 */
public class Space {
    private static final String videosApi = "https://app.bilibili.com/x/v2/space/archive/cursor";
    private static Gson gson = new Gson();

    /**
     * 获取space 视频列表
     * @param mid
     * @return
     */
    public static SpaceVideosDTO getVideos(String mid) throws Exception {
        String url = videosApi + "?vmid=" + mid + "&ps=30";
        JsonObject object = get(url);
        return gson.fromJson(object,SpaceVideosDTO.class);
    }

    /**
     * 获取space 视频列表
     * @param url
     * @return
     */
    public static SpaceVideosDTO getVideosByUrl(String url) throws Exception {
        String mid = getMid(url);
        String url1 = videosApi + "?vmid=" + mid + "&ps=30";
        JsonObject object = get(url1);
        return gson.fromJson(object,SpaceVideosDTO.class);
    }

    /**
     * 获取哔哩哔哩UP主的信息
     * https://api.bilibili.com/x/web-interface/card?mid=3493116233386366
     * @return
     */
    public static UpCardDTO getUpInfo(String mid) throws Exception {
        LinkedHashMap params = new LinkedHashMap();
        params.put("mid",mid);
        String url = Http.buildParams("https://api.bilibili.com/x/web-interface/card", params);
        JsonObject object = get(url);
        return gson.fromJson(object, UpCardDTO.class);
    }

    /**
     * 通过链接获取space信息
     * @param url https://space.bilibili.com/483162496?spm_id_from=333.337.search-card.all.click
     * @return
     * @throws Exception
     */
    public static UpCardDTO getUpInfoByUrl(String url) throws Exception {
        return getUpInfo(getMid(url));
    }

    /**
     * 获取space mid
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public static String getMid(String url) throws MalformedURLException {
        String path = new URL(url).getPath();
        return path.replace("/","");
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
        try {
            return gson.fromJson(json, JsonObject.class);
        } catch (JsonSyntaxException e) {
            throw new Exception(json);
        }
    }

    /**
     * 判断该链接是否是space链接
     * @param url https://space.bilibili.com/483162496?spm_id_from=333.337.search-card.all.click
     * @return
     */
    public static boolean isVaildUrl(String url){
        String regex = "https\\:\\/\\/space\\.bilibili\\.com\\/\\d{1,}.*";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url).find();
    }
}
