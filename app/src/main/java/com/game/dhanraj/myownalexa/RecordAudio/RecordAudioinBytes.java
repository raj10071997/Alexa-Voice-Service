package com.game.dhanraj.myownalexa.RecordAudio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;

/**
 * Created by Dhanraj on 03-06-2017.
 */

public class RecordAudioinBytes extends AudioRecord {
    /**
     * Class constructor.
     * Though some invalid parameters will result in an {@link IllegalArgumentException} exception,
     * other errors do not.  Thus you should call {@link #getState()} immediately after construction
     * to confirm that the object is usable.
     *
     * @param audioSource       the recording source.
     *                          See {@link MediaRecorder.AudioSource} for the recording source definitions.
     * @param sampleRateInHz    the sample rate expressed in Hertz. 44100Hz is currently the only
     *                          rate that is guaranteed to work on all devices, but other rates such as 22050,
     *                          16000, and 11025 may work on some devices.
     *                          {@link AudioFormat#SAMPLE_RATE_UNSPECIFIED} means to use a route-dependent value
     *                          which is usually the sample rate of the source.
     *                          {@link #getSampleRate()} can be used to retrieve the actual sample rate chosen.
     * @param channelConfig     describes the configuration of the audio channels.
     *                          See {@link AudioFormat#CHANNEL_IN_MONO} and
     *                          {@link AudioFormat#CHANNEL_IN_STEREO}.  {@link AudioFormat#CHANNEL_IN_MONO} is guaranteed
     *                          to work on all devices.
     * @param audioFormat       the format in which the audio data is to be returned.
     *                          See {@link AudioFormat#ENCODING_PCM_8BIT}, {@link AudioFormat#ENCODING_PCM_16BIT},
     *                          and {@link AudioFormat#ENCODING_PCM_FLOAT}.
     * @param bufferSizeInBytes the total size (in bytes) of the buffer where audio data is written
     *                          to during the recording. New audio data can be read from this buffer in smaller chunks
     *                          than this size. See {@link #getMinBufferSize(int, int, int)} to determine the minimum
     *                          required buffer size for the successful creation of an AudioRecord instance. Using values
     *                          smaller than getMinBufferSize() will result in an initialization failure.
     * @throws IllegalArgumentException
     */


  //  private final int mSampleRate;
    private final int RESOLUTION_IN_BYTES=2;
    private final int CHANNELS = 1;
    private final int mOneSec;
    private final byte[] mRecording;
    private static final int BUFFER_SIZE_MUTLIPLIER=4;
    private byte[] mBuffer;
    private int mRecordedLength = 0;
    private double mAvgEnergy = 0;

    // The number of bytes the client has already consumed
    private int mConsumedLength = 0;



   /* public RecordAudioinBytes(int audioSource,int sampleRate)
    {
        mSampleRate = sampleRate;
        mOneSec = RESOLUTION_IN_BYTES*CHANNELS*mSampleRate;

        mRecording = new byte[mOneSec*35];

        int bufferSize = getBufferSize();
        int framePeriod = bufferSize / (2 * RESOLUTION_IN_BYTES * CHANNELS);

        this(audioSource,sampleRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,bufferSize);
        //this(audioSource,sampleRate)
    }*/

    public RecordAudioinBytes() throws IllegalArgumentException {
        super(RecorderConstants.AudioSource, RecorderConstants.mSampleRate,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,RecorderConstants.bufferSize);

        mOneSec = RESOLUTION_IN_BYTES*CHANNELS*RecorderConstants.mSampleRate;

        mRecording = new byte[mOneSec*35];

          //   int bufferSize = getBufferSize();
         //   int framePeriod = bufferSize / (2 * RESOLUTION_IN_BYTES * CHANNELS);
        //    createRecorder(audioSource, sampleRateInHz, bufferSize);
        checkthingsforrecoder();
        mBuffer = new byte[RecorderConstants.framePeriod*RESOLUTION_IN_BYTES*CHANNELS];

    }

    public void start()
    {
        if(getState()==AudioRecord.STATE_INITIALIZED)
        {
            startRecording();
            if(getRecordingState()==AudioRecord.RECORDSTATE_RECORDING)
            {
                new Thread() {
                    public void run() {
                        recorderLoop();
                    }
                }.start();
            }  else {
            Log.e("startRecording()"," failed");
            }
        } else {
            Log.e("start()"," called on illegal state");
        }

    }



