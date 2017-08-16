package com.example.wenhai.listenall.data.onlineprovider

import android.os.AsyncTask
import android.text.TextUtils
import com.example.wenhai.listenall.data.ArtistRegion
import com.example.wenhai.listenall.data.LoadAlbumCallback
import com.example.wenhai.listenall.data.LoadAlbumDetailCallback
import com.example.wenhai.listenall.data.LoadArtistAlbumsCallback
import com.example.wenhai.listenall.data.LoadArtistDetailCallback
import com.example.wenhai.listenall.data.LoadArtistHotSongsCallback
import com.example.wenhai.listenall.data.LoadArtistsCallback
import com.example.wenhai.listenall.data.LoadBannerCallback
import com.example.wenhai.listenall.data.LoadCollectByCategoryCallback
import com.example.wenhai.listenall.data.LoadCollectCallback
import com.example.wenhai.listenall.data.LoadCollectDetailCallback
import com.example.wenhai.listenall.data.LoadSearchRecommendCallback
import com.example.wenhai.listenall.data.LoadSearchResultCallback
import com.example.wenhai.listenall.data.LoadSongDetailCallback
import com.example.wenhai.listenall.data.MusicProvider
import com.example.wenhai.listenall.data.MusicSource
import com.example.wenhai.listenall.data.bean.Album
import com.example.wenhai.listenall.data.bean.Artist
import com.example.wenhai.listenall.data.bean.Collect
import com.example.wenhai.listenall.data.bean.Song
import com.example.wenhai.listenall.utils.BaseResponseCallback
import com.example.wenhai.listenall.utils.LogUtil
import com.example.wenhai.listenall.utils.OkHttpUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Calendar

/**
 * 音乐源：虾米音乐
 * Created by Wenhai on 2017/8/4.
 */
internal class Xiami : MusicSource {

    companion object {
        @JvmStatic
        val TAG = "Xiami"
        val BASE_URL = "http://api.xiami.com/web?v=2.0&app_key=1&"
        val SUFFIX_COLLECT_DETAIL = "&callback=jsonp122&r=collect/detail"
        val SUFFIX_ALBUM_DETAIL = "&page=1&limit=20&callback=jsonp217&r=album/detail"
        val PREFIX_SEARCH_SONG = "http://api.xiami.com/web?v=2.0&app_key=1&key="
        val SUFFIX_SEARCH_SONG = "&page=1&limit=50&callback=jsonp154&r=search/songs"
        val PREFIX_SEARCH_RECOMMEND = "http://www.xiami.com/ajax/search-index?key="
        //后面加时间
        val INFIX_SEARCH_RECOMMEND = "&_="


        //get hidden listen url when "listen file" is null
        val PREFIX_SONG_DETAIL = "http://www.xiami.com/song/playlist/id/"
        val SUFFIX_SONG_DETAIL = "/object_name/default/object_id/0/cat/json"

        //singer type:0-全部 1-华语 2-欧美 3-日本 4-韩国
        //http://www.xiami.com/artist/index/c/2/type/1
        // c 1-本周流行 2-热门艺人
//        http://www.xiami.com/artist/index/c/2/type/1/class/0/page/1
        val URL_PREFIX_LOAD_ARTISTS = "http://www.xiami.com/artist/index/c/2/type/"
        val URL_INFIX_LOAD_ARTISTS = "/class/0/page/"
        val URL_HOME = "http://www.xiami.com"
    }


    override fun loadBanner(callback: LoadBannerCallback) {
        val url = "http://www.xiami.com/"
        callback.onStart()
        LoadBannerTask(callback).execute(url)
    }

    override fun loadHotCollect(count: Int, callback: LoadCollectCallback) {
        val url = "http://www.xiami.com/collect/recommend/page/1"
        callback.onStart()
        LoadCollectTask(count, callback).execute(url)
    }

    override fun loadNewAlbum(count: Int, callback: LoadAlbumCallback) {
//        type contains "all" "huayu" "oumei" "ri" "han"
        //presenting "全部" "华语"  "欧美"  "日本" "韩国"
        val url = "http://www.xiami.com/music/newalbum/type/all/page/1"
        callback.onStart()
        LoadNewAlbumTask(count, callback).execute(url)
    }

