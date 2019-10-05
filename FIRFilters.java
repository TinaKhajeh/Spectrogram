package com.example.audio_recorder;



public class FIRFilters {
	  double window [] =new double[1024];
	  private Window windowType;
	  public enum Window {
		  RECTANGULAR, HAMMING, HANN, BLACKMAN, KAISER
		}
	  //constructor to initialize window type and window
	  public FIRFilters(Window windowType) {
			super();
			this.windowType = windowType;
			setWindow();
		}
	  
		
		public void setWindow() {
			Window windowType = this.windowType;
			if(windowType==Window.RECTANGULAR){
		    	 RectangularWindow();
		     }
		     else if(windowType==Window.HANN){
		    	 HannWindow();
		     }
		     else if(windowType==Window.HAMMING){
		    	 HammingWindow();
		     }
		     else if(windowType==Window.BLACKMAN){
		    	 BlackmanWindow();
		     }
		     else if(windowType==Window.KAISER){
		    	 KaiserFromAttenuation(60,8000);
		     }
			System.out.println("window changed ");
		}
	  public void setWindowType(Window type){//set new type for window
		  this.windowType=type;
	  }
	  
	  public Window getWindowType(){//get current value of window
		  return this.windowType;
	  }
	  
	   public void RectangularWindow(){
		   for (int i = 0; i < window.length; i++) {
			window[i]=1;
		}
	   }
	   
	   public void HammingWindow(){
		   double alpha=0.54;
		   double beta=1-alpha;
		   for (int i = 0; i < window.length; i++) {
			window[i]=alpha-beta*Math.cos((2.0*Math.PI*i)/(window.length-1));
		}
	   }
	   
	   public void HannWindow(){
		   for (int i = 0; i < window.length; i++) {
			window[i]=0.5*(1-Math.cos(2*Math.PI*i/(window.length-1)));
		}
	   }
	   /**
	    * Blackman window with the specified length
	    */
	   public void BlackmanWindow(){
		   double alpha=0.16;
		   double a0=1-alpha/(2*1.0);
		   double a1=0.5;
		   double a2=alpha/(2*1.0);
		   for (int i = 0; i < window.length; i++) {
			window[i] = a0-a1*Math.cos(2*Math.PI*i/(window.length-1))+a2*Math.cos(4*Math.PI*i/(window.length-1));
		}  
	   }
	   
	   /**
	     * Zeroth order modified Bessel function.
	     *
	     * @param x
	     *            Value.
	     * @return Return value.
	     */
	    public final static double i0(final double x)
	    {
	        double f = 1;
	        final double x2 = x * x * 0.25;
	        double xc = x2;
	        double v = 1 + x2;
	        for (int i = 2; i < 100; i++)
	        {
	            f *= i;
	            xc *= x2;
	            final double a = xc / (f * f);
	            v += a;
	            if (a < 1e-20) break;
	        }
	        return v;
	    }
	   
	   /**
	     * Applies a Kaiser window with given size and attenuation to the given FIR filter.
	     *
	     * @param fir
	     *            The FIR filter.
	     * @param attenuation
	     *            Attenuation in dB.
	     * @param fs
	     *            Sampling frequency.
	     * @return The windowed FIR filter.
	     */
	    public void KaiserFromAttenuation(final double attenuation, final double fs)
	    {
	        final int m = window.length;
	        final double beta;
	        if (attenuation <= 21)
	            beta = 0;
	        else if (attenuation <= 50)
	            beta = 0.5842 * Math.pow(attenuation - 21, 0.4) + 0.07886 * (attenuation - 21);
	        else
	            beta = 0.1102 * (attenuation - 8.7);

	        final double i0b = i0(beta);

	        for (int n = 0; n < m; n++)
	        {
	            final double v = beta * Math.sqrt(1.0 - Math.pow(2.0 * n / (m - 1) - 1.0, 2));
	            window[n] *= i0(v) / i0b;
	        }   
	    }

	  
}
