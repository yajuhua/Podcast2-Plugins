package io.github.yajuhua.plugin.bilibili.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.yajuhua.plugin.bilibili.api.downloader.Downloader;
import io.github.yajuhua.plugin.bilibili.api.downloader.Request;
import io.github.yajuhua.plugin.bilibili.api.downloader.Response;
import io.github.yajuhua.plugin.bilibili.api.dto.FavContentDTO;
import io.github.yajuhua.plugin.bilibili.api.dto.FavInfoDTO;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 收藏夹
 * https://www.bilibili.com/medialist/detail/ml1208491396
 * https://api.bilibili.com/x/v3/fav/folder/info?media_id=1208491396
 */
public class Favorites {

    private static final String favInfoApi = "https://api.bilibili.com/x/v3/fav/folder/info";
    private static final String favContent = "https://api.bilibili.com/x/v3/fav/resource/list";
    private static Gson gson = new Gson();

    /**
     * 获取收藏夹信息
     * @param mediaId
     */
    public static FavInfoDTO getInfoByMediaId(String mediaId) throws Exception {
        String url = favInfoApi + "?media_id=" + mediaId;
        JsonObject object = get(url);
        return gson.fromJson(object, FavInfoDTO.class);
    }

    /**
     * 获取收藏夹信息
     * @param url
     * @return
     * @throws Exception
     */
    public static FavInfoDTO getInfoByUrl(String url) throws Exception{
        String mediaId = getMediaId(url);
        return getInfoByMediaId(mediaId);
    }

    /**
     * 获取media_id
     * 链接：https://www.bilibili.com/medialist/detail/ml1208491396
     * 要去掉前面的ml
     * @param url
     * @return
     */
    public static String getMediaId(String url) throws Exception {
        String path = new URL(url).getPath();
        if (path != null && !path.isEmpty() && path.startsWith("/medialist/detail/")){
            String mediaId = path
                    .replace("/medialist/detail/", "")
                    .replace("ml", "");
            return mediaId;
        }
       throw new Exception("链接不合法: " + url);
    }

    /**
     * 获取收藏夹内容
     * @param mediaId
     */
    public static FavContentDTO getContent(String mediaId) throws Exception {
        String url = favContent + "?media_id=" + mediaId + "&ps=20";
        JsonObject object = get(url);
        return gson.fromJson(object, FavContentDTO.class);
    }

    /**
     * 方式get请求
     * @param url
     * @return
     * @throws Exception
     */
    private static JsonObject get(String url) throws Exception {
        Downloader downloader = Init.getDownloader();
        Map<String, List<String>> header = new HashMap();
        header.put("platform", Arrays.asList("web"));
        Request request = Request.builder()
                .httpMethod("GET")
                .url(url)
                .header(header)
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
     * 判断该链接是否是收藏夹
     * @param url https://www.bilibili.com/medialist/detail/ml1208491396
     * @return
     */
    public static boolean isVaildUrl(String url){
        String regex = "https\\:\\/\\/www\\.bilibili\\.com\\/medialist\\/detail\\/ml\\d{1,}.*";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url).find();
    }
}