    override fun loadCollectDetail(id: Long, callback: LoadCollectDetailCallback) {
        val url = BASE_URL + "id=$id" + SUFFIX_COLLECT_DETAIL
        OkHttpUtil.getForXiami(url, object : BaseResponseCallback() {
            override fun onStart() {
                callback.onStart()
            }

            override fun onJsonObjectResponse(jsonObject: JSONObject) {
                val collect = getCollectFormJson(jsonObject)
                callback.onSuccess(collect)
            }

            override fun onFailure(msg: String) {
                LogUtil.e(TAG, msg)
                callback.onFailure(msg)
            }

        })

    }

    private fun getCollectFormJson(data: JSONObject): Collect {
        val collect = Collect()
        collect.source = MusicProvider.XIAMI
        collect.id = data.getLong("list_id")
        collect.title = data.getString("collect_name")
        collect.coverUrl = data.getString("logo")
        collect.songCount = data.getInt("songs_count")
        collect.createDate = data.getLong("gmt_create")
        collect.updateDate = data.getLong("gmt_modify")
        collect.playTimes = data.getInt("play_count")
        collect.songs = getSongsFromJson(data.getJSONArray("songs"))
        return collect
    }

    private fun getSongsFromJson(songs: JSONArray?): ArrayList<Song>? {
        val songCount = songs !!.length()
        val songList = ArrayList<Song>(songCount)
        for (i in 0 until songCount) {
            val song = Song()
            val jsonSong: JSONObject = songs.get(i) as JSONObject
            song.songId = jsonSong.getLong("song_id")
            song.name = jsonSong.getString("song_name")
            song.albumId = jsonSong.getLong("album_id")
            song.albumName = jsonSong.getString("album_name")
            song.artistId = jsonSong.getLong("artist_id")
            try {
                song.artistName = jsonSong.getString("artist_name")
            } catch (e: JSONException) {
                song.artistName = jsonSong.getString("singers")
            }
            // multi artist
            if (song.artistName.contains(";")) {
                val artists = song.artistName.split(";")
                val artistBuilder = StringBuilder()
                for (artist in artists) {
                    artistBuilder.append(artist)
                    artistBuilder.append("&")
                }
                song.artistName = artistBuilder.substring(0, artistBuilder.length - 1)
            }

            song.listenFileUrl = ""
            song.supplier = MusicProvider.XIAMI
            songList.add(song)
        }
        return songList
    }

    override fun loadSongDetail(song: Song, callback: LoadSongDetailCallback) {
        val id = song.songId
        val url = PREFIX_SONG_DETAIL + id + SUFFIX_SONG_DETAIL
        OkHttpUtil.getForXiami(url, object : BaseResponseCallback() {
            override fun onStart() {
                callback.onStart()
            }

            override fun onJsonObjectResponse(jsonObject: JSONObject) {
                val songInfo = jsonObject.getJSONObject("data")
                if (songInfo.isNull("trackList")) {
                    //不能播放
                    callback.onFailure("当前歌曲不能播放，请切换平台试试")
                } else {
                    val trackList: JSONArray = songInfo.getJSONArray("trackList")
                    if (trackList.length() > 0) {
                        val track = trackList.getJSONObject(0)
                        if (TextUtils.isEmpty(song.listenFileUrl)) {
                            song.listenFileUrl = getListenUrlFromLocation(track.getString("location"))
                        }
                        val canFreeListen = track.getJSONObject("purviews").getJSONObject("LISTEN").getString("LOW")
                        song.isCanFreeListen = canFreeListen == "FREE"
                        val canFreeDownload = track.getJSONObject("purviews").getJSONObject("DOWNLOAD").getString("LOW")
                        song.isCanFreeDownload = canFreeDownload == "FREE"
                        song.length = track.getInt("length")
                        try {
                            song.lyricUrl = track.getString("lyric_url")
                        } catch (e: JSONException) {
                            song.lyricUrl = ""
                        }
                        song.albumCoverUrl = track.getString("album_pic")
                        song.miniAlbumCoverUrl = track.getString("pic")
                        song.albumName = track.getString("album_name")
                        song.albumId = track.getLong("album_id")
                        song.artistName = track.getString("artist_name")
                        song.artistId = track.getLong("artist_id")
                        callback.onSuccess(song)
                    } else {
                        callback.onFailure("当前歌曲不能播放，请切换平台试试")
                    }

                }
            }

            override fun onFailure(msg: String) {
                callback.onFailure(msg)
            }

        })
    }

