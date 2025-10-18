package io.github.yajuhua.plugin.bilibili.downloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.yajuhua.plugin.bilibili.Utils.Danmaku;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.commons.progress.DownloadProgressCallback;
import io.github.yajuhua.download.commons.utils.BuildCmd;
import io.github.yajuhua.download.downloader.Downloader;
import io.github.yajuhua.download.downloader.ytdlp.utils.Convert;
import io.github.yajuhua.download.downloader.ytdlp.utils.Info;
import io.github.yajuhua.plugin.bilibili.Utils.FileUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 嵌入弹幕下载
 */
@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DanmakuVideoDownloader implements Runnable, Downloader {
    private int seconds = 0;
    private double speed = 0;
    private double percent = 0;
    private double totalSize = 0;
    private String finalFormat = "unknown";


    private List<String> links;
    private Operation operation;
    private Type type;
    private String channelUuid;
    private String uuid;
    private Map args;
    private File dir;
    private DownloadProgressCallback callback;
    private Matcher matcher;
    private String line;
    private BufferedReader br;
    private Process process;
    private int exitCode;
    private String[] cmd;
    public static boolean kill = false;
    private final Pattern ytdlpCompile = Pattern.compile(
            "\\[download\\]\\s+(?<percent>\\d+\\.\\d)%.*(?<totalSize>\\d+\\.\\d+K?M?G?)iB\\s+" +
                    "at\\s+(?<speed>\\d+\\.\\d+K?M?G?)iB/s\\s+ETA\\s+(?<s1>\\d{2}):(?<s2>\\d{2}):?(?<s3>\\d{2})?");
    public static Object proxyArgs;

    @Override
    public void startDownload() throws Exception {
        finalFormat = "mkv";
        //设置格式
        boolean hasFormat = args.containsKey("-f") || args.containsKey("--format");
        String format;
        if (hasFormat){
            if (args.containsKey("-f")){
                format = args.get("-f").toString();
            }else {
                format = args.get("--format").toString();
            }
        }else {
            format = "bestvideo+bestaudio";
        }

        //获取视频信息
        String json = Info.cmd(new String[]{"yt-dlp", "-f", format, "-J", links.get(0)});
        Map<String, Integer> widthAndHeight = getWidthAndHeight(json);

        //获取字幕内容并写入临时文件
        Double width = widthAndHeight.get("width").doubleValue();
        Double height = widthAndHeight.get("height").doubleValue();
        String assContent = Danmaku.danmakuXmlToAss(links.get(0), width.intValue(), height.intValue(), 10,
                46, 36, 1.2f, 0.7f, 2f);
        File assFile = new File("/tmp/bilibili/ass/" + Danmaku.getCidByVideoLink(links.get(0)) + ".ass");
        FileUtils.write(assFile,assContent,"UTF-8");

        //设置参数
//        String mp4Path = dir.getAbsolutePath() + File.separator + uuid + ".mp4";
        String mkvPath = dir.getAbsolutePath() + File.separator + uuid + ".mkv";
        String tmpPath = dir.getAbsolutePath() + File.separator + uuid + "-tmp.mkv";
        String assPath = assFile.getAbsolutePath();
        args.put("--merge-output-format","mkv");
        args.put("--path",dir.getAbsolutePath());
        args.put("--output",tmpPath);

        //构建下载命令
        String[] embedDanmakuCmd = new String[]{"ffmpeg","-i",tmpPath,"-i",assPath,"-c:v","copy","-c:a","copy","-c:s",
                "ass","-map","0:v","-map","0:a","-map","1:s",mkvPath};
        String[] ytdlpCmd = BuildCmd.buildYtDlpCmd(args,links.get(0));

        //开始下载
        log.info("执行命令: {}", Arrays.toString(ytdlpCmd));
        cmd = ytdlpCmd;
        process = Runtime.getRuntime().exec(cmd);
        br = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
        while ((line=br.readLine()) != null){
            log.debug("yt-dlp-log：{}",line);
            if (kill){return;}
            matcher = ytdlpCompile.matcher(line);
            if (matcher.find()) {
                matcherInfo();
                updateProgressStatus(Context.DOWNLOADING);
            }
        }
        exitCode = process.waitFor();
        if (exitCode != 0){
            updateProgressStatus(Context.DOWNLOAD_ERR);
            return;
        }else {
            updateProgressStatus(Context.DOWNLOADING_PATH1);
        }

        //嵌入弹幕
        String rs = Info.cmd(embedDanmakuCmd);
        updateProgressStatus(Context.MERGE_ING);
        if (rs != null){
            updateProgressStatus(Context.COMPLETED);
        }else {
            updateProgressStatus(Context.MERGE_ERR);
        }

        //清除过程文件
        File tmpFile = new File(tmpPath);
        if (tmpFile.exists()){
            tmpFile.delete();
        }
        if (assFile.exists()){
            assFile.delete();
        }
    }


    @Override
    public void run() {
        try {
            startDownload();
        } catch (Exception e) {
           updateProgressStatus(Context.DOWNLOAD_ERR);
           log.error("下载错误",e);
        }
    }

    /**
     * 结束下载
     * @throws Exception
     */
    public void kill(){
        kill=true;
        this.updateProgressStatus(Context.REMOVE);
    }

    /**
     * 是否完成
     * @return
     */
    public boolean isCompleted(){
        final Integer[] status1 = new Integer[1];
        callback((channelUuid, uuid, status, downloadProgress, downloadTimeLeft
                , totalSize, downloadSpeed, operation, type, finalFormat) -> status1[0] = status);
        return status1[0] == Context.COMPLETED;
    }

    /**
     * 获取下载回调信息
     * @param callback
     */
    public void callback(DownloadProgressCallback callback){
        this.callback = callback;
    }

    /**
     * 更新状态码
     * @param status
     */
    private void updateProgressStatus(Integer status){
        callback.onProgressUpdate(channelUuid,uuid,
                status,percent,seconds,totalSize,speed
                ,operation.toString(),type.toString(),finalFormat);
    }

    /**
     * 匹配下载日志信息
     */
    private void matcherInfo(){
        percent = Double.parseDouble(matcher.group("percent"));
        totalSize = Convert.convertToByteNum(matcher.group("totalSize"));
        speed = Convert.convertToByteNum(matcher.group("speed"));
        seconds = Convert.convertToSeconds(matcher.group("s1"),
                matcher.group("s2"), matcher.group("s3"));//剩余秒数
    }

    /**
     * 获取视频宽高
     * @param ytDlpJson yt-dlp输出的json数据
     * @return
     */
    public static Map<String,Integer> getWidthAndHeight(String ytDlpJson){
        Map<String,Integer> widthAndHeight = new HashMap<>();
        JsonElement jsonElement = JsonParser.parseString(ytDlpJson);
        JsonObject object = jsonElement.getAsJsonObject();
        if (object.has("entries")){
            JsonArray entries = object.get("entries").getAsJsonArray();
            //最后一个分辨率是最大的
            JsonObject entrie = entries.get(entries.size() - 1).getAsJsonObject();
            int width = entrie.get("width").getAsInt();
            int height = entrie.get("height").getAsInt();
            widthAndHeight.put("height",height);
            widthAndHeight.put("width",width);

        }else if (object.has("width") && object.has("height")){
            int width = object.get("width").getAsInt();
            int height = object.get("height").getAsInt();
            widthAndHeight.put("height",height);
            widthAndHeight.put("width",width);
        }else {
            //默认
            widthAndHeight.put("width",1920);
            widthAndHeight.put("height",1080);
        }
        return widthAndHeight;
    }
}
