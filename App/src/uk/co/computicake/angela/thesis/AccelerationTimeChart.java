package uk.co.computicake.angela.thesis;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.Point;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;

/**
 * Class handling displaying of acceleration values as a chart. 
 *
 */
public class AccelerationTimeChart {
	
	TimeSeries datasetx;
	TimeSeries datasety;
	TimeSeries datasetz;
	private GraphicalView view;
	private XYSeriesRenderer rendererx;
	private XYSeriesRenderer renderery;
	private XYSeriesRenderer rendererz;
	private XYMultipleSeriesDataset mDataset;
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); 
	
	private static int MAX_X_SIZE = 80;
	private int x_pos;
	private static int ANDROID_ICS_COLOUR = Color.rgb(51, 181,229);
	
	public AccelerationTimeChart() {
		
		datasetx = new TimeSeries("Acc X");
		datasety = new TimeSeries("Acc Y");
		datasetz = new TimeSeries("Acc Z");
		mDataset = new XYMultipleSeriesDataset();
		mDataset.addSeries(datasetx);
		mDataset.addSeries(datasety);
		mDataset.addSeries(datasetz);
		
		rendererx = initRenderer(ANDROID_ICS_COLOUR, Color.argb(120,51, 181,229));		
		renderery = initRenderer(Color.RED, Color.argb(120,250, 81,29));
		rendererz = initRenderer(Color.WHITE, Color.argb(120,5, 11, 9));
			
		mRenderer.setXTitle("Data points");
		mRenderer.setYTitle("m/s^2");
		x_pos = 0;
		
		mRenderer.addSeriesRenderer(rendererx);
		mRenderer.addSeriesRenderer(renderery);
		mRenderer.addSeriesRenderer(rendererz);
	}
	
	public GraphicalView getView(Context context){	
		view = ChartFactory.getLineChartView(context, mDataset, mRenderer);
		return view;
	}
	
	/**
	 * Initialises a renderer.
	 * @param c graph colour
	 * @param fillColor fill colour
	 * @return an initialised renderer
	 */
	private XYSeriesRenderer initRenderer(int c, int fillColor){
		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setColor(c);
		renderer.setFillBelowLine(true);	
		renderer.setFillBelowLineColor(fillColor);
		return renderer;
	}
	
	 /**
	  * Allows us to dynamically change and add new points to the chart.
	  * @param px point representing the x coordinate. 
	  * @param py point representing the y coordinate.
	  * @param pz point representing the z coordinate.
	  */
	public void addNewPoints(Point px, Point py, Point pz){
		datasetx.add(px.getX(), px.getY());
		datasety.add(py.getX(), py.getY());
		datasetz.add(pz.getX(), pz.getY());
	}
	
	/**
	 * Moves the graph so the spacing between points stays uniform throughout the recording. 
	 * Avoids problem of entire graph wanting to be displayed simultaneously and looking really squashed. 
	 * @param pos
	 */
	public void adjust_x(int pos){
		if(pos > MAX_X_SIZE){
			mRenderer.setXAxisMin(++x_pos);
		}
	}
}