    /*
     *parse "location" string and get listen file url
     */
    private fun getListenUrlFromLocation(location: String): String {
        val num = location[0] - '0'
        val avgLen = Math.floor((location.substring(1).length / num).toDouble()).toInt()
        val remainder = location.substring(1).length % num

        val result = ArrayList<String>()
        (0 until remainder).mapTo(result) { location.substring(it * (avgLen + 1) + 1, (it + 1) * (avgLen + 1) + 1) }
        (0 until num - remainder).mapTo(result) { location.substring((avgLen + 1) * remainder).substring(it * avgLen + 1, (it + 1) * avgLen + 1) }

        val s = ArrayList<String>()
        for (i in 0 until avgLen) {
            (0 until num).mapTo(s) { result[it][i].toString() }
        }
        (0 until remainder).mapTo(s) { result[it][result[it].length - 1].toString() }

        val joinStr = s.joinToString("")
        val listenFile = URLDecoder.decode(joinStr, "utf-8").replace("^", "0")
        return listenFile
    }

    override fun loadAlbumDetail(id: Long, callback: LoadAlbumDetailCallback) {
        val url = BASE_URL + "id=$id" + SUFFIX_ALBUM_DETAIL
        OkHttpUtil.getForXiami(url, object : BaseResponseCallback() {
            override fun onStart() {
                callback.onStart()
            }

            override fun onJsonObjectResponse(jsonObject: JSONObject) {
                super.onJsonObjectResponse(jsonObject)
                val album = Album()
                album.supplier = MusicProvider.XIAMI
                album.id = jsonObject.getLong("album_id")
                album.artist = jsonObject.getString("artist_name")
                album.artistId = jsonObject.getLong("artist_id")
                album.title = jsonObject.getString("album_name")
                album.songNumber = jsonObject.getInt("song_count")
                album.publishDate = jsonObject.getLong("gmt_publish")
                album.coverUrl = jsonObject.getString("album_logo")
                album.miniCoverUrl = album.coverUrl + "@1e_1c_100Q_100w_100h"
                album.songs = getSongsFromJson(jsonObject.getJSONArray("songs"))
                callback.onSuccess(album)
            }

            override fun onFailure(msg: String) {
                callback.onFailure(msg)
                LogUtil.e(TAG, msg)
            }

        })
    }

    override fun searchByKeyword(keyword: String, callback: LoadSearchResultCallback) {
        val encodedKeyword = URLEncoder.encode(keyword, "utf-8")
        val url = PREFIX_SEARCH_SONG + encodedKeyword + SUFFIX_SEARCH_SONG
        OkHttpUtil.getForXiami(url, object : BaseResponseCallback() {
            override fun onStart() {
                callback.onStart()
            }

            override fun onJsonObjectResponse(jsonObject: JSONObject) {
                super.onJsonObjectResponse(jsonObject)
                val songs: ArrayList<Song>? = getSongsFromJson(jsonObject.getJSONArray("songs"))
                if (songs == null || songs.size == 0) {
                    callback.onFailure("搜索失败")
                } else {
                    callback.onSuccess(songs)
                }
            }

            override fun onFailure(msg: String) {
                callback.onFailure(msg)
            }

        })
    }

    override fun loadSearchRecommend(keyword: String, callback: LoadSearchRecommendCallback) {
        val currentTime = Calendar.getInstance().timeInMillis
        val url = PREFIX_SEARCH_RECOMMEND + URLEncoder.encode(keyword, "utf-8") + INFIX_SEARCH_RECOMMEND + currentTime
        OkHttpUtil.getForXiami(url, object : BaseResponseCallback() {

            override fun onStart() {
                callback.onStart()
            }

            override fun onHtmlResponse(html: String) {
                val keywordList = getRecommendKeywords(html)
                callback.onSuccess(keywordList)
            }

            override fun onFailure(msg: String) {
                super.onFailure(msg)
                callback.onFailure(msg)
            }

        })

    }

