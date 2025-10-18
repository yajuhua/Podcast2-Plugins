package io.github.yajuhua.plugin.bilibili.api;

import com.google.gson.Gson;
import io.github.yajuhua.plugin.bilibili.Utils.Http;
import io.github.yajuhua.plugin.bilibili.api.downloader.Downloader;
import io.github.yajuhua.plugin.bilibili.api.downloader.Request;
import io.github.yajuhua.plugin.bilibili.api.downloader.Response;
import io.github.yajuhua.plugin.bilibili.api.dto.PlayDTO;
import io.github.yajuhua.plugin.bilibili.api.dto.VideoDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取视频流
 * API https://api.bilibili.com/x/player/playurl?avid=969628065&cid=244954665&qn=0&fnval=80&fnver=0&fourk=1
 */
public class Play {

    private static String api = "https://api.bilibili.com/x/player/playurl";
    private static Gson gson = new Gson();

    /**
     * 获取视频流信息
     * @param url
     * @return
     * @throws Exception
     */
    public static PlayDTO getByUrl(String url) throws Exception {

        //判断是否是多P视频
        Integer p = 0;//默认是第一个
        if (url.contains("?")){
            String query = url.split("\\?")[1];
            Map<String, String> queryParams = Http.getQueryParams(query);
            if (queryParams.containsKey("p")){
                p = Integer.parseInt(queryParams.get("p")) - 1;
            }
        }

        //1.获取cid
        VideoDTO videoDTO = Video.getByUrl(url);
        VideoDTO.DataDTO.PagesDTO pagesDTO = videoDTO.getData().getPages().get(p);
        Long cid = pagesDTO.getCid();

        //2.构建请求
        Downloader downloader = Init.getDownloader();
        Map params = new HashMap();
        params.put("cid", cid);
        params.put("bvid",videoDTO.getData().getBvid());
        params.put("qn","0");
        params.put("fnval","80");
        params.put("fnver","0");
        params.put("fourk","1");
        Request request = Request.builder()
                .httpMethod("GET")
                .url(Http.buildParams(api,params))
                .build();
        Response response = downloader.execute(request);
        Integer responseCode = response.getResponseCode();
        if (200 != responseCode){
            throw new Exception(response.getResponseMessage());
        }
        String json = response.getResponseBody();
        return gson.fromJson(json,PlayDTO.class);
    }
}
