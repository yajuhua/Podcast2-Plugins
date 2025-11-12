package io.github.yajuhua.youtube.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * cmd命令工具类
 */
@Slf4j
public class CmdLineUtil {

    /**
     *  执行命令
     * @param cmd 命令
     * @throws IOException
     * @throws Exception
     */
    public static String exec(List<String> cmd) throws Exception {
        log.info("执行命令: {}", cmd);
        BufferedReader brInput = null;
        BufferedReader brError = null;
        try {
            Process process = Runtime.getRuntime().exec(toArray(cmd));
            String line;
            StringBuilder inputLines = new StringBuilder();
            StringBuilder errorLines = new StringBuilder();
            brInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            brError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line=brInput.readLine()) != null){
                inputLines.append(line);
            }
            while ((line=brError.readLine()) != null){
                errorLines.append(line);
            }
            int exitCode = process.waitFor();
            if (exitCode == 0){
                return inputLines.toString();
            }
            throw new Exception(errorLines.toString());
        } finally {
            if (brInput != null){
                brInput.close();
            }
            if (brError != null){
                brError.close();
            }
        }
    }

    /**
     * 执行命令
     * @param appPath
     * @param options
     * @return
     */
    public static String exec(String appPath, Map options) throws Exception {
        List optionsToList = optionsToList(options);
        optionsToList.add(0,appPath);
        return exec(optionsToList);
    }

    /**
     * 执行命令
     * @param appPath
     * @param options
     * @param url
     * @return
     */
    public static String exec(String appPath, Map options,String url) throws Exception {
        List optionsToList = optionsToList(options);
        optionsToList.add(0,appPath);
        optionsToList.add(url);
        return exec(optionsToList);
    }

    /**
     * 将Map集合转换成List
     * @param options
     * @return
     */
    public static List optionsToList(Map options){
        List list = new ArrayList();
        for (Object k : options.keySet()) {
            list.add(k);
            if (options.containsKey(k) && options.get(k) != null){
                list.add(options.get(k));
            }
        }
        return list;
    }

    public static String[] toArray(List<String> list){
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}