    private fun getRecommendKeywords(string: String): List<String> {
        val keywordList = ArrayList<String>()
        val document = Jsoup.parse(string)
        val result = document.getElementsByClass("result")
        result.map { it.select("a").first().attr("title") }
                .filterNotTo(keywordList) { TextUtils.isEmpty(it) }
        return keywordList
    }

    override fun loadArtists(region: ArtistRegion, callback: LoadArtistsCallback) {
        val type = when (region) {
            ArtistRegion.ALL -> {
                0
            }
            ArtistRegion.CN -> {
                1
            }
            ArtistRegion.EA -> {
                2
            }
            ArtistRegion.JP -> {
                3
            }
            ArtistRegion.KO -> {
                4
            }
        }
        val page = 1.toString()
        val url = URL_PREFIX_LOAD_ARTISTS + "$type" + URL_INFIX_LOAD_ARTISTS + page
        OkHttpUtil.getForXiami(url, object : BaseResponseCallback() {
            override fun onStart() {
                callback.onStart()
            }

            override fun onFailure(msg: String) {
                super.onFailure(msg)
                callback.onFailure(msg)
            }

            override fun onHtmlResponse(html: String) {
                super.onHtmlResponse(html)
                val artists: ArrayList<Artist> = parseArtistList(html)
                callback.onSuccess(artists)
            }

        })

    }

    private fun parseArtistList(html: String): ArrayList<Artist> {
        val result = ArrayList<Artist>()
        val document = Jsoup.parse(html)
        val artists = document.getElementById("artists")
        val artistElements = artists.getElementsByClass("artist")
        for (artistElement in artistElements) {
            val artist = Artist()
            val img = artistElement.getElementsByClass("image").first()
            artist.name = artistElement.getElementsByClass("info").first()
                    .select("a").first().attr("title")
            artist.miniImgUrl = img.select("img").first()
                    .attr("src")
            val homePageSuffix = artistElement.getElementsByClass("image").first()
                    .select("a").first()
                    .attr("href")
            artist.homePageSuffix = homePageSuffix
            val artistId = homePageSuffix.substring(homePageSuffix.lastIndexOf("/") + 1)
            artist.artistId = artistId
            artist.hotSongSuffix = "/artist/top-" + artistId
            artist.albumSuffix = "/artist/album-" + artistId
            result.add(artist)
        }
        return result

    }

    override fun loadArtistDetail(artist: Artist, callback: LoadArtistDetailCallback) {
        val url = URL_HOME + artist.homePageSuffix
        OkHttpUtil.getForXiami(url, object : BaseResponseCallback() {
            override fun onStart() {
                callback.onStart()
            }

            override fun onHtmlResponse(html: String) {
                super.onHtmlResponse(html)
                val detailedArtist = parseAndAddArtistDetail(html, artist)
                callback.onSuccess(detailedArtist)
            }

            override fun onFailure(msg: String) {
                super.onFailure(msg)
                callback.onFailure(msg)
            }

        })
    }

    //get desc and imgUrl
    private fun parseAndAddArtistDetail(html: String, artist: Artist): Artist {
        val document = Jsoup.parse(html)
        val block = document.getElementById("artist_block")
        val info = block.getElementById("artist_info")
        val desc = info.select("tr").last()
                .getElementsByClass("record").first()
                .text()
        artist.desc = desc
        val img = block.getElementById("artist_photo")
        val imgUrl = img.select("a").first().attr("href")
        artist.imgUrl = imgUrl
        return artist
    }

    override fun loadArtistHotSongs(artist: Artist, callback: LoadArtistHotSongsCallback) {
//        后面加 ?page=1 指定页数
        val url = URL_HOME + artist.hotSongSuffix + "?page=1"
        OkHttpUtil.getForXiami(url, object : BaseResponseCallback() {
            override fun onStart() {
                callback.onStart()
            }

            override fun onHtmlResponse(html: String) {
                super.onHtmlResponse(html)
                val hotSongs = parseArtistHotSongs(html)
                callback.onSuccess(hotSongs)
            }

            override fun onFailure(msg: String) {
                super.onFailure(msg)
                callback.onFailure(msg)
            }

        })
    }

