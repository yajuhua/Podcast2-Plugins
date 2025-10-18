package io.github.yajuhua.plugin.bilibili.api.downloader;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 封装http响应数据
 */
@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private Integer responseCode;
    private String responseMessage;
    private Map<String, List<String>> responseHeaders;
    private String responseBody;
    private String latestUrl;
}
