package com.master.torowrapper

import android.net.Uri
import android.os.Handler
import android.view.View
import android.widget.ImageView
import com.master.torowrapper.getConfig
import im.ene.toro.ToroPlayer
import im.ene.toro.ToroUtil
import im.ene.toro.exoplayer.ExoPlayerViewHelper
import im.ene.toro.media.PlaybackInfo
import im.ene.toro.widget.Container

interface ToroWrapperBaseViewHolder : ToroPlayer {

    //STEP 1
    var exoPlayerHelper: ExoPlayerViewHelper?

    override fun initialize(container: Container, playbackInfo: PlaybackInfo) {
        if (exoPlayerHelper == null) {
            if (container.getConfig() != null) {
                exoPlayerHelper = ExoPlayerViewHelper(this, Uri.parse(getVideoUrl()), null, container.getConfig()!!)
            } else {
                exoPlayerHelper = ExoPlayerViewHelper(this, Uri.parse(getVideoUrl()))
            }
        }
        exoPlayerHelper?.initialize(container, playbackInfo)
    }

    //STEP 2
    override fun getPlayerView(): View {
        return getPlayer()
    }

    //STEP 3
    override fun getCurrentPlaybackInfo(): PlaybackInfo {
        return exoPlayerHelper?.latestPlaybackInfo ?: PlaybackInfo()
    }

    //STEP 4
    override fun getPlayerOrder(): Int {
        return getAdapterPosition()
    }

    //STEP 5
    override fun wantsToPlay(): Boolean {
        return ToroUtil.visibleAreaOffset(this, playerView.parent) >= 0.99
    }

    //STEP 6
    override fun isPlaying(): Boolean {
        return exoPlayerHelper?.isPlaying ?: false
    }

    //STEP 7
    override fun play() {

        exoPlayerHelper?.play()
        setVolume(if (!getMuteStatus()) 1f else 0f)

        Handler().postDelayed({
            getImagePreview()?.visibility = View.GONE
        }, 1000)
    }

    //STEP 8
    override fun pause() {
        exoPlayerHelper?.pause()
    }

    //STEP 9
    override fun release() {
        exoPlayerHelper?.release()
        exoPlayerHelper = null
    }

    fun setVolume(volume: Float) {
        try {
            exoPlayerHelper?.volume = volume
        } catch (e: Exception) {

        }
    }

    fun getVideoUrl(): String
    fun getMuteStatus(): Boolean
    fun getPlayer(): View
    fun getImagePreview(): ImageView? {
        return null
    }

    fun getAdapterPosition(): Int

}