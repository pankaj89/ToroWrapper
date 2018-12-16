package com.master.toro

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ui.PlayerView
import com.master.torowrapper.ToroWrapperBaseViewHolder
import im.ene.toro.exoplayer.ExoPlayerViewHelper

class VideosRecyclerViewAdapter(val activity: AppCompatActivity, var mute: Boolean = true) : RecyclerView.Adapter<VideosRecyclerViewAdapter.MyViewHolder>() {

    val layoutInflater = LayoutInflater.from(activity)
    val glide = Glide.with(activity)

    companion object {
        const val TYPE_VIDEO = 1
        const val TYPE_OTHER = 2
    }

    val list = ArrayList<MediaModel>()

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int): MyViewHolder {

        when (viewType) {
            TYPE_VIDEO -> {
                val view = layoutInflater.inflate(R.layout.single_card_video, container, false)
                return MyVideoViewHolder(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.single_card_image, container, false)
                return MyViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list.get(position).name?.startsWith("Video") == true) {
            TYPE_VIDEO
        } else {
            TYPE_OTHER
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.onBind(list.get(position))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: MutableList<Any>) {
        var isHandled = false
        if (!payloads.isEmpty()) {
            if (payloads.get(0).equals("update-volume")) {
                holder.onBindPayload(payloads.get(0).toString())
                isHandled = true
            }
        }
        if (!isHandled) {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        Log.i("TAG", "onViewAttachedToWindow ${holder.adapterPosition}")
        holder.onViewAttachedToWindow()
        super.onViewAttachedToWindow(holder)
    }

    open class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        open fun onBind(model: MediaModel) {}
        open fun onBindPayload(payload: String) {}
        open fun onViewAttachedToWindow() {}
    }


    internal inner class MyVideoViewHolder(view: View) : MyViewHolder(view), ToroWrapperBaseViewHolder {

        override var exoPlayerHelper: ExoPlayerViewHelper? = null

        val frPlayer = view.findViewById<View>(R.id.frPlayer)
        val playerView = view.findViewById<PlayerView>(R.id.playerView)
        val imgPreview = view.findViewById<ImageView>(R.id.img_preview)
        val imgVolume = view.findViewById<ImageView>(R.id.img_vol)


        var model: MediaModel? = null
        override fun onBind(model: MediaModel) {
            super.onBind(model)

            this.model = model

            imgVolume.isSelected = !mute
            setVolume(if (!mute) 1f else 0f)

            glide.load(model.image_url).into(imgPreview)
            imgPreview.visibility = View.VISIBLE

            imgVolume.setOnClickListener {
                mute = !mute
                imgVolume.isSelected = !mute
                setVolume(if (!mute) 1f else 0f)

                //notify above 2 and below 2 elements
                this@VideosRecyclerViewAdapter.notifyItemChanged(adapterPosition - 1, "update-volume")
                this@VideosRecyclerViewAdapter.notifyItemChanged(adapterPosition - 2, "update-volume")
                this@VideosRecyclerViewAdapter.notifyItemChanged(adapterPosition + 1, "update-volume")
                this@VideosRecyclerViewAdapter.notifyItemChanged(adapterPosition + 2, "update-volume")
            }
            frPlayer.setOnClickListener {
                VideoPlayerActivity.startActivity(activity, model.video_url!!)
            }

        }

        override fun onBindPayload(payload: String) {
            super.onBindPayload(payload)
            if (payload.equals("update-volume")) {
                imgVolume.isSelected = !mute
            }
        }

        override fun onViewAttachedToWindow() {
            imgPreview.visibility = View.VISIBLE
        }

        override fun getVideoUrl(): String {
            return model?.video_url ?: ""
        }

        override fun getMuteStatus(): Boolean {
            return mute
        }

        override fun getPlayer(): View {
            return playerView
        }

        override fun getImagePreview(): ImageView? {
            return imgPreview
        }
    }
}