package com.example.wenhai.listenall.moudle.detail

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.example.wenhai.listenall.R
import com.example.wenhai.listenall.data.bean.Album
import com.example.wenhai.listenall.data.bean.Collect
import com.example.wenhai.listenall.data.bean.Song
import com.example.wenhai.listenall.utils.DateUtil
import com.example.wenhai.listenall.utils.GlideApp

class DetailFragment : Fragment(), DetailContract.View {
    @BindView(R.id.action_bar_title)
    lateinit var mActionBarTitle: TextView
    @BindView(R.id.detail_cover)
    lateinit var mCover: ImageView
    @BindView(R.id.detail_title)
    lateinit var mTitle: TextView
    @BindView(R.id.detail_artist)
    lateinit var mArtist: TextView
    @BindView(R.id.detail_date)
    lateinit var mDate: TextView
    @BindView(R.id.detail_song_list)
    lateinit var mSongList: RecyclerView

    lateinit var mSongListAdapter: SongListAdapter


    override fun setAlbumDetail(album: Album) {
    }

    override fun setPresenter(presenter: DetailContract.Presenter) {
        mPresenter = presenter
    }

    companion object {
        const val TAG = "DetailFragment"
    }

    lateinit var mPresenter: DetailContract.Presenter
    lateinit var mUnBinder: Unbinder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater !!.inflate(R.layout.fragment_detail, container, false)
        mUnBinder = ButterKnife.bind(this, contentView)
        initView()

        val id = arguments.getLong("id")
        val type = arguments.getInt("type")
        val loadType: Type = if (type == Type.COLLECT.ordinal) {
            Type.COLLECT
        } else {
            Type.ALBUM
        }
        mPresenter.loadDetails(id, loadType)
        return contentView
    }


    override fun initView() {
        mActionBarTitle.text = "歌单详情"
        mSongListAdapter = SongListAdapter(ArrayList<Song>())
        mSongList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mSongList.adapter = mSongListAdapter
    }

    @OnClick(R.id.action_bar_back)
    fun onClick(view: View) {
        when (view.id) {
            R.id.action_bar_back -> {
                activity.finish()
            }
        }
    }

    override fun setCollectDetail(collect: Collect) {
        activity.runOnUiThread({
            mTitle.text = collect.title
            mArtist.visibility = View.GONE
            GlideApp.with(context).load(collect.coverUrl)
                    .placeholder(R.drawable.ic_main_all_music)
                    .into(mCover)
            val displayDate = "创建时间：${DateUtil.getDate(collect.createDate)}"
            mDate.text = displayDate
            mSongListAdapter.setData(collect.songs)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mUnBinder.unbind()
    }

    inner class SongListAdapter(var songList: List<Song>) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val song = songList[position]
            val index = "${position + 1}"
            holder !!.index.text = index
            holder.title.text = song.name
            // TODO: 2017/8/5 如果歌手名和专辑名太长，只显示歌手名
            val artistAndAlbum = song.artistName
            holder.artistAlbum.text = artistAndAlbum
        }

        override fun getItemCount(): Int = songList.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_detail_song_list, parent, false)
            return ViewHolder(itemView)
        }

        fun setData(songList: List<Song>) {
            this.songList = songList
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var index: TextView = itemView.findViewById(R.id.detail_index)
            val title: TextView = itemView.findViewById(R.id.detail_song_title)
            var artistAlbum: TextView = itemView.findViewById(R.id.detail_artist_album)
        }
    }
}