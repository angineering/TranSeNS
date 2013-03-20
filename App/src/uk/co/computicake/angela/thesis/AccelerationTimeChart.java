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
	
	private GraphicalView view;
	
	private TimeSeries dataset = new TimeSeries("Acceleration");
	private XYSeriesRenderer renderer = new XYSeriesRenderer(); // This will be used for customisation
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); 
	
	public AccelerationTimeChart() {
		
		mDataset.addSeries(dataset);
		
		renderer.setColor(Color.WHITE);
		renderer.setPointStyle(PointStyle.CIRCLE);
		renderer.setFillPoints(true);
		
		//cannot be bothered to enable zoom at present
		
		mRenderer.setXTitle("Time in s");
		mRenderer.setYTitle("m/s");
		
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
}
