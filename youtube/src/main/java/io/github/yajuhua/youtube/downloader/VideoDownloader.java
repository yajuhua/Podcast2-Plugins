package io.github.yajuhua.youtube.downloader;


import com.google.gson.Gson;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.commons.progress.DownloadProgressCallback;
import io.github.yajuhua.download.downloader.Downloader;
import io.github.yajuhua.download.downloader.ytdlp.utils.Convert;
import io.github.yajuhua.youtube.utils.CmdLineUtil;
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

@Slf4j
public class VideoDownloader implements Downloader,Runnable {

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
    private final Pattern YtDlpPattern = Pattern.compile(
            "\\[download\\]\\s+(?<percent>\\d+\\.\\d)%.*(?<totalSize>\\d+\\.\\d+K?M?G?)iB\\s+" +
                    "at\\s+(?<speed>\\d+\\.\\d+K?M?G?)iB/s\\s+ETA\\s+(?<s1>\\d{2}):(?<s2>\\d{2}):?(?<s3>\\d{2})?");
    public static final Pattern Aria2Patten = Pattern.compile("\\((?<percent>\\d+)\\%\\)\\sCN:\\d{1,}\\s" +
            "DL:(?<speed>\\d+\\.?\\d+K?M?G?)iB\\sETA:(?<eta>\\w+)\\]");
    private Gson gson = new Gson();

    public void startDownload()throws Exception {

        //1.视频链接
        String videoLink = links.get(0);//第一条就是
        //2.文件扩展名
        if (args.containsKey("--merge-output-format")){
            finalFormat = args.get("--merge-output-format").toString();
        }else if (args.containsKey("--audio-format")){
            finalFormat = args.get("--audio-format").toString();
        }else {
            log.error("未找到扩展名: {}",finalFormat);
            throw new Exception("未找到扩展名");
        }

        //3.构建下载命令
        if (args == null){
            args = new HashMap();
        }
        args.put("--path",dir.getAbsolutePath());
        args.put("--output",uuid + ".%(ext)s");
        List list = CmdLineUtil.optionsToList(args);
        list.add(0,"yt-dlp");
        list.add(videoLink);
        String[] cmdArr = CmdLineUtil.toArray(list);

        //4.执行下载
        Process p = Runtime.getRuntime().exec(cmdArr);
        log.info("执行命令: {}",Arrays.toString(cmdArr));
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = br.readLine()) != null){
            if (kill){return;}
            matcher = YtDlpPattern.matcher(line);
            if (matcher.find()){
                matcherInfo();
                updateProgressStatus(Context.DOWNLOADING);
            }
        }
        int exitCode = p.waitFor();
        if (exitCode != 0){
            StringBuilder result = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(p.getErrorStream(), "UTF-8"));
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
            log.error("执行异常：{}", result);
            updateProgressStatus(Context.DOWNLOAD_ERR);

        }else {
            updateProgressStatus(Context.COMPLETED);
        }
    }

    @Override
    public void run() {
        try {
            startDownload();
        } catch (Exception e) {
            updateProgressStatus(Context.DOWNLOAD_ERR);
            log.error("下载失败",e);
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

    @Override
    public String getUuid() {
        return this.uuid;
    }

    @Override
    public String getChannelUuid() {
        return this.channelUuid;
    }

    /**
     * 获取下载回调信息
     * @param callback
     */
    @Override
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
}
