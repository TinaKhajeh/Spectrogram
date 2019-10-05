package com.example.audio_recorder;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;


public class MainActivity extends ActionBarActivity {
    Recorder rec = new Recorder();//
    int  maxDisplay = rec.BufferElements2Rec/2+1;//max number of DFT points will to display
    int seekLevel = 3;//level of recall rate is set to 3th level corresponds to 0.3
    
    GraphView.GraphViewData[] data = new GraphView.GraphViewData[maxDisplay - 1];
    int programState =0 ;//flag hold program state. 0: not started, 1:started before and now stops, 2:start
    
    Fft fft=new Fft(rec.BufferElements2Rec); //fft object(rec.BufferElements2Rec point fft object)
    FIRFilters FIRFilt = new FIRFilters (FIRFilters.Window.RECTANGULAR);
    
    private Thread recordingThread;
    private final Handler mHandler = new Handler();
    private Runnable refresher;
    private boolean help=false;


    @Override
    public void onCreate(Bundle savedInstanceState) {//on create method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
        programState = 0;
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    /***
     * Handle action bar item clicks in this function. The action bar will automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //shows help message for user
        if (id == R.id.help) {
            help=true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(Html.fromHtml(getString(R.string.help)));
            builder.setTitle("Help");
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        //shows options for window
        else if(id == R.id.window){
        	final Dialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Window Options");
            String[] items = new String[] { "Rectangle", "Hamming", "Hann", "Blackman", "Kiaser" };
            int selected =0;
            selected = (FIRFilt.getWindowType()).ordinal();
            builder.setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                	String t = "";
                    switch(item)
                    {
                        case 0:
                        	FIRFilt.setWindowType(FIRFilters.Window.RECTANGULAR);
                        	t = "Rectangle";
                    		break;
                        case 1:
                        	FIRFilt.setWindowType(FIRFilters.Window.HAMMING);
                        	t = "Hamming";
                    		break;
                        case 2:
                        	FIRFilt.setWindowType(FIRFilters.Window.HANN);
                        	t = "Hann";
                    		break;
                        case 3:
                        	FIRFilt.setWindowType(FIRFilters.Window.BLACKMAN);
                        	t = "Blackman";
                    		break;
                        case 4:
                        	FIRFilt.setWindowType(FIRFilters.Window.KAISER);
                        	t = "Kiaser";
                    		break;
                    	default:
                    		FIRFilt.setWindowType(FIRFilters.Window.RECTANGULAR);
                    		break;  
                    }
                    FIRFilt.setWindow();
                    Toast.makeText(getApplicationContext(), "Window type changed to: " + t, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();   
                    }
                });
            dialog = builder.create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Check screen orientation or screen rotate event here
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen for landscape and portrait
        if ((programState==2) && (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            enableButtons(false);
            mHandler.removeCallbacks(refresher);
            rec.stopRecording();
            SeekBar seek = (SeekBar) findViewById(R.id.seekBar1);
        	seekLevel = seek.getProgress();
            if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            	setContentView(R.layout.activity_main_land);
            }
            else {
            	setContentView(R.layout.activity_main);
            }
            setup();
        	seek = (SeekBar) findViewById(R.id.seekBar1);
        	seek.setProgress(seekLevel);
            enableButtons(true);
            boolean result = rec.startRecording();
            if (result == true) {
                recordingThread = new Thread(new Runnable() {
                    public void run() {
                        Process(rec);
                    }
                }, "AudioRecorder Thread");
                recordingThread.start();
            }   
        }
        else if(programState!=2) {
        	SeekBar seek = (SeekBar) findViewById(R.id.seekBar1);
        	seekLevel = seek.getProgress();
        	if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            	setContentView(R.layout.activity_main_land);
            }
            else {
            	setContentView(R.layout.activity_main);
            }
        	setup();
        	seek = (SeekBar) findViewById(R.id.seekBar1);
        	seek.setProgress(seekLevel);
        }
    }
    
    // onClick of backbutton finishes the activity.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
    	if(programState==2){
    		mHandler.removeCallbacks(refresher);
            rec.stopRecording();
    	}
    	finish();
    }
    return super.onKeyDown(keyCode, event);
    }
    
    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            Button b = (Button)v;
            String buttonText = b.getText().toString();
            if(buttonText == "Start"){
                programState = 2;
                enableButtons(true);//buttons changed to Stop
                boolean result = rec.startRecording();
                if(result==true){
                    recordingThread = new Thread(new Runnable() {
                        public void run() {
                            Process(rec);
                        }
                    }, "AudioRecorder Thread");
                    recordingThread.start();
                }
            }
            else if(buttonText=="Stop"){
                programState = 1;
                enableButtons(false);
                mHandler.removeCallbacks(refresher);
                rec.stopRecording();
            }
        }
    };
    
    /**
     * setup is a function which shows the state of program if it is started or not and set all for the first time
     */
    public void setup() {
    	//setup graphView
    	final GraphView graphView = new LineGraphView(this, "");
        graphView.setManualYAxisBounds(140, 0);
        graphView.setScrollable(true);
        graphView.setScalable(true);
        graphView.getGraphViewStyle().setGridStyle(GraphViewStyle.GridStyle.BOTH);
        FrameLayout layout = (FrameLayout) findViewById(R.id.layout);
        graphView.getGraphViewStyle().setTextSize(11);
        final GraphViewSeries graphViewSeries = new GraphViewSeries(new GraphView.GraphViewData[] { new GraphView.GraphViewData(1, 2) });
        graphView.addSeries(graphViewSeries);
        GraphView.GraphViewData[] data2 = new GraphView.GraphViewData[ maxDisplay];
        for (int i = 0; i < maxDisplay; i++) {
            double index = (i*8000*1.0)/(rec.BufferElements2Rec*1000.0);//index hold the ferequncy of each point, fs = 8000
            index = round(index, 2);
            data2[i] = new GraphView.GraphViewData(index, 0);//data2 holds magnitude of each ferequency when not started (all values are zero)
        }
        if(programState == 0)
        	graphViewSeries.resetData(data2);//all values will be set to zero at start
        else
        	graphViewSeries.resetData(data);//after rotation we will need to display older values for each frequency
        layout.addView(graphView);
        
        // //setup seekbar
        SeekBar seekBar =(SeekBar) findViewById(R.id.seekBar1);
        seekBar.setProgress(3);//initial value of Recall Rate is 0.3
        final TextView textView = (TextView) findViewById(R.id.textView1);
        textView.setText("Recall Rate: 0.3");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//function execution is by changing the seekbar level
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;//progress value is between 0 to 10
                double value =progress*1.0/10.0;//to map it between the interval 0 to 0.99
                if(value == 1){
                    value=0.99;
                }
                textView.setText("Recall Rate: " + Double.toString(value));
                fft.setAlpha( 1.0-value);//set alpha
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        //setup buttons
        setButtonHandlers();
        enableButtons(false);//start will appear and stop will hide
        
    }
    
    
    


    private void setButtonHandlers() {
        ((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
    }

    /**
     * receive a boolean and  set appropriate lable (Start/Stop) for the Button in GUI
     * @param isRecording
     */
    private void enableButtons(boolean isRecording) {
        if(isRecording == false){
            ((Button) findViewById(R.id.btnStart)).setText("Start");
        }
        else{
            ((Button) findViewById(R.id.btnStart)).setText("Stop");
        }

    }


    /**
     * function which convert a short array to double
     * @param sData
     * @param size
     * @return
     */
    public double[] convertShortArrayToDouble(short [] sData, int size){
        double real [] = new double[size];
        for (int i = 0; i < sData.length; i++) {
            real [i] = (double) sData[i];
        }
        return real;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;   
    }
    
    
    private void Process(final Recorder rec) {
        final double[] YImg = new double[rec.BufferElements2Rec];
        final double[] YReal = new double[rec.BufferElements2Rec];
        final GraphView graphView = new LineGraphView(this, "");
        graphView.setManualYAxisBounds(140, 0);
        graphView.setScrollable(true);
        graphView.setScalable(true);
        graphView.getGraphViewStyle().setGridStyle(GraphViewStyle.GridStyle.BOTH);
        final FrameLayout layout = (FrameLayout) findViewById(R.id.layout);
        graphView.getGraphViewStyle().setTextSize(11);
        final GraphViewSeries graphViewSeries = new GraphViewSeries(new GraphView.GraphViewData[] { new GraphView.GraphViewData(1, 2) });
        graphView.addSeries(graphViewSeries);
        GraphView.GraphViewData[] data2 = new GraphView.GraphViewData[ maxDisplay];
        for (int i = 0; i < maxDisplay; i++) {    
            double index = (i*8000*1.0)/(rec.BufferElements2Rec*1000.0);//index showes frequency 
            index = round(index, 2);
            data2[i] = new GraphView.GraphViewData(index, 0);//data2 holds magnitude of each ferequency for the first time which is zero for all values
        }
        graphViewSeries.resetData(data2);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layout.removeAllViews();
                graphView.setScrollable(true);
                graphView.setScalable(true);
                layout.addView(graphView);   
            }
        });
        refresher = new Runnable() {
            public void run() {
                short sData[] = new short[rec.BufferElements2Rec];
                double real[] = new double[rec.BufferElements2Rec];
                sData = rec.getshamples();
                real = convertShortArrayToDouble(sData, rec.BufferElements2Rec);
                double img[] = new double[rec.BufferElements2Rec];//set imaginary part of the audio signal to zero
                for (int index = 0; index < real.length; index++) {
        			real[index] = real[index]* FIRFilt.window[index];
        		}
                fft.fft(real, img);//take fft
                //use recall factor
                fft.filter(img, YImg);
                fft.filter(real, YReal);
                int display=0;//hold maximum size for displaing on the screen
                
                if(rec.BufferElements2Rec%2==0){
                    display=rec.BufferElements2Rec/2;
                }
                else{
                    display=rec.BufferElements2Rec/2+1;
                }
                int LOUDNESS_BIAS = 50;
                for (int i = 0; i < display; i++) {
                    double tmp = Math.sqrt(Math.pow(YReal[i], 2)+ Math.pow(YImg[i], 2));//tmp holds magnitude of signal
                    double tmp2 = 20 * Math.log10(tmp);//tmp2 holds magnitude of signal in db
                    double index = (i*8000*1.0)/(rec.BufferElements2Rec*1000.0);//calculate the ferequency for each point
                    index = round(index, 2);
                    double tmp3 = tmp2-LOUDNESS_BIAS;//scale magnitude with respect to the characteristics of audio signals
                    if(tmp3>140)
                        tmp3=140;
                    else if(tmp3<0)
                        tmp3=0;
                    data[i] = new GraphView.GraphViewData(index, tmp3);
                }
                graphViewSeries.resetData(data);
                mHandler.postDelayed(this, 3);//draw it with a delay
            }
        };
        mHandler.postDelayed(refresher, 10);
    }

}
