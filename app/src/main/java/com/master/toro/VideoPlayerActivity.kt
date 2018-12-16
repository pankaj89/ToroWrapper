package com.master.toro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.master.torowrapper.ToroWrapper
import kotlinx.android.synthetic.main.activity_video_player.*

class VideoPlayerActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context, url: String) {
            context.startActivity(Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra("url", url)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        var file = "https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"
        if (intent.hasExtra("url")) {
            file = intent.getStringExtra("url")
        }
        val toroWrapper = ToroWrapper.with(this, playerView = playerView, enableCaching = true, enableLooping = false)
        toroWrapper.makeLifeCycleAware(this)
        toroWrapper.setVideoUrl(file, true)
    }
}