    private fun parseArtistHotSongs(html: String): List<Song> {
        val document = Jsoup.parse(html)
        val trackList = document.getElementsByClass("track_list").first()
        val tracks = trackList.select("tr")
        val songs = ArrayList<Song>()
        for (track in tracks) {
            val song = Song()
            song.name = track.getElementsByClass("song_name").first()
                    .select("a").first()
                    .attr("title")
            val onClick = track.getElementsByClass("song_act").first()
                    .getElementsByClass("song_play").first()
                    .attr("onClick")
            val extra = track.getElementsByClass("song_name").first()
                    .getElementsByClass("show_zhcn").first()
            if (extra != null) {
                //临时显示用
                song.albumName = extra.text()
            } else {
                song.albumName = ""
            }
            val songId = onClick.substring(onClick.indexOf("'") + 1, onClick.indexOf(",") - 1)
            song.songId = songId.toLong()
            song.supplier = MusicProvider.XIAMI
            songs.add(song)
        }
        return songs
    }

    override fun loadArtistAlbums(artist: Artist, callback: LoadArtistAlbumsCallback) {
//        ?page=2
        val url = URL_HOME + artist.albumSuffix + "?page=1"
        OkHttpUtil.getForXiami(url, object : BaseResponseCallback() {
            override fun onStart() {
                callback.onStart()
            }

            override fun onHtmlResponse(html: String) {
                super.onHtmlResponse(html)
                val albums = parseArtistAlbums(html)
                callback.onSuccess(albums)
            }

            override fun onFailure(msg: String) {
                callback.onFailure(msg)
            }

        })
    }

    private fun parseArtistAlbums(html: String): List<Album> {
        val albums = ArrayList<Album>()
        val document = Jsoup.parse(html)
        val albumsElement = document.getElementById("artist_albums").
                getElementsByClass("albumThread_list").first()
                .select("li")
        for (albumElement in albumsElement) {
            val album = Album()
            album.supplier = MusicProvider.XIAMI
            val id = albumElement.select("div").first().attr("id")
            album.id = id.substring(id.indexOf("_") + 1).toLong()
            album.miniCoverUrl = albumElement.getElementsByClass("cover").first()
                    .select("img").attr("src")
            album.coverUrl = album.miniCoverUrl.substring(0, album.miniCoverUrl.indexOf("@"))
            val detail = albumElement.getElementsByClass("detail").first()
            val title = detail.getElementsByClass("name").first()
                    .select("a").first().attr("title")
            album.title = title
            val publishDate = detail.getElementsByClass("company").first()
                    .select("a").last().text()
            album.publishDateStr = publishDate
            albums.add(album)
        }
        return albums
    }

    override fun loadCollectByCategory(category: String, callback: LoadCollectByCategoryCallback) {
        val url = if (category == "全部歌单") {
            "http://www.xiami.com/collect/recommend/page/1" //热门
        } else {
            "http://www.xiami.com/search/collect?key=${URLEncoder.encode(category, "utf-8")}"
        }
        OkHttpUtil.getForXiami(url, object : BaseResponseCallback() {
            override fun onStart() {
                callback.onStart()
            }

            override fun onHtmlResponse(html: String) {
                super.onHtmlResponse(html)
                val collects = parseCollectsFromHTML(html)
                callback.onSuccess(collects)
            }

            override fun onFailure(msg: String) {
                super.onFailure(msg)
                callback.onFailure(msg)
            }

        })

    }

    private fun parseCollectsFromHTML(html: String): List<Collect> {
        val document = Jsoup.parse(html)
        val collects = ArrayList<Collect>()
        val page = document.getElementById("page")
        val list = page.getElementsByClass("block_items clearfix")
        for (i in 0 until list.size) {
            val element = list[i]
            val a = element.select("a").first()
            val title = a.attr("title")
            val ref = a.attr("href")
            val id = getIdFromHref(ref)
            val coverUrl = a.select("img").first().attr("src")
            val collect = Collect()
            collect.id = id.toLong()
            collect.title = title
            collect.coverUrl = coverUrl.substring(0, coverUrl.length - 11)
            collect.source = MusicProvider.XIAMI
            collects.add(collect)
        }
        return collects
    }

