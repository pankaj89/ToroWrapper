package com.master.toro

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.exoplayer2.ExoPlaybackException
import com.master.torowrapper.ToroWrapper
import com.master.torowrapper.VideoTimelineView
import kotlinx.android.synthetic.main.activity_video_trimmer.*
import kotlin.math.roundToLong

class VideoTrimmerActivity : AppCompatActivity() {

    var leftProgress: Float = 0f
    var rightProgress: Float = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_trimmer)

//        val file="https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"
        val file = "/storage/emulated/0/WhatsApp/Media/WhatsApp Video/VID-20181215-WA0000.mp4"

        val toroWrapper = ToroWrapper.with(this, playerView = playerView)
        toroWrapper.makeLifeCycleAware(this)
        toroWrapper.setVideoUrl(file, false)

        val TAG = "TAG"
        toroWrapper.setListener(true, object : ToroWrapper.Listener {

            override fun onProgress(positionMs: Long) {
                super.onProgress(positionMs)
                Log.d(TAG, "onProgress $positionMs")

                if (positionMs >= rightProgress) {
                    toroWrapper.seekTo(leftProgress.roundToLong())
                    toroWrapper.pause()
                }
            }

            override fun onPlayerReady() {
                Log.d(TAG, "onPlayerReady")
            }

            override fun onBuffering(isBuffering: Boolean) {
                Log.d(TAG, "onBuffering: ${isBuffering}")
            }

            override fun onError(error: ExoPlaybackException?) {
                Log.d(TAG, "onError: ${error}")
            }

            override fun onStart() {
                super.onStart()
                Log.d(TAG, "onStart")
            }

            override fun onStop() {
                super.onStop()
                Log.d(TAG, "onStop")
            }

            override fun onToggleControllerVisible(isVisible: Boolean) {
                Log.d(TAG, "onToggleControllerVisible:${isVisible}")
            }
        })

        //------Trimmer
        videoTimeLineView.setVideoPath(file)

        videoTimeLineView.setMaxProgressDiffInMillis(20000f) //20 seconds
        videoTimeLineView.setMinProgressDiffInMillis(2000f) //2 seconds

        leftProgress = videoTimeLineView.leftProgressInMillis
        rightProgress = videoTimeLineView.rightProgressInMillis
        updateText()

        toroWrapper.seekTo(leftProgress.roundToLong())

//        range_slider.setRoundFrames(true)
        videoTimeLineView.setDelegate(object : VideoTimelineView.VideoTimelineViewDelegate {
            override fun onLeftProgressChanged(progress: Float) {
                leftProgress = videoTimeLineView.leftProgressInMillis
                rightProgress = videoTimeLineView.rightProgressInMillis
                updateText()

                toroWrapper.seekTo(leftProgress.roundToLong())
                toroWrapper.pause()
            }

            override fun onRightProgressChanged(progress: Float) {
                leftProgress = videoTimeLineView.leftProgressInMillis
                rightProgress = videoTimeLineView.rightProgressInMillis
                updateText()
                toroWrapper.seekTo(leftProgress.roundToLong())
                toroWrapper.pause()
            }

            override fun didStartDragging() {
            }

            override fun didStopDragging() {
            }
        })
    }

    private fun updateText() {
        tvMessage.setText("${millisecondsToTime(leftProgress.toLong())}-${millisecondsToTime(rightProgress.toLong())}")
    }

    private fun millisecondsToTime(milliseconds: Long): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60
        val secondsStr = java.lang.Long.toString(seconds)
        val secs: String
        if (secondsStr.length >= 2) {
            secs = secondsStr.substring(0, 2)
        } else {
            secs = "0$secondsStr"
        }

        return minutes.toString() + ":" + secs
    }
}