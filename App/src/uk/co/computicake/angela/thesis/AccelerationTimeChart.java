package uk.co.computicake.angela.thesis;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.Point;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class AccelerationTimeChart {
	
	TimeSeries dataset;
	private GraphicalView view;
	private XYSeriesRenderer renderer = new XYSeriesRenderer(); // This will be used for customisation
	private XYMultipleSeriesDataset mDataset;
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); 
	
	private static int MAX_X_SIZE = 80;
	private int x_pos;
	private static int ANDROID_ICS_COLOUR = Color.rgb(51, 181,229);
	
	public AccelerationTimeChart() {
		
		TimeSeries dataset = new TimeSeries("Acceleration");
		mDataset = new XYMultipleSeriesDataset();
		mDataset.addSeries(dataset);
		
		renderer.setColor(ANDROID_ICS_COLOUR);
		renderer.setPointStyle(PointStyle.CIRCLE);
		renderer.setFillPoints(true);
		renderer.setFillBelowLine(true);
		
		renderer.setFillBelowLineColor(Color.argb(120,51, 181,229));
		
		//cannot be bothered to enable zoom at present
		
		mRenderer.setXTitle("Time in s");
		mRenderer.setYTitle("m/s");
		x_pos = 0;
		
		// allows for the entire graph to be customised
		mRenderer.addSeriesRenderer(renderer);
	}
	
	public GraphicalView getView(Context context){
		
		view = ChartFactory.getLineChartView(context, mDataset, mRenderer); //possibly getTimeChartView instead
		return view;
	}
	
	//allows us to dynamically change and add new points
	public void addNewPoints(Point p){
		dataset.add(p.getX(), p.getY());
	}
	
	//Assumes pos > MAX_X_SIZE
	public void adjust_x(int pos){
		// if it's already gone past
		if(pos > MAX_X_SIZE){
			mRenderer.setXAxisMin(++x_pos);
		}
	}
	
	public void clear(){
		TimeSeries dataset = new TimeSeries("Acceleration");
		mDataset = new XYMultipleSeriesDataset();
		mDataset.addSeries(dataset);
	}
}