    private fun getIdFromHref(ref: String): Int {
        val idStr = ref.substring(ref.lastIndexOf('/') + 1)
        return Integer.valueOf(idStr) !!
    }


    //AsyncTasks
    internal class LoadBannerTask(val callback: LoadBannerCallback)
        : AsyncTask<String, Void, List<String>?>() {

        override fun doInBackground(vararg url: String?): List<String>? {
            val document: Document? = Jsoup.connect(url[0]).get()
            if (document != null) {
                val slider: Element? = document.getElementById("slider")
                val items: Elements = slider !!.getElementsByClass("item")
                val imgUrlList = ArrayList<String>(items.size)
                (0 until items.size).mapTo(imgUrlList) {
                    items[it].select("a").first().select("img").first().attr("src")
                }

                return imgUrlList
            } else {
                return null
            }
        }

        override fun onPostExecute(result: List<String>?) {
            super.onPostExecute(result)
            if (result == null || result.isEmpty()) {
                callback.onFailure("")
            } else {
                callback.onSuccess(result)
            }
        }

    }

    internal class LoadCollectTask(val count: Int, val callback: LoadCollectCallback)
        : AsyncTask<String, Void, List<Collect>?>() {
        override fun doInBackground(vararg url: String?): List<Collect>? {
            val document: Document? = Jsoup.connect("http://www.xiami.com/collect/recommend/page/1").get()
            if (document != null) {
                val page = document.getElementById("page")
                val list = page.getElementsByClass("block_items clearfix")
                val collectList = ArrayList<Collect>(6)
                for (i in 0 until count) {
                    val element = list[i]
                    val a = element.select("a").first()
                    val title = a.attr("title")
                    val ref = a.attr("href")
                    val id = getIdFromHref(ref)
                    val coverUrl = a.select("img").first().attr("src")
                    val collect = Collect()
                    collect.id = id.toLong()
                    collect.title = title
                    collect.coverUrl = coverUrl.substring(0, coverUrl.length - 11)
                    collect.source = MusicProvider.XIAMI
                    collectList.add(collect)
                }
                return collectList
            } else {
                return null
            }
        }

        override fun onPostExecute(result: List<Collect>?) {
            super.onPostExecute(result)
            if (result == null || result.isEmpty()) {
                callback.onFailure("获取热门歌单失败")
            } else {
                callback.onSuccess(result)
            }
        }

        private fun getIdFromHref(ref: String): Int {
            val idStr = ref.substring(ref.lastIndexOf('/') + 1)
            return Integer.valueOf(idStr) !!
        }


    }

    internal class LoadNewAlbumTask(val count: Int, val callback: LoadAlbumCallback)
        : AsyncTask<String, Void, List<Album>?>() {
        override fun doInBackground(vararg url: String?): List<Album>? {
            val document = Jsoup.connect(url[0]).get()
            if (document != null) {
                val albumElement = document.getElementById("albums")
                val albums = albumElement.getElementsByClass("album")
                val albumList = ArrayList<Album>(count)
                if (albums.size > 0) {
                    for (i in 0 until count) {
                        val imgElement = albums[i].getElementsByClass("image").first()
                        val onclick: String = imgElement.select("b").first().attr("onclick")
                        val id = onclick.substring(onclick.indexOf('(', 0, false) + 1, onclick.indexOf(',', 0, false))
                        val coverUrl = imgElement.select("img").first().attr("src")
                        val title = albums[i].getElementsByClass("info").first()
                                .select("p").first()
                                .select("a").first().attr("title")
                        val artist = albums[i].getElementsByClass("info").first()
                                .select("p").next()
                                .select("a").first().attr("title")
                        val album = Album()
                        album.coverUrl = coverUrl.substring(0, coverUrl.length - 20)
                        album.title = title
                        album.artist = artist
                        album.id = id.toLong()
                        albumList.add(album)
                    }
                }
                return albumList
            } else {
                return null
            }
        }

        override fun onPostExecute(result: List<Album>?) {
            super.onPostExecute(result)
            if (result == null || result.isEmpty()) {
                callback.onFailure("获取最新音乐失败")
            } else {
                callback.onSuccess(result)
            }
        }

    }
}