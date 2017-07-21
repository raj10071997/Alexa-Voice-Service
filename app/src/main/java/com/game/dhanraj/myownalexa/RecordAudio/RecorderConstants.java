package com.game.dhanraj.myownalexa.RecordAudio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by Dhanraj on 03-06-2017.
 */

public class RecorderConstants {

    public static int mSampleRate;
    public static int RESOLUTION_IN_BYTES=2;
    public static int CHANNELS = 1;
//    public static int mOneSec;
//    public static byte[] mRecording=null;
    public static int BUFFER_SIZE_MUTLIPLIER=4;
    public static int framePeriod;
    public static int bufferSize;
    public static int AudioSource;


    public RecorderConstants(int sampleRate)
    {
        this.AudioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
        mSampleRate=sampleRate;
        bufferSize = getBufferSize();
        framePeriod = bufferSize / (2 * RESOLUTION_IN_BYTES * CHANNELS);
    }



    private int getBufferSize(){
        //the function below gives min buffer size which is necesaary for audio recording
        int minBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (minBufferSizeInBytes == AudioRecord.ERROR_BAD_VALUE) {
            throw new IllegalArgumentException("SpeechRecord.getMinBufferSize: parameters not supported by hardware");
        } else if (minBufferSizeInBytes == AudioRecord.ERROR) {
            // Log.e("SpeechRecord.getMinBufferSize: unable to query hardware for output properties");
            minBufferSizeInBytes = mSampleRate * (120 / 1000) * RESOLUTION_IN_BYTES * CHANNELS;
        }
        //buffer_size_multiplier=4 ka reason nhi pata hai
        int bufferSize = BUFFER_SIZE_MUTLIPLIER * minBufferSizeInBytes;
        //Log.i("SpeechRecord buffer size: " + bufferSize + ", min size = " + minBufferSizeInBytes);
        return bufferSize;
    }

}
