package io.github.yajuhua.plugin.bilibili.wrapper;

import io.github.yajuhua.plugin.bilibili.api.Collection;
import io.github.yajuhua.plugin.bilibili.api.Favorites;
import io.github.yajuhua.plugin.bilibili.api.Space;
import io.github.yajuhua.plugin.bilibili.api.Video;
import io.github.yajuhua.plugin.bilibili.api.dto.CollectionDTO;
import io.github.yajuhua.plugin.bilibili.api.dto.FavInfoDTO;
import io.github.yajuhua.plugin.bilibili.api.dto.UpCardDTO;
import io.github.yajuhua.plugin.bilibili.api.dto.VideoDTO;
import io.github.yajuhua.podcast2API.Channel;
/**
 * 目前支持：
 * space https://space.bilibili.com/483162496
 * 收藏夹 https://www.bilibili.com/medialist/detail/ml1208491396?type=1&spm_id_from=333.999.0.0
 */
public class ChannelInfo {

    public static Channel get(String url)throws Exception{
        if (Collection.isVaildUrl(url)){
            //合集
            return getCollection(url,true);
        } else if (Favorites.isVaildUrl(url)) {
            //收藏夹
            return getFav(url);
        } else if (Space.isVaildUrl(url)) {
            //用户视频列表
            return getSpace(url);
        } else if (Video.isVaildUrl(url)) {
            //多p视频
            return getVideo(url);
        }else {
            throw new Exception("不支持该链接: " + url);
        }
    }

    /**
     * 获取收藏夹信息
     * @param url
     * @return
     */
    public static Channel getFav(String url) throws Exception {
        FavInfoDTO info = Favorites.getInfoByUrl(url);
        if (0 != info.getCode()){
            throw new Exception(info.getMessage());
        }
        FavInfoDTO.DataDTO data = info.getData();
        return Channel.builder()
                .image(data.getCover())
                .author(data.getUpper().getName())
                .title(data.getTitle())
                .description("")
                .link(url)
                .status(1)
                .build();
    }

    /**
     * 获取space主页信息
     * @param url
     * @return
     * @throws Exception
     */
    public static Channel getSpace(String url)throws Exception{
        UpCardDTO upInfo = Space.getUpInfoByUrl(url);
        if ( 0 != upInfo.getCode()){
            throw new Exception(upInfo.getMessage());
        }
        UpCardDTO.DataDTO data = upInfo.getData();
        UpCardDTO.DataDTO.CardDTO card = data.getCard();
        return Channel.builder()
                .link(url)
                .author(card.getName())
                .description(card.getDescription())
                .status(1)
                .title(card.getName())
                .image(card.getFace())
                .build();
    }

    /**
     * 视频列表 p1 p2之类的
     * @param url https://www.bilibili.com/video/BV1iV411z7Nj/?spm_id_from=333.337.search-card.all.click
     * @return
     */
    public static Channel getVideo(String url) throws Exception {
        VideoDTO video = Video.getByUrl(url);
        if (0 != video.getCode()){
            throw new Exception(video.getMessage());
        }
        VideoDTO.DataDTO data = video.getData();
        return Channel.builder()
                .link(url)
                .author(data.getOwner().getName())
                .description(data.getDesc())
                .status(1)
                .title(data.getTitle())
                .image(data.getPic())
                .build();
    }


    /**
     * 获取合集
     * @param url https://space.bilibili.com/224267770/channel/collectiondetail?sid=3851794
     * @param sortReverse 为true时第一个视频是最新的
     * @return
     */
    public static Channel getCollection(String url,boolean sortReverse) throws Exception {
        CollectionDTO info = Collection.getInfo(url,sortReverse);
        if (0 != info.getCode()){
            throw new Exception(info.getMessage());
        }
        CollectionDTO.DataDTO.MetaDTO meta = info.getData().getMeta();
        return Channel.builder()
                .link(url)
                .author(meta.getName())
                .description(meta.getDescription())
                .status(1)
                .title(meta.getName())
                .image(meta.getCover())
                .build();
    }

}
