package com.example.audio_recorder;

import com.jjoe64.graphview.GraphView.GraphViewData;


public class Fft { 
	  private double alpha;
	 
	  public void setAlpha(double value){//set new value for alpha
		  this.alpha=value;
	  }
	  
	  public double getAlpha(){//get current value of alpha
		  return this.alpha;
	  }
	  
	   int n, m;  
	  
	   double[] cos;  
	   double[] sin;  
	  
	     public Fft(int n) {
	     this.n = n;  
	     this.m = (int)(Math.log(n) / Math.log(2)); // m = numbers of iterations to convert time domain to freq domain
	     this.alpha=0.7;//default value
	       
	     cos = new double[n/2];  
	     sin = new double[n/2];  
	  
	     for(int i=0; i<n/2; i++) {  
	       cos[i] = Math.cos(-2.0*Math.PI*i*1.0/n*1.0);  
	       sin[i] = Math.sin(-2.0*Math.PI*i*1.0/n*1.0);
	       
	     }  
	   }  

	     /***
	      * calculate FFT transform(convert time domain to frequency domain) samples stroed in x and y vector
	      * @param x
	      * 		real part of time domain
	      * @param y
	      * 		imaginary part of time domain
	      * 
	      */
	   public void fft(double[] x, double[] y)  
	   {  
	     int i,j,k,n1,n2,a;  
	     double c,s,t1,t2;  
	      
	     j = 0;  
	     n2 = n/2;  
	     for (i=1; i < n - 1; i++) {  
	       n1 = n2;  
	       while ( j >= n1 ) {  
	         j = j - n1;  
	         n1 = n1/2;  
	       }  
	       j = j + n1;  
	       
	       if (i < j) {  
	    	   //exchange elements in index i and j
	         t1 = x[i];  
	         x[i] = x[j];  
	         x[j] = t1;  
	         t1 = y[i];  
	         y[i] = y[j];  
	         y[j] = t1;  
	       }  
	     }  
	  
	     // FFT  
	     n1 = 0;  
	     n2 = 1;  
	     //m = number of iterations 
	     for (i=0; i < m; i++) {  
	       n1 = n2;  
	       n2 = n2 + n2;  
	       a = 0;  
	  
	       for (j=0; j < n1; j++) {  
	         c = cos[a];  
	         s = sin[a];  
	         a +=  1 << (m-i-1);  
	         
	         for (k=j; k < n; k=k+n2) {  
	           t1 = c*x[k+n1] - s*y[k+n1];  
	           t2 = s*x[k+n1] + c*y[k+n1];  
	           x[k+n1] = x[k] - t1;  
	           y[k+n1] = y[k] - t2;  
	           x[k] = x[k] + t1;  
	           y[k] = y[k] + t2;  
	         }  
	       }  
	     }  
	   }  
	  
	 
	   /***
	    * function which produces combination of input window n and n-1 as a processing window for time n
	    * @param Xnow
	    * 			samples gather from microphone now
	    * @param Y
	    * 			samples which are combinations of Xnow and also previous Y and construct Y for now
	    */
	    public void filter(double []Xnow, double [] Y){//This function will smooth the changes of a signal in time 
	    	//domain with merge received signal with it's history.
	    	int len = Xnow.length;
	    	//alpha = recall factor
	    	double alpha = this.getAlpha();
	    	for (int i = 0; i < len; i++) {
				Y[i] = (alpha)*Xnow[i]+(1-alpha)*Y[i];//signal is in the Frequency domain.
			}		
	    }
	    /**
	     * 
	     * @param real
	     * @param img
	     * @return
	     * after using FFT to find max frequency use this function
	     */
	    public int findMaxFrequency(double [] real, double [] img){//This Function return the index of max frequnecy
	    	double max = Math.sqrt(Math.pow(real[0],2)+Math.pow(img[0],2));
    		int maxIndex=0;
    		for (int i = 0; i < img.length; i++) {
    			double tmp = Math.sqrt(Math.pow(real[i],2)+Math.pow(img[i],2));
				if(tmp>max){
					max=tmp;
					maxIndex=i;
				}
			}
    		return maxIndex*8000/1024;
	    }

	}  