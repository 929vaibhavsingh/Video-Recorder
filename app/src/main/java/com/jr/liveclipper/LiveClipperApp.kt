package com.jr.liveclipper

import android.app.Application

class LiveClipperApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private lateinit var instance: LiveClipperApp

        @JvmStatic
        fun getApplicationContext() = instance
    }
}