package com.example.audio_recorder;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;


class Recorder {
	
	//!! ye constructor besaz vasash
	//!! behtare hast ke in feild ha private bashan o seter o geter dashte bashan
	int RATE;
	AudioRecord recorder = null;
	boolean isRecording = false;
	int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    
    
    /** 
     * Create a new AudioRecord instance for recording process. initiate sampling rate, audio format and channel config to create audio instance
     * for recorder device
	 * 
	 */
    public AudioRecord findAudioRecord() {
		int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
	    for (int rate : mSampleRates) {
	    	//PCM 16 bit per sample. Guaranteed to be supported by devices
	        for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
	            for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {//
	                try {
	                    int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);//8000,AudioFormat.ENCODING_PCM_16BIT,
	                    //Returns the minimum buffer size required for the successful creation of an AudioRecord object, in byte units
	                    
	                    if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
	                        // check if we can instantiate and have a success
	                        AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);//?? in ham check kon
	                        if (recorder.getState() == AudioRecord.STATE_INITIALIZED){
	                        	RATE=rate;
	                            return recorder;
	                        }
	                    }
	                } catch (Exception e) {
	                    //Log.e(C.TAG, rate + "Exception, keep trying.",e);
	                }
	            }
	        }
	    }
	    return null;
	}
	
	
	/***
	 * get samples from Recorder 
	 * @return
	 * 			short[] as output
	 */			
	protected short[] getshamples(){
		short sData[] = new short[BufferElements2Rec];
		int as =(this.recorder).read(sData, 0, BufferElements2Rec);
		return sData;
	}
	/***
	 * Type conversion from Short to Double
	 * @param sData
	 * @return
	 */
	public double[] convertShortArrayToDouble(short [] sData){
		double real [] = new double[BufferElements2Rec];
		for (int i = 0; i < sData.length; i++) {
        	real [i] = (double) sData[i];
    	}
		return real;
	}
	
	
	/***
	 * Starts the recording activity
	 * @return
	 * 			
	 */
	public boolean startRecording() {
    	this.recorder = this.findAudioRecord();
    	if(this.recorder !=  null){
    		(this.recorder).startRecording();//Starts recording from the AudioRecord instance.
    		this.isRecording = true;
        	return true;
    	}
    	else{
    		return false;
    	}
    	
    	}
    /***
     * stops the recording activity
     */
    public void stopRecording() {
    	if (null != this.recorder) {
    		this.isRecording = false;

    	 
    	(this.recorder).stop();
    	(this.recorder).release();

    	this.recorder = null;
    	
    	}
    	}
	
	
}
