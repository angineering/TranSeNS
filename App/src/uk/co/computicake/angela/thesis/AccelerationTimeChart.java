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
	
	TimeSeries datasetx;
	TimeSeries datasety;
	TimeSeries datasetz;
	private GraphicalView view;
	private XYSeriesRenderer renderer = new XYSeriesRenderer(); // This will be used for customisation
	private XYSeriesRenderer renderery = new XYSeriesRenderer();
	private XYSeriesRenderer rendererz = new XYSeriesRenderer();
	private XYMultipleSeriesDataset mDataset;
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); 
	
	private static int MAX_X_SIZE = 80;
	private int x_pos;
	private static int ANDROID_ICS_COLOUR = Color.rgb(51, 181,229);
	
	public AccelerationTimeChart() {
		
		datasetx = new TimeSeries("Acceleration");
		datasety = new TimeSeries("Accy");
		datasetz = new TimeSeries("Accz");
		mDataset = new XYMultipleSeriesDataset();
		mDataset.addSeries(datasetx);
		mDataset.addSeries(datasety);
		mDataset.addSeries(datasetz);
		
		renderer.setColor(ANDROID_ICS_COLOUR);
		renderer.setPointStyle(PointStyle.CIRCLE);
		renderer.setFillPoints(true);
		renderer.setFillBelowLine(true);	
		renderer.setFillBelowLineColor(Color.argb(120,51, 181,229));
		
		renderery.setColor(Color.RED);
		renderery.setPointStyle(PointStyle.CIRCLE);
		renderery.setFillPoints(true);
		renderery.setFillBelowLine(true);		
		renderery.setFillBelowLineColor(Color.argb(120,250, 81,29));
		
		
		rendererz.setColor(Color.WHITE);
		rendererz.setPointStyle(PointStyle.CIRCLE);
		rendererz.setFillPoints(true);
		rendererz.setFillBelowLine(true);		
		rendererz.setFillBelowLineColor(Color.argb(120,5, 11, 9));
		
		
		//cannot be bothered to enable zoom at present		
		mRenderer.setXTitle("Time");
		mRenderer.setYTitle("m/s");
		x_pos = 0;
		
		// allows for the entire graph to be customised
		mRenderer.addSeriesRenderer(renderer);
		mRenderer.addSeriesRenderer(renderery);
		mRenderer.addSeriesRenderer(rendererz);
	}
	
	public GraphicalView getView(Context context){	
		view = ChartFactory.getLineChartView(context, mDataset, mRenderer); //possibly getTimeChartView instead
		return view;
	}
	
	//allows us to dynamically change and add new points
	public void addNewPoints(Point p, Point py, Point pz){
		datasetx.add(p.getX(), p.getY());
		datasety.add(py.getX(), py.getY());
		datasetz.add(pz.getX(), pz.getY());
	}
	
	//Assumes pos > MAX_X_SIZE
	public void adjust_x(int pos){
		// if it's already gone past
		if(pos > MAX_X_SIZE){
			mRenderer.setXAxisMin(++x_pos);
		}
	}
	/*
	public void clear(){
		dataset = new TimeSeries("Acceleration");
		mDataset = new XYMultipleSeriesDataset();
		mDataset.addSeries(dataset);
	}*/
}
