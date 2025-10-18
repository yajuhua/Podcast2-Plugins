package io.github.yajuhua.plugin.bilibili.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.yajuhua.plugin.bilibili.pojo.DanmakuInfo;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.*;
import java.util.zip.Inflater;

/**
 * 弹幕处理
 */
public class Danmaku {
    /**
     * 获取视频cid
     * @param bvid
     * @return
     */
    public static String getCidByBvid(String bvid){
        String apiUrl = "https://api.bilibili.com/x/player/pagelist?bvid=" + bvid;
        String json = Http.get(apiUrl, null);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        int code = jsonObject.get("code").getAsInt();
        if (code != 0){
            throw new RuntimeException("无法获取cid: " + jsonObject.get("message").getAsString());
        }
        JsonArray dataList = jsonObject.get("data").getAsJsonArray();
        String cid = dataList.get(0).getAsJsonObject().get("cid").getAsString();
        return cid;
    }

    /**
     * 通过视频链接获取cid
     * @param videoLink
     * @return
     */
    public static String getCidByVideoLink(String videoLink) throws Exception{
        String bvid = new URL(videoLink).getPath().split("/")[2];

        if (bvid == null){
            throw new RuntimeException("无法获取bvid: " + videoLink);
        }
        return getCidByBvid(bvid);
    }

    /**
     * 获取URL请求参数
     * @param url
     * @return
     * @throws Exception
     */
    public static Map<String,Object> getUrlParams(String url)throws Exception{
        String query = new URL(url).getQuery();
        String[] paramsArray = query.split("&");
        Map<String,Object> paramMap = new HashMap<String,Object>(){};
        for (String p : paramsArray) {
            String[] split = p.split("=");
            if (split.length == 2){
                paramMap.put(split[0],split[1]);
            }
            if (split.length == 1){
                paramMap.put(split[0],null);
            }
        }
        return paramMap;
    }

    /**
     * 获取XML弹幕链接
     * API https://comment.bilibili.com/{cid}.xml
     * @param videoLink
     * @return
     */
    public static String getDanmakuXmlUrl(String videoLink) throws Exception{
        String cid = getCidByVideoLink(videoLink);
        return  "https://comment.bilibili.com/" + cid + ".xml";
    }

    /**
     * 获取XML弹幕内容
     * @param videoLink
     * @return
     * @throws Exception
     */
    public static String getDanmakuXmlContent(String videoLink) throws Exception{
        String danmakuXmlUrl = getDanmakuXmlUrl(videoLink);
        return new String(decompress(Http.getToBytes(danmakuXmlUrl,null)));
    }

    /**
     * 获取弹幕并封装
     * @param videoLink
     * @return
     */
    public static List<DanmakuInfo> getDanmakuInfoList(String videoLink)throws Exception{
        List<DanmakuInfo> danmakuInfoList = new ArrayList<>();
        String xmlContent = getDanmakuXmlContent(videoLink);
        Document document = DocumentHelper.parseText(xmlContent);
        List<Element> d = document.getRootElement().elements("d");
        for (Element element : d) {
            String p = element.attributeValue("p");
            String[] split = p.split(",");
            DanmakuInfo danmakuInfo = DanmakuInfo.builder()
                    .availabilityTime(Float.parseFloat(split[0]))
                    .danmakuType(Integer.parseInt(split[1]))
                    .fontSize(Integer.parseInt(split[2]))
                    .color(split[3])
                    .sendTime(Long.parseLong(split[4]))
                    .danmakuPoolType(Integer.parseInt(split[5]))
                    .senderMidHash(split[6])
                    .danmakuDmid(split[7])
                    .shieldingLevel(Integer.parseInt(split[8]))
                    .content(element.getText())
                    .build();
            danmakuInfoList.add(danmakuInfo);
        }
        return danmakuInfoList;
    }

    /**
     * 将xml弹幕转换成ass
     * @return
     */
    public static String danmakuXmlToAss(String videoLink) throws Exception{
        String cid = getCidByVideoLink(videoLink);
        List<DanmakuInfo> danmakuInfoList = getDanmakuInfoList(videoLink);
        String build = Ass.build(cid, 1280, 720, 10, 46, 36, danmakuInfoList,
                1.2f,0.7f,2f);
        return build;
    }
    /**
     * 将xml弹幕转换成ass
     * @return
     */
    public static String danmakuXmlToAss(String videoLink,Integer width, Integer height, Integer duration,
                                         Integer rowHeightPx, Integer fontSize, Float danmakuWidthMultiplier,
                                         Float displayArea,Float shadow) throws Exception{
        String cid = getCidByVideoLink(videoLink);
        List<DanmakuInfo> danmakuInfoList = getDanmakuInfoList(videoLink);
        String build = Ass.build(cid, width, height, duration, rowHeightPx, fontSize, danmakuInfoList,
                danmakuWidthMultiplier,displayArea,shadow);
        return build;
    }

    /**
     * 解压缩数据
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] decompress(byte[] data) throws Exception {
        byte[] decompressData = null;
        Inflater decompressor = new Inflater(true);
        decompressor.reset();
        decompressor.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int i = decompressor.inflate(buf);
                outputStream.write(buf, 0, i);
            }
            decompressData = outputStream.toByteArray();
        } catch (Exception e) {
        } finally {
            outputStream.close();
        }
        decompressor.end();
        return decompressData;
    }
}
