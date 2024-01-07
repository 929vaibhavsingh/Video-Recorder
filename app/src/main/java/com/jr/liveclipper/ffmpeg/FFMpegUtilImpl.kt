package com.jr.liveclipper.ffmpeg

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import java.util.concurrent.TimeUnit

class FFMpegUtilImpl : FFMpegUtil {

    companion object {

        private val TAG = FFMpegUtilImpl::class.java.name

        private const val OUTPUT_EXT = ".mp4"
        private const val CMD_PART_SEPARATOR = " "
        private const val CMD_PART_OVERRIDE = "-y"
        private const val CMD_PART_INPUT = "-i \"%s\""
        private const val CMD_PART_OUTPUT = "\"%s\""
        private const val CMD_PART_SPLIT_START = "-ss %s"
        private const val CMD_PART_SPLIT_END = "-to %s"
        private const val CMD_PART_VIDEO_CODEC = "-c:v %s"
        private const val CMD_PART_AUDIO_CODEC = "-c:a %s"

        private fun getSecondsString(time: Long): String {
            val seconds = TimeUnit.MILLISECONDS.toSeconds(time)
            val ms = time - TimeUnit.SECONDS.toMillis(seconds)
            return String.format("%02d.%03d", seconds, ms)
        }
    }

    private var commandRunning = false

    @WorkerThread
    override fun split(
        inputFilePath: String,
        outputFilePath: String,
        startTime: Long,
        endTime: Long,
        state: MutableLiveData<FFMpegUtil.State>
    ) {
        Log.d(
            TAG,
            "split() called with: " +
                    "inputFilePath = $inputFilePath, " +
                    "outputFilePath = $outputFilePath, " +
                    "startTime = $startTime, " +
                    "endTime = $endTime"
        )
        val audioQualityLowDefault = "aac -qscale:a 9"
        val videoQualityLowDefault = "libx264 -profile:v baseline -level 3.0 -pix_fmt yuv420p -preset faster"
        val inputPart = String.format(CMD_PART_INPUT, inputFilePath)
        val outputPart = String.format(CMD_PART_OUTPUT, outputFilePath)
        val startPart = String.format(CMD_PART_SPLIT_START, getSecondsString(startTime))
        val endPart = String.format(CMD_PART_SPLIT_END, getSecondsString(endTime))
        val videoCodecPart = String.format(CMD_PART_VIDEO_CODEC, videoQualityLowDefault)
        val audioCodecPart = String.format(CMD_PART_AUDIO_CODEC, audioQualityLowDefault)
        val totalTime = (endTime - startTime).toInt()

        state.postValue(FFMpegUtil.State(FFMpegUtil.Status.IN_PROGRESS, 0, totalTime))

        val command = buildCommand(
            CMD_PART_OVERRIDE,
            inputPart,
            startPart,
            endPart,
            videoCodecPart,
            audioCodecPart,
            outputPart
        )
        Log.d(TAG, "split: command = $command")

        Config.enableStatisticsCallback(null)
        Config.resetStatistics()
        Config.enableStatisticsCallback { newStatistics ->
            Log.d(TAG, "statisticsCallback() called with: time = ${newStatistics.time}")
            state.postValue(
                FFMpegUtil.State(
                    FFMpegUtil.Status.IN_PROGRESS,
                    newStatistics.time,
                    totalTime
                )
            )
        }
        commandRunning = true
        val rc = FFmpeg.execute(command)
        commandRunning = false
        when (rc) {
            RETURN_CODE_SUCCESS -> state.postValue(
                FFMpegUtil.State(FFMpegUtil.Status.SUCCESS)
            )
            else -> state.postValue(
                FFMpegUtil.State(FFMpegUtil.Status.FAILED)
            )
        }
    }

    override fun getOutputExt(): String = OUTPUT_EXT

    override fun abort() {
        if (commandRunning) {
            FFmpeg.cancel()
            commandRunning = false
        }
    }

    private fun buildCommand(vararg parts: String): String {
        Log.d(TAG, "buildCommand() called with: parts = $parts")
        return parts.joinToString(separator = CMD_PART_SEPARATOR)
    }
}