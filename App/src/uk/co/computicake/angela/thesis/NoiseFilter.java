package uk.co.computicake.angela.thesis;

/**
 * Low pass filters for reducing sensor noise.
 */
public class NoiseFilter {
	private static final float STILL_NOISE = (float) 0.1; 
	// Smoothing constants for low-pass filter. Smaller values mean more smoothing.
	private static final float ALPHA = 0.75f;
	private static final float ALPHA_Z = 1f;

	/**
	 * Low pass filter applied to raw sensor data.
	 * @param input		the new sensor values, as [x,y,z] 
	 * @param output	holds the last set of smoothed values, as [x,y,z]
	 * @return smoothed values
	 */
	public float[] lowPass(float[] input, float[] output){
		if ( output == null ) return input;	     	  
	    output[0] = output[0] + ALPHA * (input[0] - output[0]);
	    output[1] = output[1] + ALPHA * (input[1] - output[1]);
	    output[2] = output[2] + ALPHA_Z * (input[2] - output[2]);
	    
	    for(int i=0; i<output.length; i++){
	    	if(isStill(output[i])){
	    		output[i] = 0;
	    	}
	    }
	    
	    return output;
	}
	
	private boolean isStill(float output){
		if(output < STILL_NOISE && output > -STILL_NOISE){
			return true;
		}
		return false;
	}
}
