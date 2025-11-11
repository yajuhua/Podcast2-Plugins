package io.github.yajuhua.youtube.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class EntrieDTO {

    @SerializedName("_type")
    private String type;
    @SerializedName("ie_key")
    private String ieKey;
    @SerializedName("id")
    private String id;
    @SerializedName("url")
    private String url;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("duration")
    private Double duration;
    @SerializedName("channel_id")
    private String channelId;
    @SerializedName("channel")
    private String channel;
    @SerializedName("channel_url")
    private String channelUrl;
    @SerializedName("uploader")
    private String uploader;
    @SerializedName("uploader_id")
    private String uploaderId;
    @SerializedName("uploader_url")
    private String uploaderUrl;
    @SerializedName("thumbnails")
    private List<ThumbnailsDTO> thumbnails;
    @SerializedName("timestamp")
    private String timestamp;
    @SerializedName("release_timestamp")
    private String releaseTimestamp;
    @SerializedName("availability")
    private String availability;
    @SerializedName("view_count")
    private Integer viewCount;
    @SerializedName("live_status")
    private String liveStatus;
    @SerializedName("channel_is_verified")
    private Boolean channelIsVerified;

    @NoArgsConstructor
    @Data
    public static class ThumbnailsDTO {
        @SerializedName("url")
        private String url;
        @SerializedName("height")
        private Integer height;
        @SerializedName("width")
        private Integer width;
    }
}
