package com.example.wenhai.listenall.module.albumlist

import com.example.wenhai.listenall.base.BasePresenter
import com.example.wenhai.listenall.base.BaseView
import com.example.wenhai.listenall.data.bean.Album

interface AlbumListContract {
    interface Presenter : BasePresenter {
        fun loadNewAlbums(page: Int)
    }

    interface View : BaseView<Presenter> {
        fun onNewAlbumsLoad(albumList: List<Album>)
    }
}