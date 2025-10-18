package io.github.yajuhua.plugin.bilibili.Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class Http {

    /**
     * 读取 plugin.properties配置文件
     * @return
     */
    public static Properties readProperties(){
        Properties pluginProperties = new Properties();
        try {
            // 使用ClassLoader加载Properties文件
            InputStream inputStream = Http.class.getClassLoader().getResourceAsStream("plugin.properties");
            pluginProperties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pluginProperties;
    }

    /**
     * 获取网页内容
     * @param url
     * @return
     */
    public static String getHttpContent(String url){
        return WebContentP(url);
    }

    /**
     * 代理版获取网页内容
     * @param URL
     * 配置proxyHost和proxyPort即可
     * @return
     * @throws Exception
     */
    public static String WebContentP(String URL){
        String Content = null;
        try {
            java.net.URL url = new URL(URL);
            HttpURLConnection connection =(HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.67");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);//超时设置为5s

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            Content = "";
            while ((line = reader.readLine()) != null) {
                Content = Content + line;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  Content;
    }

    /**
     * get请求
     * @return
     * @throws Exception
     */
    public static String get(String url, Map heads){
        String Content = null;
        try {
            HttpURLConnection connection =(HttpURLConnection)new URL(url).openConnection();

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.67");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
            connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
            connection.setRequestMethod("GET");
            if (heads != null){
                Set set = heads.keySet();
                for (Object key : set) {
                    connection.setRequestProperty((String) key, (String) heads.get(key));
                }
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            Content = "";
            while ((line = reader.readLine()) != null) {
                Content = Content + line;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  Content;
    }

    /**
     * get请求
     * @return
     * @throws Exception
     */
    public static byte[] getToBytes(String url, Map heads){
        String Content = null;
        try {
            HttpURLConnection connection =(HttpURLConnection)new URL(url).openConnection();

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.67");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
            connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
            connection.setRequestMethod("GET");
            if (heads != null){
                Set set = heads.keySet();
                for (Object key : set) {
                    connection.setRequestProperty((String) key, (String) heads.get(key));
                }
            }
            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                return byteArrayOutputStream.toByteArray();
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String post(String url, Map heads,String params){
        String Content = null;
        try {
            HttpURLConnection connection =(HttpURLConnection)new URL(url).openConnection();

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.67");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
            connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
            connection.setRequestMethod("POST");
            if (params != null){
                connection.setDoOutput(true);
            }
            if (heads != null || !heads.isEmpty()){
                Set set = heads.keySet();
                for (Object key : set) {
                    connection.setRequestProperty((String) key, (String) heads.get(key));
                }
            }
            OutputStream os = connection.getOutputStream();
            byte[] bytes = params.getBytes();
            os.write(bytes,0,bytes.length);
            os.flush();
            os.close();

            System.out.println(connection.getHeaderFields());


            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            Content = "";
            while ((line = reader.readLine()) != null) {
                Content = Content + line;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  Content;
    }

    /**
     * 构建请求
     * @param url
     * @return
     */
    public static String buildParams(String url, Map params){
        if (params != null || !params.isEmpty()){
            Set keys = params.keySet();
            String p = "";
            for (Object key : keys) {
              p = p +  key + "=" + params.get(key) + "&";
            }
            return url + "?" +  p.substring(0,p.length() - 1);
        }
        return url;
    }

    /**
     * 构建请求
     * @param params
     * @return
     */
    public static String buildParams(Map params){
        if (params != null || !params.isEmpty()){
            Set keys = params.keySet();
            String p = "";
            for (Object key : keys) {
                p = p +  key + "=" + params.get(key) + "&";
            }
            return p.substring(0,p.length() - 1);
        }
        return null;
    }

    /**
     * 解析查询字符串为参数键值对
     * @param query
     * @return
     * @throws Exception
     */
    public static Map<String, String> getQueryParams(String query) throws Exception {
        Map<String, String> params = new HashMap<>();
        String[] pairs = query.split("&");  // 按照 & 分割每个参数

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = URLDecoder.decode(keyValue[0], "UTF-8");  // 解码参数名
            String value = URLDecoder.decode(keyValue[1], "UTF-8");  // 解码参数值
            params.put(key, value);  // 将键值对放入 map
        }

        return params;
    }
}
