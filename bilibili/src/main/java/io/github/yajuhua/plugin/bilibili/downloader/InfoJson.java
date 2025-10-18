package io.github.yajuhua.plugin.bilibili.downloader;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 封装成yt-dlp所需的Json数据
 */
@NoArgsConstructor
@Data
public class InfoJson {

    @SerializedName("id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("formats")
    private List<FormatsDTO> formats;

    @NoArgsConstructor
    @Data
    public static class FormatsDTO {
        @SerializedName("url")
        private String url;
        @SerializedName("ext")
        private String ext;
        @SerializedName("width")
        private Integer width;
        @SerializedName("height")
        private Integer height;
        @SerializedName("vcodec")
        private String vcodec;
        @SerializedName("acodec")
        private String acodec;
        @SerializedName("format_id")
        private String formatId;
        @SerializedName("http_headers")
        private HttpHeadersDTO httpHeaders;
        @SerializedName("video_ext")
        private String videoExt;
        @SerializedName("audio_ext")
        private String audioExt;
        @SerializedName("format")
        private String format;

        @NoArgsConstructor
        @Data
        public static class HttpHeadersDTO {
            @SerializedName("User-Agent")
            private String userAgent;
            @SerializedName("Accept")
            private String accept;
            @SerializedName("Accept-Language")
            private String acceptLanguage;
            @SerializedName("Sec-Fetch-Mode")
            private String secFetchMode;
            @SerializedName("Referer")
            private String referer;
        }
    }
}
