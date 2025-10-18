package io.github.yajuhua.plugin.bilibili.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Cookie {

    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDLgd2OAkcGVtoE3ThUREbio0Eg\n" +
            "Uc/prcajMKXvkCKFCWhJYJcLkcM2DKKcSeFpD/j6Boy538YXnR6VhcuUJOhH2x71\n" +
            "nzPjfdTcqMz7djHum0qSZA0AyCBDABUqCrfNgCiJ00Ra7GmRj+YCK1NJEuewlb40\n" +
            "JNrRuoEUXpabUzGB8QIDAQAB\n" +
            "-----END PUBLIC KEY-----";


    /**
     * copy https://socialsisteryi.github.io/bilibili-API-collect/docs/login/cookie_refresh.html#go
     * @param plaintext
     * @param publicKeyStr
     * @return
     * @throws Exception
     */
    public static String getCorrespondPath(String plaintext, String publicKeyStr) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        publicKeyStr = publicKeyStr
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\n", "")
                .trim();
        byte[] publicBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicBytes);
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

        String algorithm = "RSA/ECB/OAEPPadding";
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // Encode the plaintext to bytes
        byte[] plaintextBytes = plaintext.getBytes("UTF-8");

        // Add OAEP padding to the plaintext bytes
        OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);
        // Encrypt the padded plaintext bytes
        byte[] encryptedBytes = cipher.doFinal(plaintextBytes);
        // Convert the encrypted bytes to a Base64-encoded string
        return new BigInteger(1, encryptedBytes).toString(16);
    }

    public static String getCorrespondPath()throws Exception{
       return getCorrespondPath(String.format("refresh_%d", System.currentTimeMillis()), PUBLIC_KEY);
    }

    /**
     * 是否需要更新
     * @param cookies
     * @return
     */
    public static boolean refresh(String cookies){
        String url = "https://passport.bilibili.com/x/passport-login/web/cookie/info";
        Map map = new HashMap();
        map.put("cookie",cookies);
        String json = Http.get(url, map);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String message = jsonObject.get("message").getAsString();
        try {
            return jsonObject.get("data").getAsJsonObject().get("refresh").getAsBoolean();
        } catch (Exception e) {
            throw new RuntimeException("检查是否需要刷新异常: " + message);
        }
    }


    /**
     * 获取refresh_csrf
     * @param cookies
     * @return
     * @throws Exception
     */
    private static String getRefreshCsrf(String cookies) throws Exception {
        String url = "https://www.bilibili.com/correspond/1/" + getCorrespondPath();
        Map map = new HashMap();
        map.put("cookie",cookies);
        Document document = Jsoup.connect(url).cookies(map).get();
        try {
            return document.getElementById("1-name").text();
        } catch (Exception e) {
            throw new RuntimeException("获取refresh_csrf失败: " + e.getMessage());
        }
    }

    /**
     * 游客获取 buvid3
     * @return buvid3
     */
    private static String getBuvid3(){
       return getBuvid3AndBuvid4().get("b_3");
    }

    /**
     * 游客获取 buvid4
     * @return buvid4
     */
    private static String getBuvid4(){
        return getBuvid3AndBuvid4().get("b_4");
    }

    /**
     * 获取 buvid3 / buvid4
     * @return  buvid3 / buvid4
     */
    private static Map<String,String> getBuvid3AndBuvid4(){
        String json = Http.get("https://api.bilibili.com/x/frontend/finger/spi", null);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        int code = jsonObject.get("code").getAsInt();
        if (code != 0){
            throw new RuntimeException("无法获取buvid3和buvid4");
        }
        String buvid3 = jsonObject.get("data").getAsJsonObject().get("b_3").getAsString();
        String buvid4 = jsonObject.get("data").getAsJsonObject().get("b_4").getAsString();

        Map<String,String> b34 = new HashMap<>();
        b34.put("b_3",buvid3);
        b34.put("b_4",buvid4);

        return b34;
    }

    /**
     * 获取位于 Cookie 中的bili_jct字段
     * @param cookies
     * @return
     */
    private static String getCsrf(String cookies){
        Map<String,String> map = cookiesToMap(cookies);
        if (map.containsKey("bili_jct")){
            return map.get("bili_jct");
        } else if (map.containsKey("Secure, bili_jct")) {
            return map.get("Secure, bili_jct");
        } else if (map.containsKey("Secure,bili_jct")) {
            return map.get("Secure,bili_jct");
        }
        throw new RuntimeException("未找到Cookie中的bili_jct字段");
    }


    /**
     * 刷新cookie
     * @param cookies
     * @return
     */
    private static Map<String,String> reflushCookie(String cookies,String refreshToken) throws Exception{
        cookies = cookies.replaceAll("\\s","").replaceAll("\\n","");

        //添加buvid3和buvid4
        Map<String, String> buvid3AndBuvid4 = getBuvid3AndBuvid4();
        StringBuilder cookieSb = new StringBuilder();
        cookieSb.append("buvid3=").append(buvid3AndBuvid4.get("b_3")).append(";");
        cookieSb.append("buvid4=").append(buvid3AndBuvid4.get("b_4")).append(";");
        cookies = cookieSb.append(cookies).toString();

        Map heads = new HashMap();
        heads.put("cookie",cookies);
        String url = "https://passport.bilibili.com/x/passport-login/web/cookie/refresh";
        Map params = new HashMap();
        params.put("csrf",getCsrf(cookies));
        params.put("refresh_csrf",getRefreshCsrf(cookies));
        params.put("source","main_web");
        params.put("refresh_token",refreshToken);
        String buildParams = Http.buildParams(params);

        String content = null;
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
            byte[] bytes = buildParams.getBytes();
            os.write(bytes,0,bytes.length);
            os.flush();
            os.close();


            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            content = "";
            while ((line = reader.readLine()) != null) {
                content = content + line;
            }
            reader.close();

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(content, JsonObject.class);
            int code = jsonObject.get("code").getAsInt();
            String message = jsonObject.get("message").getAsString();

            if (code == 0){
                String newRefreshToken = jsonObject.get("data").getAsJsonObject().get("refresh_token").getAsString();
                Map map = new HashMap();
                Map<String, List<String>> headerFields = connection.getHeaderFields();
                List<String> list = headerFields.get("Set-Cookie");
                String setCookieStr = "";
                for (String s : list) {
                    setCookieStr = setCookieStr + s + ";";
                }
                setCookieStr = setCookieStr.replaceAll("\\s","").replaceAll("\\n","");
                map.put("refresh_token",newRefreshToken);
                map.put("Set-Cookie",setCookieStr);
                return map;
            }else {
                throw new RuntimeException("刷新cookie异常: " + message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     *
     * @param newCookies 最新的cookies
     * @param oldRreshToken 旧的，不是刷新返回的
     * @return
     */
    private static boolean confirmRefresh(String newCookies,String oldRreshToken){
        newCookies = newCookies.replaceAll("\\s","").replaceAll("\\n","");
        String url = "https://passport.bilibili.com/x/passport-login/web/confirm/refresh";
        Map map = new HashMap();
        map.put("csrf",getCsrf(newCookies));
        map.put("refresh_token",oldRreshToken);
        Map map1 = new HashMap();
        map1.put("cookie",newCookies);
        String json = Http.post(url, map1, Http.buildParams(map));
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        return jsonObject.get("code").getAsInt() == 0;
    }

    /**
     * 获取最新cookie和fresh_token
     * @param cookies
     * @param freshToken
     * @return refresh_token 和 Set-Cookie
     * @throws Exception
     */
    public static Map getNewCookieAndRreshToken(String cookies,String freshToken) throws Exception{
        Map<String, String> map = reflushCookie(cookies,freshToken);
        boolean b = confirmRefresh(map.get("Set-Cookie"), freshToken);
        if (b){
            return map;
        }else {
            return null;
        }
    }


    /**
     * 将字符串cookies转成Map集合
     * @param cookies
     * @return
     */
    public static Map cookiesToMap(String cookies){
        cookies = cookies.replaceAll("\\n","").replaceAll("\\s","");
        String[] split = cookies.split(";");
        Map<String,String> cookieList = new HashMap();
        for (String s : split) {
            if (s != null && s.contains("=")){
                String[] s1 = s.split("=");
                cookieList.put(s1[0],s1[1]);
            }
        }
        return cookieList;
    }

    /**
     * 将cookie写成Netscape格式的
     */
    public static List<String> createCookieToNetscape(String cookies){
        Map<String,String> map = cookiesToMap(cookies);
        List<String> netcape = new ArrayList<>();
        Set<String> keys = map.keySet();
        long millis = 13371627903633644L;
        String head = "# Netscape HTTP Cookie File";
        netcape.add(head);
        for (String key : keys) {
            String line = String.format(".bilibili.com\tTRUE\t/\tFALSE\t%d\t%s\t%s",millis,key,map.get(key));
            netcape.add(line);
        }
        return netcape;
    }
}