    private void recorderLoop() {
        while (getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            int status = read(mBuffer);
            if (status < 0) {
                Log.e("status = ", String.valueOf(status));
                break;
            }
        }
    }

    protected int read(byte[] buffer)
    {
        int len = buffer.length;
        //the read function below is responsible for writing the audio data in the byte array
        int numOfBytes = read(buffer, 0, len);
        int status = getStatus(numOfBytes, len);
        if (status == 0 && numOfBytes >= 0) {
            // arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
            // numOfBytes <= len, typically == len, but at the end of the recording can be < len.
            System.arraycopy(buffer, 0, mRecording, mRecordedLength, numOfBytes);
            mRecordedLength += len;
        }
        return status;
    }

    private void checkthingsforrecoder() {
        int audioSessionId = getAudioSessionId();

        if(NoiseSuppressor.isAvailable())
        {
          //  NoiseSuppressor.create(audioSessionId);
        }
        if(AutomaticGainControl.isAvailable())
        {
           // AutomaticGainControl.create(audioSessionId);
        }
        if(AcousticEchoCanceler.isAvailable()){
           // AcousticEchoCanceler.create(audioSessionId);
        }
    }

    protected int getStatus(int numOfBytes, int len) {
      //  Log.i("Read bytes: request/actual: " + len + "/" + numOfBytes);
        if (numOfBytes < 0) {
        //    Log.e("AudioRecord error: " + numOfBytes);
            return numOfBytes;
        }
        if (numOfBytes > len) {
          //  Log.e("Read more bytes than is buffer length:" + numOfBytes + ": " + len);
            return -100;
        } else if (numOfBytes == 0) {
            //Log.e("Read zero bytes");
            return -200;
        } else if (mRecording.length < mRecordedLength + numOfBytes) {
           // Log.e("Recorder buffer overflow: " + mRecordedLength);
            return -300;
        }
        return 0;
    }

    //don't know the reason
    public boolean isPausing() {
        double pauseScore = getPauseScore();
      //  Log.i("Pause score: " + pauseScore);
        return pauseScore > 7;
    }
    //don't know the reason
    private double getPauseScore() {
        long t2 = getRms(mRecordedLength, mOneSec);
        if (t2 == 0) {
            return 0;
        }
        double t = mAvgEnergy / t2;
        mAvgEnergy = (2 * mAvgEnergy + t2) / 3;
        return t;
    }
    //don't know the reason
    private long getRms(int end, int span) {
        int begin = end - span;
        if (begin < 0) {
            begin = 0;
        }
        // make sure begin is even
        if (0 != (begin % 2)) {
            begin++;
        }

        long sum = 0;
        for (int i = begin; i < end; i += 2) {
            short curSample = getShort(mRecording[i], mRecording[i + 1]);
            sum += curSample * curSample;
        }
        return sum;
    }
    //don't know the reason
    private static short getShort(byte argB1, byte argB2) {
        return (short) (argB1 | (argB2 << 8));
    }
//don't know the reason and what is the data type of it-synchronized????
    public synchronized byte[] consumeRecording() {
        byte[] bytes = getCurrentRecording(mConsumedLength);
        mConsumedLength = mRecordedLength;
        return bytes;
    }
    protected byte[] getCurrentRecording(int startPos) {
        int len = getLength() - startPos;
        byte[] bytes = new byte[len];
        System.arraycopy(mRecording, startPos, bytes, 0, len);
       // Log.i("Copied from: " + startPos + ": " + bytes.length + " bytes");
        return bytes;
    }

    public int getLength() {
        return mRecordedLength;
    }



    protected void createRecorder(int audioSource, int sampleRateInHz, int bufferSize) {
    }

    public float getRmsdb() {
        long sumOfSquares = getRms(mRecordedLength, mBuffer.length);
        double rootMeanSquare = Math.sqrt(sumOfSquares / (mBuffer.length / 2));
        if (rootMeanSquare > 1) {
            // TODO: why 10?
            return (float) (10 * Math.log10(rootMeanSquare));
        }
        return 0;
    }



}
