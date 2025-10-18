package io.github.yajuhua.youtube.main;

import com.google.gson.Gson;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2API.Channel;
import io.github.yajuhua.podcast2API.Item;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.Podcast2;
import io.github.yajuhua.podcast2API.extension.build.ExtendList;
import io.github.yajuhua.podcast2API.extension.build.Input;
import io.github.yajuhua.podcast2API.extension.build.Select;
import io.github.yajuhua.podcast2API.setting.Setting;
import io.github.yajuhua.podcast2API.utils.SettingUtils;
import io.github.yajuhua.youtube.downloader.VideoDownloadConfig;
import io.github.yajuhua.youtube.downloader.VideoDownloader;
import io.github.yajuhua.youtube.dto.ChannelDTO;
import io.github.yajuhua.youtube.dto.ItemDTO;
import io.github.yajuhua.youtube.utils.CmdLineUtil;
import io.github.yajuhua.youtube.utils.NetworkUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.Proxy;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Main implements Podcast2 {

    private static Params params;
    private String proxyUrl;
    private Proxy proxy;
    private Gson gson = new Gson();

    public Main() {
    }

    public Main(String paramsStr) {
        this.params = gson.fromJson(paramsStr, Params.class);
    }

    public Main(Params params) {
        this.params = params;
        this.proxy = NetworkUtil.getProxy(params);
        this.proxyUrl = NetworkUtil.getProxyUrl(params);
    }

    /**
     * 获取频道信息
     * @return Channel
     * @throws Exception
     */
    @Override
    public Channel channel() throws Exception {
        //1.校验链接
        String regex = "https://www\\.youtube\\.com/@[\\w-]+/(videos|streams)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(params.getUrl());
        if (!matcher.matches()){
            throw new Exception("不支持该链接: " + params.getUrl());
        }

        //2.获取json数据
        Map options = new HashMap();
        options.put("-J",null);
        options.put("--playlist-items","0");
        if (proxyUrl != null){
            options.put("--proxy", proxyUrl);
        }
        String jsonStr = CmdLineUtil.exec("yt-dlp", options, params.getUrl());
        ChannelDTO channelDTO = gson.fromJson(jsonStr, ChannelDTO.class);

        //封装信息
        return Channel.builder()
                .title(channelDTO.getTitle())
                .status(1)
                .image(channelDTO.getThumbnails().get(channelDTO.getThumbnails().size() - 1).getUrl())
                .link(channelDTO.getChannelUrl())
                .description(channelDTO.getDescription())
                .author(channelDTO.getUploader())
                .category(null)
                .build();
    }

    @Override
    public Request getRequest(String link) {
        Map<String, String> args = VideoDownloadConfig.configureDownloadOptions(params, proxy);
        return Request.builder()
                .args(args)
                .links(Arrays.asList(link))
                .type(Type.valueOf(params.getType().name()))
                .downloader(DownloadManager.Downloader.Customize)
                .operation(Operation.Single)
                .customizeDownloader(new VideoDownloader())
                .build();
    }

    @Override
    public Item getItem(String link) throws Exception {
        //1.获取节目json数据
        Map options = new HashMap();
        if (proxyUrl != null){
            options.put("--proxy", proxyUrl);
        }
        options.put("-J", null);
        String jsonStr = CmdLineUtil.exec("yt-dlp", options, link);
        ItemDTO itemDTO = gson.fromJson(jsonStr, ItemDTO.class);

        //2.封装信息
        Item item = Item.builder()
                .title(itemDTO.getTitle())
                .image(itemDTO.getThumbnail())
                .description(itemDTO.getDescription())
                .duration(itemDTO.getDuration())
                .createTime(itemDTO.getTimestamp() * 1000L)//需要传入时间毫秒值
                .enclosure(link)
                .equal(itemDTO.getId())
                .link(link)
                .request(getRequest(link))
                .build();
        if (supportPublicTime()){
            item.setPublicTime(itemDTO.getTimestamp() * 1000L);
        }
        return item;
    }

    @Override
    public List<Item> items() throws Exception {
        //匹配链接
        Matcher matcherVideos = Pattern.compile("https://www\\.youtube\\.com/@[\\w-]+/videos").matcher(params.getUrl());
        Matcher matcherStreams = Pattern.compile("https://www\\.youtube\\.com/@[\\w-]+/streams").matcher(params.getUrl());
        List<Item> items = new ArrayList<>();

        //构建参数
        Map options = new HashMap();
        options.put("-j", null);
        if (proxyUrl != null){
            options.put("--proxy", proxyUrl);
        }
        if (matcherVideos.matches()){
            //yt-dlp --playlist-items 1 -j https://www.youtube.com/@MuseAsia/videos
            options.put("--playlist-items", "1");
        }else if (matcherStreams.matches()){
            //yt-dlp --match-filters !is_live --no-playlist  --max-downloads 1 -j "https://www.youtube.com/@MuseAsia/streams"
            options.put("--match-filters", "!is_live");
            options.put("--no-playlist", null);
            options.put("--max-downloads", "1");
        }else {
            throw new Exception("不支持该链接: " + params.getUrl());
        }

        //获取json数据
        String jsonStr = CmdLineUtil.exec("yt-dlp", options, params.getUrl());
        ItemDTO itemDTO = gson.fromJson(jsonStr, ItemDTO.class);

        //封装
        items.add(getItem(itemDTO.getOriginalUrl()));
        return items;
    }

    @Override
    public Item latestItem() throws Exception{
        return items().get(0);
    }

    @Override
    public Map getInfo() throws Exception{
        //读取插件属性文件plugin.properties
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("plugin.properties");
        Properties properties = new Properties();
        properties.load(is);

        //封装信息
        Map map = new HashMap();
        map.put("版本",properties.getProperty("version"));
        map.put("名称",properties.getProperty("name"));
        map.put("更新时间",properties.getProperty("update"));
        map.put("uuid",properties.getProperty("uuid"));
        map.put("domainNames",properties.getProperty("name") + ",youtube.com");
        map.put("keyInfo","需要没有被ban的IP");
        map.put("支持videos","https://www.youtube.com/@MuseAsia/videos");
        map.put("支持streams","https://www.youtube.com/@MuseAsia/streams");

        return map;
    }

    @Override
    public List<Setting> settings() throws Exception{
        Map map = SettingUtils.SettingListToMap(params.getSettings());
        List<Setting> settings = new ArrayList<>();

        Setting httpProxySetting = Setting.builder()
                .name("http代理")
                .tip("例如：192.168.123.72:20171")
                .updateTime(System.currentTimeMillis())
                .content((String) map.get("http代理"))
                .build();
        settings.add(httpProxySetting);

        Setting socksProxySetting = Setting.builder()
                .name("socks代理")
                .tip("例如：192.168.123.72:20170")
                .updateTime(System.currentTimeMillis())
                .content((String) map.get("socks代理"))
                .build();
        settings.add(socksProxySetting);
        return settings;
    }

    @Override
    public ExtendList getExtensions() throws Exception{
        ExtendList extendList = new ExtendList();
        List<Select> selectList = new ArrayList<>();

        //分辨率
        Select selectResolution = new Select();
        selectResolution.setName("分辨率");
        selectResolution.setOptions(Arrays.asList("最差","最佳"));
        selectList.add(selectResolution);

        //字幕
        Select subtitleSelect = new Select();
        subtitleSelect.setOptions(Arrays.asList("是","否"));
        subtitleSelect.setName("字幕");
        selectList.add(subtitleSelect);

        //视频编码
        Select vcodecSelect = new Select();
        vcodecSelect.setName("视频编码");
        vcodecSelect.setOptions(Arrays.asList("默认","vp9","avc"));
        selectList.add(vcodecSelect);

        //自定义-f,其他选项将失效
        Input formatInput = new Input("自定义-f");

        //自定义--merge-output-format
        Input mergeOutputFormat = new Input("自定义扩展名");

        extendList.setInputList(Arrays.asList(formatInput, mergeOutputFormat));
        extendList.setSelectList(selectList);

        return extendList;
    }

    /**
     * podcast2 2.5.0才支持publicTime字段
     * @return
     */
    public static boolean supportPublicTime(){
        try {
            String podcast2Version = params.getPodcast2Version();
            if (podcast2Version != null){
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