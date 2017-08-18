package com.example.wenhai.listenall.moudle.detail

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.example.wenhai.listenall.R
import com.example.wenhai.listenall.data.bean.Album
import com.example.wenhai.listenall.data.bean.Collect
import com.example.wenhai.listenall.data.bean.Song
import com.example.wenhai.listenall.moudle.main.MainActivity
import com.example.wenhai.listenall.utils.DateUtil
import com.example.wenhai.listenall.utils.FragmentUtil
import com.example.wenhai.listenall.utils.GlideApp
import com.example.wenhai.listenall.utils.ToastUtil

class DetailFragment : Fragment(), DetailContract.View {
    override fun onFailure(msg: String) {
        ToastUtil.showToast(context, msg)
    }

    companion object {
        const val TAG = "DetailFragment"
        const val TYPE_ALBUM = 0
        const val TYPE_COLLECT = 1
        const val TYPE_RANKING = 2
        const val ARGS_ID = "id"
        const val ARGS_TYPE = "type"
        const val ARGS_RANKING = "ranking"
    }

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

    private lateinit var mSongListAdapter: SongListAdapter


    lateinit var mPresenter: DetailContract.Presenter
    private lateinit var mUnBinder: Unbinder
    private var mLoadType: Int = TYPE_COLLECT
    private lateinit var rankingCollcet: Collect


    override fun setPresenter(presenter: DetailContract.Presenter) {
        mPresenter = presenter
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DetailPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater !!.inflate(R.layout.fragment_detail, container, false)
        mUnBinder = ButterKnife.bind(this, contentView)

        mLoadType = arguments.getInt("type")
        if (mLoadType == TYPE_RANKING) {
            rankingCollcet = arguments.getParcelable(ARGS_RANKING)
        } else {
            val id = arguments.getLong("id")
            mPresenter.loadSongsDetails(id, mLoadType)
        }
        initView()
        return contentView
    }


    override fun initView() {
        mActionBarTitle.text = when (mLoadType) {
            TYPE_COLLECT -> getString(R.string.collect_detail)
            TYPE_ALBUM -> getString(R.string.album_detail)
            else -> rankingCollcet.title
        }
        mSongListAdapter = SongListAdapter(context, ArrayList())
        mSongList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mSongList.adapter = mSongListAdapter
        if (mLoadType == TYPE_RANKING) {
            setRankingDetail(rankingCollcet)
        }
    }

    private fun playSong(song: Song) {
        (activity as MainActivity).playNewSong(song)
    }

    @OnClick(R.id.action_bar_back, R.id.detail_play_all, R.id.detail_download_all, R.id.detail_add_to_play, R.id.detail_liked)
    fun onClick(view: View) {
        when (view.id) {
            R.id.action_bar_back -> {
                FragmentUtil.removeFragment(fragmentManager, this)
            }
            R.id.detail_play_all -> {
                ToastUtil.showToast(activity, "play all")
            }
            R.id.detail_add_to_play -> {

            }
            R.id.detail_liked -> {

            }
        }
    }

    override fun onSongDetailLoaded(song: Song) {
        activity.runOnUiThread {
            playSong(song)
        }
    }

    override fun onLoadFailed(msg: String) {
        activity.runOnUiThread {
            ToastUtil.showToast(context, msg)
        }
    }

    override fun setCollectDetail(collect: Collect) {
        activity.runOnUiThread({
            mTitle.text = collect.title
            mArtist.visibility = View.GONE
            GlideApp.with(context).load(collect.coverUrl)
                    .placeholder(R.drawable.ic_main_all_music)
                    .into(mCover)
            val displayDate = "更新时间：${DateUtil.getDate(collect.updateDate)}"
            mDate.text = displayDate
            mSongListAdapter.setData(collect.songs)
        })
    }

    private fun setRankingDetail(collect: Collect) {
        activity.runOnUiThread({
            mTitle.text = collect.title
            mArtist.visibility = View.GONE
            GlideApp.with(context).load(collect.coverDrawable)
                    .into(mCover)
            mDate.visibility = View.GONE
            mSongListAdapter.setData(collect.songs)
        })
    }


    override fun setAlbumDetail(album: Album) {
        activity.runOnUiThread {
            mTitle.text = album.title
            mArtist.visibility = View.VISIBLE
            mArtist.text = album.artist
            val displayDate = "发行时间：${DateUtil.getDate(album.publishDate)}"
            GlideApp.with(context).load(album.coverUrl)
                    .placeholder(R.drawable.ic_main_all_music)
                    .into(mCover)
            mDate.text = displayDate
            mSongListAdapter.setData(album.songs)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mUnBinder.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    inner class SongListAdapter(val context: Context, private var songList: List<Song>) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val song = songList[position]
            val index = "${position + 1}"
            holder !!.index.text = index
            holder.title.text = song.name
            val artistName = song.artistName
            holder.artistAlbum.text = artistName
            holder.item.setOnClickListener({
                mPresenter.loadSongDetail(song)
            })
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
            var item: LinearLayout = itemView.findViewById(R.id.detail_song_item)
            var index: TextView = itemView.findViewById(R.id.detail_index)
            val title: TextView = itemView.findViewById(R.id.detail_song_title)
            var artistAlbum: TextView = itemView.findViewById(R.id.detail_artist_album)
        }
    }
}