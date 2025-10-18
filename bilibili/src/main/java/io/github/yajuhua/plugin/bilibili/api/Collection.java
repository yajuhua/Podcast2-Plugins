package io.github.yajuhua.plugin.bilibili.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.yajuhua.plugin.bilibili.Utils.Http;
import io.github.yajuhua.plugin.bilibili.api.downloader.Downloader;
import io.github.yajuhua.plugin.bilibili.api.downloader.Request;
import io.github.yajuhua.plugin.bilibili.api.downloader.Response;
import io.github.yajuhua.plugin.bilibili.api.dto.CollectionDTO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 视频合集
 * 如：https://space.bilibili.com/224267770/channel/collectiondetail?sid=3851794
 * API https://api.bilibili.com/x/polymer/web-space/seasons_archives_list?
 * mid=224267770
 * &season_id=1809611
 * &sort_reverse=true //默认一个是最旧的
 * &page_num=1&page_size=30
 * @return
 */
public class Collection {

    private static Gson gson = new Gson();
    private static final String collectionApi = "https://api.bilibili.com/x/polymer/web-space/seasons_archives_list";

    /**
     * API https://api.bilibili.com/x/polymer/web-space/seasons_archives_list?
     * mid=224267770
     * &season_id=1809611
     * &sort_reverse=true //默认一个是最旧的
     * &page_num=1&page_size=30
     * 链接：https://space.bilibili.com/224267770/channel/collectiondetail?sid=1809611
     * 获取合集信息
     * @param mid
     */
    public static CollectionDTO getInfo(String mid,String sid,boolean sortReverse) throws Exception {
        Map params = new HashMap();
        params.put("mid",mid);
        params.put("season_id",sid);
        params.put("sort_reverse",sortReverse);
        params.put("page_num",1);
        params.put("page_size",30);
        String url = Http.buildParams(collectionApi, params);
        JsonObject object = get(url);
        return gson.fromJson(object, CollectionDTO.class);
    }

    /**
     * API https://api.bilibili.com/x/polymer/web-space/seasons_archives_list?
     * mid=224267770
     * &season_id=1809611
     * &sort_reverse=true //默认一个是最旧的
     * &page_num=1&page_size=30
     * 链接：https://space.bilibili.com/224267770/channel/collectiondetail?sid=1809611
     * 获取合集信息
     * @param url
     */
    public static CollectionDTO getInfo(String url,boolean sortReverse) throws Exception {
        String mid = getMid(url);
        String sid = getSid(url);
        return getInfo(mid,sid,sortReverse);
    }

    private static String getMid(String url) throws Exception {
        Matcher matcher = matcherMidAndSid(url);
        if (matcher.find()){
            return matcher.group("mid");
        }
        throw new Exception("无法获取mid");
    }

    private static String getSid(String url) throws Exception {
        Matcher matcher = matcherMidAndSid(url);
        if (matcher.find()){
            return matcher.group("sid");
        }
        throw new Exception("无法获取sid");
    }

    /**
     * 判断该链接是否是集合链接
     * @param url https://space.bilibili.com/224267770/channel/collectiondetail?sid=1809611
     * @return
     */
    public static boolean isVaildUrl(String url){
        String regex = "https\\:\\/\\/space\\.bilibili\\.com\\/\\d{1,}\\/channel\\/collectiondetail\\?sid\\=\\d{1,}.*";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url).find();
    }

    /**
     * 匹配mid和sid
     * @param url https://space.bilibili.com/224267770/channel/collectiondetail?sid=1809611
     * @return
     */
    private static Matcher matcherMidAndSid(String url){
        String regex = "https\\:\\/\\/space\\.bilibili\\.com\\/(?<mid>\\d{1,})\\/channel\\/collectiondetail" +
                "\\?sid\\=(?<sid>\\d{1,}).*";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url);
    }

    /**
     * 方式get请求
     * @param url
     * @return
     * @throws Exception
     */
    private static JsonObject get(String url) throws Exception {
        Downloader downloader = Init.getDownloader();
        Map<String, List<String>> header = new HashMap<>();
        header.put("referer", Arrays.asList("https://space.bilibili.com"));
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
}
