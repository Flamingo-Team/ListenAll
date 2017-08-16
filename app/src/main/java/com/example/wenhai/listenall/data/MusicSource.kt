package com.example.wenhai.listenall.data

import com.example.wenhai.listenall.base.BaseCallBack
import com.example.wenhai.listenall.data.bean.Album
import com.example.wenhai.listenall.data.bean.Artist
import com.example.wenhai.listenall.data.bean.Collect
import com.example.wenhai.listenall.data.bean.Song

/**
 * 音乐数据接口类
 *
 * Created by Wenhai on 2017/8/4.
 */
interface MusicSource {
    fun loadBanner(callback: LoadBannerCallback)
    fun loadHotCollect(count: Int = 6, callback: LoadCollectCallback)
    fun loadNewAlbum(count: Int = 6, callback: LoadAlbumCallback)
    fun loadCollectDetail(id: Long, callback: LoadCollectDetailCallback)
    fun loadAlbumDetail(id: Long, callback: LoadAlbumDetailCallback)
    fun loadSongDetail(song: Song, callback: LoadSongDetailCallback)
    fun searchByKeyword(keyword: String, callback: LoadSearchResultCallback)
    fun loadSearchRecommend(keyword: String, callback: LoadSearchRecommendCallback)
    fun loadArtists(region: ArtistRegion, callback: LoadArtistsCallback)
    fun loadArtistDetail(artist: Artist, callback: LoadArtistDetailCallback)
    fun loadArtistHotSongs(artist: Artist, callback: LoadArtistHotSongsCallback)
    fun loadArtistAlbums(artist: Artist, callback: LoadArtistAlbumsCallback)
    fun loadCollectByCategory(category: String, callback: LoadCollectByCategoryCallback)
}


//callbacks
interface LoadBannerCallback : BaseCallBack {
    fun onSuccess(imgUrlList: List<String>)
}

interface LoadCollectCallback : BaseCallBack {
    fun onSuccess(collectList: List<Collect>)
}

interface LoadAlbumCallback : BaseCallBack {
    fun onSuccess(albumList: List<Album>)
}

interface LoadCollectDetailCallback : BaseCallBack {
    fun onSuccess(collect: Collect)
}

interface LoadAlbumDetailCallback : BaseCallBack {
    fun onSuccess(album: Album)
}

interface LoadSongDetailCallback : BaseCallBack {
    fun onSuccess(loadedSong: Song)
}

interface LoadSearchResultCallback : BaseCallBack {
    fun onSuccess(loadedSongs: List<Song>)
}

interface LoadSearchRecommendCallback : BaseCallBack {
    fun onSuccess(recommendKeyword: List<String>)
}

interface LoadArtistsCallback : BaseCallBack {
    fun onSuccess(artists: List<Artist>)
}

interface LoadArtistDetailCallback : BaseCallBack {
    fun onSuccess(artistDetail: Artist)
}

interface LoadArtistHotSongsCallback : BaseCallBack {
    fun onSuccess(hotSongs: List<Song>)
}

interface LoadArtistAlbumsCallback : BaseCallBack {
    fun onSuccess(albums: List<Album>)
}

interface LoadCollectByCategoryCallback : BaseCallBack {
    fun onSuccess(collects: List<Collect>)
}