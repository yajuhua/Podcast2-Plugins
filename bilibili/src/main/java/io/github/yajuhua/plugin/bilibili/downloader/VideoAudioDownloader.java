package io.github.yajuhua.plugin.bilibili.downloader;

import com.google.gson.Gson;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.commons.progress.DownloadProgressCallback;
import io.github.yajuhua.download.commons.utils.BuildCmd;
import io.github.yajuhua.download.downloader.Downloader;
import io.github.yajuhua.download.downloader.ytdlp.utils.Convert;
import io.github.yajuhua.plugin.bilibili.api.Play;
import io.github.yajuhua.plugin.bilibili.api.Video;
import io.github.yajuhua.plugin.bilibili.api.dto.PlayDTO;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 音视频下载
 */
@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VideoAudioDownloader implements Runnable, Downloader {
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
        //设置格式
        boolean hasFormat = args.containsKey("-f") || args.containsKey("--format");
        String format = null;
        String ext = "mp4";
        if (hasFormat){
            if (args.containsKey("-f")){
                format = args.get("-f").toString();
            }else {
                format = args.get("--format").toString();
            }
        }else {
            switch (type) {
                case Video:
                    format = "bestvideo+bestaudio";
                    break;
                case Audio:
                    format = "m4a";
                    ext = "m4a";
                    break;
            }
        }

        //生成info-json文件
        Gson gson = new Gson();
        File infoJsonFile = new File(System.getProperty("java.io.tmpdir"),System.currentTimeMillis() + ".json");
        InfoJson infoJson = getInfoJson(links.get(0));
        log.info("生成info-json: {}",infoJsonFile.getAbsolutePath());
        FileUtils.writeStringToFile(infoJsonFile, gson.toJson(infoJson));
        finalFormat = ext;

        String save = dir.getAbsolutePath() + File.separator + uuid + "." + ext;
        args.put("--path",dir.getAbsolutePath());
        args.put("--output",save);
        args.put("--format",format);
        args.put("--load-info-json", infoJsonFile.getAbsolutePath());
        if (type.name().equalsIgnoreCase("video")){
            args.put("--merge-output-format","mp4");
        }

        //构建下载命令
        String[] options = BuildCmd.buildArrayArgs(args);
        String[] ytDlpCmd = new String[options.length + 1];
        ytDlpCmd[0] = "yt-dlp";
        for (int i = 0; i < options.length; i++) {
            ytDlpCmd[i + 1] = options[i];
        }

        //开始下载
        log.info("执行命令: {}", Arrays.toString(ytDlpCmd));
        cmd = ytDlpCmd;
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
        }else {
            updateProgressStatus(Context.COMPLETED);
        }

        //删除临时文件
        infoJsonFile.delete();
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
     * 封装成yt-dlp所需的info-json
     * @param url
     * @return
     */
    public static InfoJson getInfoJson(String url) throws Exception {
        //获取视频流信息
        PlayDTO playDTO = Play.getByUrl(url);
        PlayDTO.DataDTO.DashDTO dash = playDTO.getData().getDash();
        List<PlayDTO.DataDTO.DashDTO.AudioDTO> audios = dash.getAudio();
        List<PlayDTO.DataDTO.DashDTO.VideoDTO> videos = dash.getVideo();

        //封装
        String bvid = Video.getBvid(url);
        InfoJson infoJson = new InfoJson();
        infoJson.setId(bvid);
        infoJson.setTitle(bvid);

        List<InfoJson.FormatsDTO> formatsDTOS = new ArrayList<>();
        //audio
        for (PlayDTO.DataDTO.DashDTO.AudioDTO audio : audios) {
            InfoJson.FormatsDTO formatsDTO = new InfoJson.FormatsDTO();
            //String format = audio.getMimeType().split("/")[1];
            String format = "m4a";
            formatsDTO.setExt(format);
            formatsDTO.setFormat(format);
            formatsDTO.setAudioExt(format);
            formatsDTO.setFormatId(audio.getId().toString());
            formatsDTO.setAcodec(audio.getCodecs());
            formatsDTO.setVcodec("none");
            formatsDTO.setUrl(audio.getBaseUrl());
            //添加到集合中
            formatsDTOS.add(formatsDTO);
        }
        //video
        for (PlayDTO.DataDTO.DashDTO.VideoDTO video : videos) {
            InfoJson.FormatsDTO formatsDTO = new InfoJson.FormatsDTO();
            String format = video.getMimeType().split("/")[1];
            formatsDTO.setVideoExt(format);
            formatsDTO.setFormat(format);
            formatsDTO.setHeight(video.getHeight());
            formatsDTO.setWidth(video.getWidth());
            formatsDTO.setVcodec(video.getCodecs());
            formatsDTO.setAcodec("none");
            formatsDTO.setFormatId(video.getId().toString());
            formatsDTO.setUrl(video.getBaseUrl());
            //添加到集合中去
            formatsDTOS.add(formatsDTO);
        }
        //http header
        InfoJson.FormatsDTO.HttpHeadersDTO httpHeader = new InfoJson.FormatsDTO.HttpHeadersDTO();
        httpHeader.setReferer("https://www.bilibili.com");
        httpHeader.setAccept("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpHeader.setAcceptLanguage("en-us,en;q=0.5");
        httpHeader.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/96.0.4664.55 Safari/537.36");

        for (InfoJson.FormatsDTO formatsDTO : formatsDTOS) {
            formatsDTO.setHttpHeaders(httpHeader);
        }

        infoJson.setFormats(formatsDTOS);
        return infoJson;
    }
}
