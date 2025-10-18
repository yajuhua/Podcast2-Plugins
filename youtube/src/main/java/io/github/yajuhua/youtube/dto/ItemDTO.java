package io.github.yajuhua.youtube.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ItemDTO {

    @SerializedName("id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("thumbnail")
    private String thumbnail;
    @SerializedName("description")
    private String description;
    @SerializedName("channel_id")
    private String channelId;
    @SerializedName("channel_url")
    private String channelUrl;
    @SerializedName("duration")
    private Integer duration;
    @SerializedName("webpage_url")
    private String webpageUrl;
    @SerializedName("live_status")
    private String liveStatus;
    @SerializedName("channel")
    private String channel;
    @SerializedName("uploader")
    private String uploader;
    @SerializedName("uploader_id")
    private String uploaderId;
    @SerializedName("uploader_url")
    private String uploaderUrl;
    @SerializedName("upload_date")
    private String uploadDate;
    @SerializedName("timestamp")
    private Integer timestamp;
    @SerializedName("availability")
    private String availability;
    @SerializedName("original_url")
    private String originalUrl;
    @SerializedName("duration_string")
    private String durationString;
    @SerializedName("is_live")
    private Boolean isLive;
    @SerializedName("was_live")
    private Boolean wasLive;
    @SerializedName("epoch")
    private Integer epoch;
    @SerializedName("ext")
    private String ext;
    @SerializedName("width")
    private Integer width;
    @SerializedName("height")
    private Integer height;
    @SerializedName("resolution")
    private String resolution;
}
