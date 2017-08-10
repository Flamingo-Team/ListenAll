package com.example.wenhai.listenall.moudle.search

import com.example.wenhai.listenall.data.LoadSearchRecommendCallback
import com.example.wenhai.listenall.data.LoadSearchResultCallback
import com.example.wenhai.listenall.data.MusicRepository
import com.example.wenhai.listenall.data.bean.Song

internal class SearchPresenter(val view: SearchContract.View) : SearchContract.Presenter {


    val musicRepository: MusicRepository = MusicRepository()

    init {
        view.setPresenter(this)
    }

    override fun searchByKeyWord(keyword: String) {
        musicRepository.searchByKeyword(keyword, object : LoadSearchResultCallback {
            override fun onFailure() {

            }

            override fun onSuccess(loadedSongs: List<Song>) {
                view.onSearchResult(loadedSongs)
            }

        })

    }

    override fun loadSearchRecommend(keyword: String) {
        musicRepository.loadSearchRecommend(keyword, object : LoadSearchRecommendCallback {
            override fun onFailure() {
            }

            override fun onSuccess(recommendKeyword: List<String>) {
                view.onSearchRecommendLoaded(recommendKeyword)
            }

        })
    }

}