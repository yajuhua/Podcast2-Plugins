package io.github.yajuhua.plugin.bilibili.wrapper;

import io.github.yajuhua.plugin.bilibili.api.Collection;
import io.github.yajuhua.plugin.bilibili.api.Favorites;
import io.github.yajuhua.plugin.bilibili.api.Space;
import io.github.yajuhua.plugin.bilibili.api.Video;
import io.github.yajuhua.plugin.bilibili.api.dto.CollectionDTO;
import io.github.yajuhua.plugin.bilibili.api.dto.FavContentDTO;
import io.github.yajuhua.plugin.bilibili.api.dto.SpaceVideosDTO;
import io.github.yajuhua.plugin.bilibili.api.dto.VideoDTO;
import io.github.yajuhua.plugin.bilibili.main.Main;
import io.github.yajuhua.podcast2API.Item;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 获取space和收藏夹中视频列表中的bvid
 * 目前支持：
 * space https://space.bilibili.com/483162496
 * 收藏夹 https://www.bilibili.com/medialist/detail/ml1208491396?type=1&spm_id_from=333.999.0.0
 */
@Slf4j
public class Items {

    /**
     * 获取收藏夹的视频列表
     * @param url
     * @param es
     * @return
     * @throws Exception
     */
    public static List<Item> getFav(String url,List<Integer> es) throws Exception {
        String mediaId = Favorites.getMediaId(url);
        FavContentDTO content = Favorites.getContent(mediaId);
        if (0 != content.getCode()){
            throw new Exception(content.getMessage());
        }
        List<FavContentDTO.DataDTO.MediasDTO> medias = content.getData().getMedias();
        List<Item> items = new ArrayList<>();
        for (FavContentDTO.DataDTO.MediasDTO media : medias) {
            String bvid = media.getBvid();
            //排除没有bvid的
            if (bvid != null && !bvid.isEmpty()){
                Item item = Item.builder()
                        //收藏的时间
                        //.publicTime(media.getFavTime() * 1000L)
                        .link("https://www.bilibili.com/video/" + bvid)
                        .enclosure("https://www.bilibili.com/video/" + bvid)
                        .duration(media.getDuration())
                        .image(media.getCover())
                        .equal(bvid)
                        .title(media.getTitle())
                        .createTime(System.currentTimeMillis())
                        .build();
                if (Main.supportPublicTime()){
                    item.setPublicTime(media.getFavTime() * 1000L);
                }
                items.add(item);
            }
        }
        //选择节目
        return (List<Item>)select(items,es,"fav");
    }

