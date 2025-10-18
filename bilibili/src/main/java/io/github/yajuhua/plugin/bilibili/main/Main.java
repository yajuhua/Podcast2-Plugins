package io.github.yajuhua.plugin.bilibili.main;
import io.github.yajuhua.plugin.bilibili.Utils.Cookie;
import com.google.gson.Gson;
import io.github.yajuhua.plugin.bilibili.Utils.FileUtils;
import io.github.yajuhua.plugin.bilibili.api.Init;
import io.github.yajuhua.plugin.bilibili.api.Video;
import io.github.yajuhua.plugin.bilibili.content.Constant;
import io.github.yajuhua.plugin.bilibili.downloader.DanmakuVideoDownloader;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.commons.utils.CommonUtils;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.plugin.bilibili.downloader.VideoAudioDownloader;
import io.github.yajuhua.plugin.bilibili.wrapper.ChannelInfo;
import io.github.yajuhua.plugin.bilibili.wrapper.Items;
import io.github.yajuhua.podcast2API.*;
import io.github.yajuhua.podcast2API.extension.build.ExtendList;
import io.github.yajuhua.podcast2API.extension.build.Input;
import io.github.yajuhua.podcast2API.extension.build.Select;
import io.github.yajuhua.podcast2API.setting.Setting;
import io.github.yajuhua.podcast2API.utils.SettingUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class Main implements Podcast2 {
    private static Gson gson = new Gson();
    public static Params params;

    public Main(Params params){
        this.params = params;
    }

    public Main(String paramsStr) {
        this.params=gson.fromJson(paramsStr,Params.class);
    }

    @Override
    public List<Item> items() throws Exception {
        //设置cookie
        setCookie();
        String url = params.getUrl();
        if (this.params.getEpisodes() == null || this.params.getEpisodes().isEmpty()){
           this.params.setEpisodes(Arrays.asList(0));
        }
        Collections.sort(this.params.getEpisodes());
        List<Integer> episodes = this.params.getEpisodes();

        //获取节目列表
        List<Item> items = Items.getItems(url, episodes);
        //设置Request
        for (Item item : items) {
            Request request = getRequest(item.getLink());
            item.setRequest(request);
        }
        //倒过来，让最新的最后一个执行
        Collections.reverse(items);
        writeCookieToFile();
        return items;
    }

    @Override
    public Channel channel() throws Exception {
        //设置cookie
        setCookie();
        String url = params.getUrl();

        //排除未设置的
        Map map = SettingUtils.SettingListToMap(params.getSettings());
        if (!map.containsKey("ac_time_value") && !map.containsKey("cookie")){
            throw new Exception("请先设置ac_time_value和cookie");
        }
        if (!map.containsKey("ac_time_value")){
            throw new Exception("请先设置ac_time_value");
        }
        if (!map.containsKey("cookie")){
            throw new Exception("请先设置cookie");
        }
        Init.cookie = map.get("cookie").toString();
        return ChannelInfo.get(url);
    }

    @Override
    public Item latestItem() throws Exception{
        List<Integer> es = new ArrayList<>();
        es.add(0);
        params.setEpisodes(es);
       return items().get(0);
    }

    @Override
    public ExtendList getExtensions() {
        ExtendList extendList = new ExtendList();
        List<Input> inputList = new ArrayList<>();
        List<Select> selectList = new ArrayList<>();

        //视频编码选择
        Select select = new Select();
        select.setName("可选视频编码");
        List<String> options = new ArrayList<>();
        options.add("默认");
        options.add("H.264");
        select.setOptions(options);
        selectList.add(select);

        //发布时间
        Select publicTime = new Select();
        publicTime.setName("视频时间");
        List<String> publicTimeOptions = new ArrayList<>();
        publicTimeOptions.add("原视频");
        publicTimeOptions.add("自动生成(默认)");
        publicTime.setOptions(publicTimeOptions);
        selectList.add(publicTime);

        extendList.setInputList(inputList);
        extendList.setSelectList(selectList);
        return extendList;
    }

    @Override
    public Map getInfo() throws Exception {
        Map map = new HashMap();
        Properties properties = new Properties();
        InputStream in = Main.class.getClassLoader().getResourceAsStream("plugin.properties");
        properties.load(in);
        in.close();
        map.put("支持用户视频列表","https://space.bilibili.com/63231");
        map.put("支持收藏夹","https://www.bilibili.com/medialist/detail/ml1208491396");
        map.put("支持多p视频","https://www.bilibili.com/video/BV1iV411z7Nj/");
        map.put("支持合集","https://space.bilibili.com/224267770/channel/collectiondetail?sid=1809611");
        map.put("名称","bilibili");
        map.put("注意","要设置cookie和ac_time_value,可在线获取https://b-login.vercel.app");
        map.put("keyInfo","⚠️要设置cookie和ac_time_value");
        map.put("插件版本",properties.get("version"));
        map.put("更新时间",properties.get("update"));
        map.put("uuid",properties.get("uuid"));
        return map;
    }

    @Override
    public List<Setting> settings() throws Exception {
        List<Setting> settings = null;
        try {
            settings = new ArrayList<>();
            if ( params == null || params.getSettings() == null || params.getSettings().size() == 0){
                //初始化用的
                Setting setting1 = Setting.builder()
                        .name("cookie")
                        .content(null)
                        .tip("在浏览器登录哔哩哔哩账号的情况下复制cookie")
                        .build();
                Setting setting2 = Setting.builder()
                        .name("ac_time_value")
                        .content(null)
                        .tip("浏览器中localStorage的 ac_time_value 字段")
                        .build();
                settings.add(setting1);
                settings.add(setting2);
                log.info("初始哔哩哔哩设置");
            }else {
                //将设置转换成Map方便获取
                Map settingListToMap = SettingUtils.SettingListToMap(params.getSettings());

                if (settingListToMap.containsKey("ac_time_value") && settingListToMap.containsKey("cookie")){
                    //检查更新cookie
                    String cookie = settingListToMap.get("cookie").toString();
                    String acTimeV = settingListToMap.get("ac_time_value").toString();
                    boolean hasRefresh = Cookie.refresh(cookie);
                    if (hasRefresh){
                        Map map = Cookie.getNewCookieAndRreshToken(cookie, acTimeV);
                        settings.clear();
                        Setting setting1 = Setting.builder()
                                .name("cookie")
                                .content((String) map.get("Set-Cookie"))
                                .tip("在浏览器登录哔哩哔哩账号的情况下复制cookie")
                                .updateTime(System.currentTimeMillis())
                                .build();
                        Setting setting2 = Setting.builder()
                                .name("ac_time_value")
                                .content((String) map.get("refresh_token"))
                                .tip("浏览器中localStorage的 ac_time_value 字段")
                                .updateTime(System.currentTimeMillis())
                                .build();
                        settings.add(setting1);
                        settings.add(setting2);
                        log.info("哔哩哔哩cookie有更新");
                    }else {
                        settings = params.getSettings();
                        log.info("哔哩哔哩cookie暂无更新");
                    }
                }else {
                    settings = params.getSettings();
                    log.error("找不到哔哩哔哩cookie设置");
                }
            }
        } catch (Exception e) {
          log.error("刷新cookie异常: {}",e.getMessage());
          e.printStackTrace();
          settings = params.getSettings();
        }
        return settings;
    }

    @Override
    public Request getRequest(String link) throws Exception {
        //设置cookie
        setCookie();
        //获取扩展选项
        Map inputAndSelectDataToMap = io.github.yajuhua.podcast2API
                .utils.CommonUtils.InputAndSelectDataToMap(params.getInputAndSelectDataList());
        String vcode = (String) inputAndSelectDataToMap.getOrDefault("可选视频编码",null);
        String danmaku = (String) inputAndSelectDataToMap.getOrDefault("可选弹幕",null);

        //封装request
        Request request = new Request();
        request.setOperation(Operation.Single);
        request.setType(Type.valueOf(params.getType().name()));
        request.setDownloader(DownloadManager.Downloader.YtDlp);
        Map args = new HashMap();
        args.put("--cookies", Constant.COOKIE_SAVE_PATH);
        if (params.getType().name().equals("Video") && vcode == null){
            args.put("--format","bestvideo+bestaudio");
        }else if (vcode != null && "H.264".equalsIgnoreCase(vcode) && params.getType().name().equals("Video")){
            //-f "(bv*[vcodec~='^(avc|h264)']+ba) / (bv*+ba)"
            args.put("--format","(bv*[vcodec~='^(avc|h264)']+ba) / (bv*+ba)");
        }

        //弹幕嵌入
        //podcast2版本必须大于2.3.2
        if (params.getType().name().equalsIgnoreCase("Video")
                && danmaku != null
                && "是".equals(danmaku)
                && isSupportDanmaku()){
            request.setDownloader(DownloadManager.Downloader.Customize);
//            request.setCustomizeDownloader(new DanmakuVideoDownloader());
            request.setCustomizeDownloader(new VideoAudioDownloader());
        }else if (params.getPodcast2Version() != null){
            //使用VideoAudioDownloader下载器
            request.setDownloader(DownloadManager.Downloader.Customize);
            request.setCustomizeDownloader(new VideoAudioDownloader());
        }
        request.setArgs(args);
        request.setLinks(Arrays.asList(link));
        writeCookieToFile();
        return request;
    }

    @Override
    public Item getItem(String link) throws Exception {
        //设置cookie
        setCookie();
        //获取单个节目信息
        Item item = Items.getItem(Video.getBvid(link));
        //是否使用原视频发布时间,多p视频不支持
        Map isdMap = io.github.yajuhua.podcast2API.utils
                .CommonUtils.InputAndSelectDataToMap(params.getInputAndSelectDataList());
        if (isdMap.containsKey("视频时间")
                && isdMap.get("视频时间") != null
                && isdMap.get("视频时间").equals("原视频")){
            item.setCreateTime(item.getPublicTime());
        }
        item.setRequest(getRequest(link));
        return item;
    }

    /**
     * podcast2版本大于2.3.2才支持弹幕
     * @return
     */
    public boolean isSupportDanmaku(){
        try {
            if (params.getPodcast2Version() == null){
                return false;
            }
            if (params.getPodcast2Version().startsWith("beta")){
                return true;
            }
            return CommonUtils.compareVersion(params.getPodcast2Version().substring(1),"2.3.2") == 1;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 写cookie到文件中供yt-dlp调用
     */
    private void writeCookieToFile(){
        if (params != null && params.getSettings() != null && !params.getSettings().isEmpty()){
            Map map = SettingUtils.SettingListToMap(params.getSettings());
            if (map.containsKey("cookie")){
                try {
                    String cookie = map.get("cookie").toString();
                    if (cookie != null && !cookie.isEmpty()){
                        FileUtils.writeLines(new File(Constant.COOKIE_SAVE_PATH), Cookie.createCookieToNetscape(cookie));
                    }
                } catch (IOException e) {
                    log.error("写入cookie失败",e);
                }
            }
        }
    }

    /**
     * 设置cookie
     * @throws Exception
     */
    private void setCookie() throws Exception {
        Map map = SettingUtils.SettingListToMap(params.getSettings());
        if (!map.containsKey("cookie")){
            throw new Exception("请先设置cookie");
        }
        Init.cookie = map.get("cookie").toString();
    }

    /**
     * podcast2 2.5.0才支持publicTime字段
     * @return
     */
    public static boolean supportPublicTime(){
        try {
            String podcast2Version = params.getPodcast2Version();

            if (podcast2Version != null){
                //支持beta版
                if (podcast2Version.startsWith("beta")){
                    return true;
                }
                int compareVersion = compareVersion(podcast2Version.substring(1), "2.5.0");
                if (compareVersion == 1 || compareVersion == 0){
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 版本比较
     * @param v1
     * @param v2
     * @return 如果v1 > v2 返回 1;v1<v2 返回 -1;相同返回0
     */
    public static int compareVersion(String v1,String v2){
        String[] v1Sp = v1.split("\\.");
        String[] v2Sp = v2.split("\\.");
        if (v1Sp.length != v2Sp.length){
            throw new RuntimeException("版本号格式不合法,格式必须为x.x.x");
        }
        String[] parts1 = v1Sp;
        String[] parts2 = v2Sp;

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int part1 = (i < parts1.length) ? Integer.parseInt(parts1[i]) : 0;
            int part2 = (i < parts2.length) ? Integer.parseInt(parts2[i]) : 0;

            if (part1 < part2) {
                return -1;
            } else if (part1 > part2) {
                return 1;
            }
        }

        return 0; // 版本号相同
    }

}
