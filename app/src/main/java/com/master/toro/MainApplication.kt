package com.master.toro

import android.app.Application
import com.master.torowrapper.ToroWrapper


class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ToroWrapper.initWith(this, cacheSizeInMb = 100)
    }
}