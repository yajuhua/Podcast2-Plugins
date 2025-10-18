package io.github.yajuhua.plugin.bilibili.api.downloader;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 构建http请求
 */
@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    private String url;
    private String body;//POST 的请求体
    private String httpMethod;//请求方式 GET/POST
    private Map<String, List<String>> header;
}
