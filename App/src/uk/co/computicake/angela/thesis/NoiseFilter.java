/**
 * Low pass filters for reducing sensor noise
 */

package uk.co.computicake.angela.thesis;

public class NoiseFilter {
	private static final float STILL_NOISE = (float) 0.1; 
	private static final float TRESHOLD = (float)7.5; //allows 0 to 100 in 4 seconds
	// Smoothing constant for low-pass filter. Smaller values mean more smoothing.
	private static final float ALPHA = 0.25f;

	/**
	 * Low pass filter applied to raw sensor data.
	 * @param input		the new sensor values, as [x,y,z] 
	 * @param output	holds the last set of smoothed values, as [x,y,z]
	 * @return smoothed values
	 */
	public float[] lowPass(float[] input, float[] output){
		if ( output == null ) return input;
	     
	    for ( int i=0; i<input.length; i++ ) {
	        output[i] = output[i] + ALPHA * (input[i] - output[i]);
	    }
	    return output;
	}
	
	/**
	 * Applied to re-oriented accelerometer data.
	 */
	public void postLPF(){
		
	}
}
