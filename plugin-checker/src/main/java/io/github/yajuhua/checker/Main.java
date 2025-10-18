package io.github.yajuhua.checker;

import com.google.gson.Gson;
import io.github.yajuhua.checker.dto.MetaDataDTO;
import io.github.yajuhua.checker.utils.MD5Util;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.Podcast2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import sun.security.provider.MD5;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    private static Map<String,List<URLClassLoader>> fileLoaderMap = new ConcurrentHashMap();

    public static void main(String[] args) throws IOException {
        //1.读取根目录下metadata.json中全部插件 与 根目录下v2全部插件 做差集，找出最新构建的插件
        File v2Dir = new File(System.getProperty("user.dir") + File.separator + "v2");
        File metadataJsonFile = new File(System.getProperty("user.dir") + File.separator + "metadata.json");
        MetaDataDTO metaDataDTO;
        Gson gson = new Gson();
        try {
            String medataJsonStr = FileUtils.readFileToString(metadataJsonFile);
            metaDataDTO = gson.fromJson(medataJsonStr, MetaDataDTO.class);

        } catch (IOException e) {
            log.error("读取metadata.json错误: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        //获取metadata.json中所有插件文件名称
        List<String> metadataPluginUrls = metaDataDTO.getPluginList()
                .stream()
                .map(MetaDataDTO.PluginListDTO::getUrl)
                .collect(Collectors.toList());
        List<String> metadataPluginNames = new ArrayList<>();
        for (String url : metadataPluginUrls) {
            metadataPluginNames.add(url.replace("/v2/", ""));
        }

        //获取v2目录下面所有的插件名称
        List<String> v2DirPluginNames = Arrays.stream(v2Dir.listFiles()).map(File::getName).collect(Collectors.toList());
        v2DirPluginNames.removeAll(metadataPluginNames);



        //2.加载新的插件的信息
        List<File> newPluginFile = Arrays.stream(v2Dir.listFiles()).filter(new Predicate<File>() {
            @Override
            public boolean test(File file) {
                return v2DirPluginNames.contains(file.getName());
            }
        }).collect(Collectors.toList());

        //3.将新的插件信息追加到medata.json
        for (File file : newPluginFile) {
            Properties properties = getPluginProperties(file.getAbsolutePath(), "plugin.properties");
            metaDataDTO.getPluginList()
                    .add(MetaDataDTO.PluginListDTO
                            .builder()
                            .name(properties.getProperty("name"))
                            .version(properties.getProperty("version"))
                            .updateTime(properties.getProperty("update"))
                            .uuid(properties.getProperty("uuid"))
                            .url("/v2/" + file.getName())
                            .md5(MD5Util.getFileMD5(file))
                            .build());
        }
        String update = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        metaDataDTO.setUpdateTime(update);

        FileUtils.writeStringToFile(metadataJsonFile, gson.toJson(metaDataDTO));

    }

    /**
     * 获取插件属性
     * @param jarPath
     * @return
     */
    private static Properties getPluginProperties(String jarPath, String propertiesFileName) {
        try {
            URLClassLoader classLoader = getClassLoader(jarPath);
            InputStream inputStream = classLoader.getResourceAsStream(propertiesFileName);
            if (inputStream == null) {
                throw new IOException("Property file not found in the jar: " + jarPath);
            }
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from jar: " + jarPath, e);
        }
    }

    /**
     * 获取类加载器
     * @param jarPath Jar文件路径
     * @return
     * @throws MalformedURLException
     */
    private static URLClassLoader getClassLoader(String jarPath) throws MalformedURLException {
        URL jarUrl = new File(jarPath).toURI().toURL();
        // 使用当前线程的上下文类加载器作为父类加载器
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl},Thread.currentThread().getContextClassLoader());
        if (fileLoaderMap.containsKey(jarPath)){
            fileLoaderMap.get(jarPath).add(classLoader);
        }else {
            List<URLClassLoader> urlClassLoaders = new ArrayList<>();
            urlClassLoaders.add(classLoader);
            fileLoaderMap.put(jarPath,urlClassLoaders);
        }
        return classLoader;
    }

}