    /**
     * 获取space 节目
     * @param url
     * @param es
     * @return
     * @throws Exception
     */
    public static List<Item> getSpace(String url,List<Integer> es) throws Exception{
        SpaceVideosDTO videos = Space.getVideosByUrl(url);
        if (0 != videos.getCode()){
            throw new Exception(videos.getMessage());
        }
        List<SpaceVideosDTO.DataDTO.ItemDTO> item = videos.getData().getItem();
        List<String> collect = item.stream()
                .map(SpaceVideosDTO.DataDTO.ItemDTO::getBvid)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) {
                        //排除掉空的
                        return s != null && !s.isEmpty();
                    }
                }).collect(Collectors.toList());
        List<String> bvidList = (List<String>) select(collect,es,"space");
        List<Item> items = new ArrayList<>();
        for (String bvid : bvidList) {
            items.add(getItem(bvid));
        }
        return items;
    }

    /**
     * 视频列表 p1 p2之类的
     * @param url https://www.bilibili.com/video/BV1iV411z7Nj/?spm_id_from=333.337.search-card.all.click
     * @return bvid?pn
     * @throws Exception
     */
    public static List<Item> getVideoPages(String url, List<Integer> es)throws Exception{
        VideoDTO video = Video.getByUrl(url);
        if (0 != video.getCode()){
            throw new Exception(video.getMessage());
        }
        List<Item> items = new ArrayList<>();
        VideoDTO.DataDTO data = video.getData();
        List<VideoDTO.DataDTO.PagesDTO> pages = data.getPages();
        for (VideoDTO.DataDTO.PagesDTO page : pages) {
            String link = "https://www.bilibili.com/video/"+ data.getBvid() +"?p=" + page.getPage();
            Item item = Item.builder()
                    .createTime(System.currentTimeMillis())
                    //.publicTime(data.getCtime().longValue())
                    .description(data.getDesc())
                    .duration(page.getDuration())
                    .link(link)
                    .image(data.getPic())
                    .equal(data.getBvid() + "?" + page.getPage())
                    .title(page.getPart())
                    .enclosure(link)
                    .build();
            if (Main.supportPublicTime()){
                item.setPublicTime(data.getCtime().longValue());
            }
            items.add(item);
        }
        //最后一个才是最新的
        return (List<Item>)select(items,es,"video");
    }

    /**
     * 根据es进行过滤选择的节目
     * @param items
     * @param es
     * @param type space 、video 、fav
     * @return
     */
    private static List select(List items,List<Integer> es,String type){
        //space第一个视频是最新的，序号是1
        //fav和video 第一个视频是最久的，序号是1

        if (es == null || es.isEmpty()){
            es = new ArrayList<>();
            es.add(0);//默认第一集
        }
        if (es.get(0) == -1){
            //最近30集
            if ("video".equalsIgnoreCase(type)){
                Collections.reverse(items);
            }
            return items;
        } else if (es.get(0) == 0) {
            //最新一集
            if ("video".equalsIgnoreCase(type)){
                Collections.reverse(items);
            }
            return Arrays.asList(items.get(0));
        }else {
            //自定义
            List custom = new ArrayList<>();
            for (Integer e : es) {
                if (e < items.size()){
                    custom.add(items.get(e - 1));
                }
            }
            return custom;
        }
    }

    /** 获取space、video?p 、fav视频节目
     * @param url
     * @param es
     * @return
     */
    public static List<Item> getItems(String url,List<Integer> es) throws Exception {
        if (Video.isVaildUrl(url)){
            //单个视频，可能存在多p
           return getVideoPages(url, es);
        } else if (Favorites.isVaildUrl(url)) {
            //收藏夹
            return getFav(url,es);
        } else if (Collection.isVaildUrl(url)) {
            //视频合集
            return getCollection(url,es);
        }else if (Space.isVaildUrl(url)) {
            //用户视频列表
           return getSpace(url, es);
        }else {
            throw new Exception("不支持该链接: " + url);
        }
    }

    /**
     * 获取视频合集
     * @param url https://space.bilibili.com/224267770/channel/collectiondetail?sid=3851794
     * @param es
     * @return
     */
    public static List<Item> getCollection(String url, List<Integer> es) throws Exception {
        //视频列表默认可能是顺序或倒序，得判断一下
        CollectionDTO info = Collection.getInfo(url,false);
        if (0 != info.getCode()){
            throw new Exception(info.getMessage());
        }

        //判断视频排序方式
        if (info.getData().getArchives().size() > 1){
            List<CollectionDTO.DataDTO.ArchivesDTO> archives = info.getData().getArchives();
            Integer ctime1 = archives.get(0).getCtime();
            Integer ctime2 = archives.get(archives.size() - 1).getCtime();
            if (ctime1 < ctime2){
                //说明最后一个视频是最新的，需要反转
                info = Collection.getInfo(url,true);
                if (0 != info.getCode()){
                    throw new Exception(info.getMessage());
                }
            }
        }

        List<Item> items = new ArrayList<>();
        List<CollectionDTO.DataDTO.ArchivesDTO> archives = info.getData().getArchives();
        archives = select(archives, es, "collection");
        for (CollectionDTO.DataDTO.ArchivesDTO archive : archives) {
            if (archive.getBvid() != null && !archive.getBvid().isEmpty()){
                VideoDTO video = Video.getByBvid(archive.getBvid());
                if (0 != video.getCode()){
                    log.warn("无法获取{}视频: {}",archive.getBvid(),video.getMessage());
                }else {
                    VideoDTO.DataDTO data = video.getData();
                    String link = "https://www.bilibili.com/video/" + data.getBvid();
                    Item item = Item.builder()
                            .title(data.getTitle())
                            .image(data.getPic())
                            .link(link)
                            .enclosure(link)
                            .duration(data.getDuration())
                            .createTime(System.currentTimeMillis())
                            .description(data.getDesc())
                            .equal(data.getBvid())
                            //.publicTime(data.getPubdate()*1000L)
                            .build();
                    if (Main.supportPublicTime()){
                        item.setPublicTime(data.getPubdate()*1000L);
                    }
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * 根据bvid封装成item
     * @param bvid
     * @return
     * @throws Exception
     */
    public static Item getItem(String bvid) throws Exception {
        VideoDTO video = Video.getByBvid(bvid);
        if (0 != video.getCode()){
            throw new Exception(video.getMessage());
        }
        VideoDTO.DataDTO data = video.getData();
        String link = "https://www.bilibili.com/video/" + data.getBvid();
        Item item = Item.builder()
                .createTime(System.currentTimeMillis())
                .description(data.getDesc())
                .duration(data.getDuration())
                .link(link)
                .image(data.getPic())
                .equal(data.getBvid())
                .title(data.getTitle())
                //.publicTime(data.getCtime().longValue())
                .enclosure(link)
                .build();
        if (Main.supportPublicTime()){
            item.setPublicTime(data.getCtime()*1000L);
        }
        return item;
    }

}
