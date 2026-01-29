package io.github.yajuhua.youtube.downloader;

import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.Type;
import io.github.yajuhua.podcast2API.utils.CommonUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VideoDownloadConfig {

    /**
     * 获取下载配置
     * @param params
     * @return
     */
    public static Map<String, String> configureDownloadOptions(Params params, Proxy proxy) {
        Map<String, String> args = new HashMap<>();
        Map<String, String> extendData = new HashMap<>();

        // 如果输入和选择数据列表不为空，将其转为 Map
        if (params.getInputAndSelectDataList() != null
                && !params.getInputAndSelectDataList().isEmpty()) {
            extendData = CommonUtils.InputAndSelectDataToMap(params.getInputAndSelectDataList());
        }

        // 如果包含字幕选项，设置字幕相关参数
        if (extendData.containsKey("字幕") && extendData.get("字幕").equals("是")) {
            args.put("--write-subs", null);
            args.put("--embed-subs", null);
            args.put("--sub-langs", "all");
        }

        //自定义-f
        if (extendData.containsKey("自定义-f") && extendData.get("自定义-f") != null
                && !extendData.get("自定义-f").isEmpty()) {
           args.put("-f",extendData.get("自定义-f"));
        }else
        //分辨率和编码选项
        {
            // 获取分辨率和视频编码
            String resolution = extendData.getOrDefault("分辨率",null);
            String vcodec = extendData.getOrDefault("视频编码",null);

            // 处理分辨率和编码的组合逻辑
            if (resolution != null) {
                configureResolutionAndCodec(args, resolution, vcodec);
            }
        }

        //自定义--merge-output-format Or --audio-format
        String fileExtName = null;
       if (extendData.containsKey("自定义扩展名") && extendData.get("自定义扩展名") != null
               && !extendData.get("自定义扩展名").isEmpty()){
           fileExtName = extendData.get("自定义扩展名");
       }
        if (Type.Video.equals(params.getType())){
            if (fileExtName!= null){
                args.put("--merge-output-format", fileExtName);
            }else {
                //视频默认mkv
                args.put("--merge-output-format", "mkv");
            }
        }else if(Type.Audio.equals(params.getType())){
            args.put("--extract-audio", null);
           if (fileExtName != null){
               //音频默认m4a
               args.put("--audio-format", fileExtName);
           }else {
               //音频默认m4a
               args.put("--audio-format", "m4a");
           }
        }

        /*
        //默认-f选项
        if (!args.containsKey("-f") && !args.containsKey("--format") && fileExtName == null){
            switch (params.getType()){
                case Audio:
                    args.put("-f", "m4a");
                    break;
                case Video:
                    args.put("-f", "bestvideo+bestaudio");
                    break;
            }
        }
        */
        //设置代理
        if (!proxy.type().equals(Proxy.Type.DIRECT)){
            InetSocketAddress address = (InetSocketAddress) proxy.address();
            String proxyUrl = proxy.type().name().toLowerCase() + "://" + address.getHostName() + ":" + address.getPort();
            args.put("--proxy",proxyUrl);
        }

        return args;
    }

    /**
     * 根据分辨率和视频编码配置下载参数
     * @param args
     * @param resolution
     * @param vcodec
     */
    private static void configureResolutionAndCodec(Map<String, String> args, String resolution, String vcodec) {
        final String BEST_RESOLUTION = "最佳";
        final String WORST_RESOLUTION = "最差";
        final String DEFAULT_RESOLUTION = "默认";
        final String AVC_CODEC = "avc";
        final String VP9_CODEC = "vp9";

        // 无视频编码的情况下处理分辨率
        if (vcodec == null) {
            if (WORST_RESOLUTION.equals(resolution)) {
                args.put("-f", "worstvideo+worstaudio");
            } else if (BEST_RESOLUTION.equals(resolution)) {
                args.put("-f", "bestvideo+bestaudio");
            } else if (DEFAULT_RESOLUTION.equals(resolution)) {
                //yt-dlp 默认是bestvideo*+bestaudio/best
            } else {
                args.put("-f", "(bv*[resolution~=" + resolution + "p]+ba)/(bv*+ba)");
            }
        } else {
            // 根据视频编码和分辨率处理
            String format = buildFormatString(vcodec, resolution, WORST_RESOLUTION, BEST_RESOLUTION);
            args.put("-f", format);
        }
    }

    /**
     * 构建格式字符串
     * @param vcodec
     * @param resolution
     * @param worst
     * @param best
     * @return
     */
    private static String buildFormatString(String vcodec, String resolution, String worst, String best) {
        final String AVC_PATTERN = "^(avc|h264)";
        final String VP9_PATTERN = "^(vp9)";

        String codecPattern = Objects.equals(vcodec, "avc") ? AVC_PATTERN : VP9_PATTERN;

        if (worst.equals(resolution)) {
            return "(wv*[vcodec~='" + codecPattern + "']+ba)/(bv*+ba)";
        } else if (best.equals(resolution)) {
            return "(bv*[vcodec~='" + codecPattern + "']+ba)/(bv*+ba)";
        } else {
            return "(bv*[vcodec~='" + codecPattern + "'][resolution~='^(" + resolution + "p)']+ba)/(bv*+ba)";
        }
    }
}

