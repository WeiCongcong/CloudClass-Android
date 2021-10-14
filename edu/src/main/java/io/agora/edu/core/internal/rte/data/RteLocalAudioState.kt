package io.agora.edu.core.internal.rte.data

import io.agora.rtc2.IRtcEngineEventHandler


enum class RteLocalAudioState(val value: Int) {
    LOCAL_AUDIO_STREAM_STATE_STOPPED(0),
    LOCAL_AUDIO_STREAM_STATE_CAPTURING(1),
    LOCAL_AUDIO_STREAM_STATE_ENCODING(2),
    LOCAL_AUDIO_STREAM_STATE_FAILED(3);

    companion object {
        fun convert(value: IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_STATE): Int {
            return when (value) {
                IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_STATE.LOCAL_AUDIO_STREAM_STATE_STOPPED -> {
                    LOCAL_AUDIO_STREAM_STATE_STOPPED.value
                }
                IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_STATE.LOCAL_AUDIO_STREAM_STATE_RECORDING -> {
                    LOCAL_AUDIO_STREAM_STATE_CAPTURING.value
                }
                IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_STATE.LOCAL_AUDIO_STREAM_STATE_ENCODING -> {
                    LOCAL_AUDIO_STREAM_STATE_ENCODING.value
                }
                IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_STATE.LOCAL_AUDIO_STREAM_STATE_FAILED -> {
                    LOCAL_AUDIO_STREAM_STATE_FAILED.value
                }
                else -> {
                    LOCAL_AUDIO_STREAM_STATE_STOPPED.value
                }
            }
        }
    }
}

enum class RteLocalAudioError(val value: Int) {
    LOCAL_AUDIO_STREAM_ERROR_OK(0),
    LOCAL_AUDIO_STREAM_ERROR_FAILURE(1),
    LOCAL_AUDIO_STREAM_ERROR_DEVICE_NO_PERMISSION(2),
    LOCAL_AUDIO_STREAM_ERROR_DEVICE_BUSY(3),
    LOCAL_AUDIO_STREAM_ERROR_CAPTURE_FAILURE(4),
    LOCAL_AUDIO_STREAM_ERROR_ENCODE_FAILURE(5);

    companion object {
        fun convert(value: IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_ERROR): Int {
            return when (value) {
                IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_ERROR.LOCAL_AUDIO_STREAM_ERROR_OK -> {
                    LOCAL_AUDIO_STREAM_ERROR_OK.value
                }
                IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_ERROR.LOCAL_AUDIO_STREAM_ERROR_FAILURE -> {
                    LOCAL_AUDIO_STREAM_ERROR_FAILURE.value
                }
                IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_ERROR.LOCAL_AUDIO_STREAM_ERROR_DEVICE_NO_PERMISSION -> {
                    LOCAL_AUDIO_STREAM_ERROR_DEVICE_NO_PERMISSION.value
                }
                IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_ERROR.LOCAL_AUDIO_STREAM_ERROR_DEVICE_BUSY -> {
                    LOCAL_AUDIO_STREAM_ERROR_DEVICE_BUSY.value
                }
                IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_ERROR.LOCAL_AUDIO_STREAM_ERROR_RECORD_FAILURE -> {
                    LOCAL_AUDIO_STREAM_ERROR_CAPTURE_FAILURE.value
                }
                IRtcEngineEventHandler.LOCAL_AUDIO_STREAM_ERROR.LOCAL_AUDIO_STREAM_ERROR_ENCODE_FAILURE -> {
                    LOCAL_AUDIO_STREAM_ERROR_ENCODE_FAILURE.value
                }
                else -> {
                    LOCAL_AUDIO_STREAM_ERROR_OK.value
                }
            }
        }
    }
}