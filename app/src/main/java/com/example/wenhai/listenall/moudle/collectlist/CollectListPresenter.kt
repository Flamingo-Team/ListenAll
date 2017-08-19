package com.example.wenhai.listenall.moudle.collectlist

import com.example.wenhai.listenall.data.LoadCollectCallback
import com.example.wenhai.listenall.data.MusicRepository
import com.example.wenhai.listenall.data.bean.Collect

internal class CollectListPresenter(val view: CollectListContract.View) : CollectListContract.Presenter {
    private val musicRepository: MusicRepository = MusicRepository.INSTANCE

    init {
        view.setPresenter(this)
    }

    companion object {
        const val TAG = "CollectListPresenter"
    }

    override fun loadCollects(count: Int) {
        musicRepository.loadHotCollect(count, object : LoadCollectCallback {
            override fun onStart() {
                view.onLoading()
            }

            override fun onFailure(msg: String) {
                view.onFailure(msg)
            }

            override fun onSuccess(collectList: List<Collect>) {
                view.setCollects(collectList)
            }
        })
    }
}