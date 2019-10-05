package com.example.audio_recorder;

public class STFT {
	static int Fs;
	public STFT(int rate){
		this.Fs=rate;
	}
	public static double[] CreateSimpleWindow(int windowSize){
		double [] win = new double [windowSize];
		for (int i = 0; i < win.length; i++) {
			win[i]=1;
		}
		return win;
	}
	
	public static double [] createHammingWindow(int N, int alpha, int beta){//alpha= 0.54 & beta=0.46
		double [] window = new double [N];
		for (int i = 0; i < N; i++) {
			window[i]=alpha-beta*Math.cos(2*Math.PI*i*1.0/(N-1));
		}
		return window;
	}
	
	public static double [][] getSTFT (double []sample, int nfft){//sample bayad sazesh daghighan be andaze window ya masalan 1024 bashad
		double [][] result = new double [nfft][3];
		Fft fft = new Fft(nfft);
		double [] window = CreateSimpleWindow(nfft);
		double [] real =  new double [nfft];
		double [] img =  new double [nfft];
		for (int i = 0; i < real.length; i++) {
			real[i] = sample [i] * window[i];
			img[i] = 0;
		}
		fft.fft(real, img);
		double[] mag = new double [nfft];
		for(int i=0; i<mag.length; i++){
		mag[i] = 10*Math.log(real[i]*real[i] + img[i]*img[i] );
		double freq = i*Fs/nfft;//nfft hamun size panjare hast ke male man 1024 hast
		double time=nfft*1.0/Fs;
		result[i][0]=time;
		result[i][1]=freq;
		result[i][2]=mag[i];//pas baghiye ferq ha chi mishan?!
		}
		return result;
		
	}
}
