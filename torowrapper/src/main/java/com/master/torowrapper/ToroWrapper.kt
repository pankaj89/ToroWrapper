package com.master.torowrapper

import android.app.Application
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import im.ene.toro.exoplayer.Config
import im.ene.toro.exoplayer.ExoCreator
import im.ene.toro.exoplayer.MediaSourceBuilder
import im.ene.toro.exoplayer.ToroExo
import im.ene.toro.widget.Container
import java.io.File

//----------
fun Container.setConfig(config: Config) {
    this.setTag(R.string.app_name, config)
}

fun Container.getConfig(): Config? {
    val tag = this.getTag(R.string.app_name)
    if (tag != null && tag is Config) {
        return tag
    }
    return null
}
//----------

class ToroWrapper : LifecycleObserver {
    companion object {
        fun with(context: Context, playerView: PlayerView, enableCaching: Boolean = false, enableLooping: Boolean = false): ToroWrapper {
            return ToroWrapper(context, playerView, enableCaching, enableLooping)
        }

        var cacheFile: SimpleCache? = null
        fun initWith(app: Application, cacheSizeInMb: Int = 400) {
            cacheFile = SimpleCache(File(app.getCacheDir(), "media"), LeastRecentlyUsedCacheEvictor((cacheSizeInMb * 1024 * 1024).toLong()))
        }
    }


    private var exoCreator: ExoCreator? = null
    private var mPlayer: SimpleExoPlayer
    private var playerView: PlayerView
    private var progressRequired: Boolean = false
    private var isPreparing = false //This flag is used only for callback

    private constructor(context: Context, playerView: PlayerView, enableCaching: Boolean = false, enableLooping: Boolean = false) {
        this.playerView = playerView
        exoCreator = ToroExo.with(context).getCreator(
                Config.Builder().apply {
                    if (enableCaching)
                        setCache(cacheFile)
                    if (enableLooping)
                        setMediaSourceBuilder(MediaSourceBuilder.LOOPING)
                }.build()
        )

        mPlayer = ToroExo.with(context).requestPlayer(exoCreator!!)
        playerView.player = mPlayer
    }


    fun makeLifeCycleAware(activity: AppCompatActivity) {
        activity.lifecycle.addObserver(this)
    }

    fun setVideoUrl(videoUrl: String, autoPlay: Boolean) {
        val mediaSource = exoCreator?.createMediaSource(Uri.parse(videoUrl), null)
        mPlayer.playWhenReady = autoPlay
        isPreparing = true
        mPlayer.prepare(mediaSource)
    }

    /**
     * Used to start player
     * Ensure you must call this method after [setUrl] method call
     */
    fun play() {
        mPlayer.playWhenReady = true
    }

    /**
     * Used to pause player
     * Ensure you must call this method after [setUrl] method call
     */
    fun pause() {
        mPlayer.playWhenReady = false
    }

    /**
     * Used to stop player
     * Ensure you must call this method after [setUrl] method call
     */
    fun stop() {
        mPlayer.stop()
    }


    /**
     * Used to seek player to given position(in milliseconds)
     * Ensure you must call this method after [setUrl] method call
     */
    fun seekTo(positionMs: Long) {
        mPlayer.seekTo(positionMs)
    }


    val durationHandler = Handler()
    private var durationRunnable: Runnable? = null

    private fun startTimer() {
        if (progressRequired) {
            if (durationRunnable != null)
                durationHandler.postDelayed(durationRunnable, 17)
        }
    }

    private fun stopTimer() {
        if (progressRequired) {
            if (durationRunnable != null)
                durationHandler.removeCallbacks(durationRunnable)
        }
    }

    /**
     * Returns SimpleExoPlayer instance you can use it for your own implementation
     */
    fun getPlayer(): SimpleExoPlayer {
        return mPlayer
    }

    /**
     * Used to set different quality url of existing video/audio
     */
    fun setQualityUrl(qualityUrl: String) {
        val currentPosition = mPlayer.currentPosition ?: 0

        val mediaSource = exoCreator?.createMediaSource(Uri.parse(qualityUrl), null)
        mPlayer.playWhenReady = true
        mPlayer.prepare(mediaSource)
        mPlayer.seekTo(currentPosition)

    }

    /**
     * Normal speed is 1f and double the speed would be 2f.
     */
    fun setSpeed(speed: Float) {
        val param = PlaybackParameters(speed)
        mPlayer.setPlaybackParameters(param)
    }

    /**
     * Returns whether player is playing
     */
    fun isPlaying(): Boolean {
        return mPlayer.playWhenReady ?: false
    }

    /**
     * Toggle mute and unmute
     */
    fun toggleMuteUnMute() {
        if (mPlayer.volume == 0f) unMute() else mute()
    }

    /**
     * Mute player
     */
    fun mute() {
        mPlayer.volume = 0f
    }

    /**
     * Unmute player
     */
    fun unMute() {
        mPlayer.volume = 1f
    }


    //Life Cycle
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected fun onPause() {
        mPlayer.playWhenReady = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected fun onDestroy() {
        mPlayer.playWhenReady = false
    }

    //LISTENERS

    /**
     * Listener that used for most popular callbacks
     */
    fun setListener(progressRequired: Boolean = false, listener: Listener) {
        this.progressRequired = progressRequired

        mPlayer.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            }

            override fun onSeekProcessed() {
            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            }

            override fun onLoadingChanged(isLoading: Boolean) {
            }

            override fun onPositionDiscontinuity(reason: Int) {
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                listener.onError(error)
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

                if (isPreparing && playbackState == Player.STATE_READY) {
                    isPreparing = false
                    listener.onPlayerReady()
                }
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        listener.onBuffering(true)
                    }
                    Player.STATE_READY -> {
                        listener.onBuffering(false)
                        if (playWhenReady) {
                            startTimer()
                            listener.onStart()
                        } else {
                            stopTimer()
                            listener.onStop()
                        }
                    }
                    Player.STATE_IDLE -> {
                        stopTimer()
                        listener.onBuffering(false)
                        listener.onError(null)
                    }
                    Player.STATE_ENDED -> {
                        listener.onBuffering(false)
                        stopTimer()
                        listener.onStop()
                    }
                }
                /*if (playbackState == Player.STATE_BUFFERING) {
                    listener.onBuffering(true)
                } else if (playbackState == Player.STATE_IDLE) {
                    listener.onError(null)
                } else {
                    listener.onBuffering(false)
                }

                if (playbackState == Player.STATE_IDLE) {
                    if (playWhenReady) {
                        startTimer()
                        listener.onStart()
                    } else {
                        stopTimer()
                        listener.onStop()
                    }
                }*/
            }
        })

        playerView.setControllerVisibilityListener { visibility ->
            listener.onToggleControllerVisible(visibility == View.VISIBLE)
        }

        if (progressRequired) {
            durationRunnable = Runnable {
                listener.onProgress(mPlayer.currentPosition)
                if (mPlayer.playWhenReady) {
                    durationHandler.postDelayed(durationRunnable, 500)
                }
            }
        }
    }

    interface Listener {
        fun onPlayerReady() {}
        fun onStart() {}
        fun onStop() {}
        fun onProgress(positionMs: Long) {}
        fun onError(error: ExoPlaybackException?) {}
        fun onBuffering(isBuffering: Boolean) {}
        fun onToggleControllerVisible(isVisible: Boolean) {}
    }
}