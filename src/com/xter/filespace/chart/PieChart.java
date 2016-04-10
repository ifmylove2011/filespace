package com.xter.filespace.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import com.xter.filespace.util.FileUtils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

public class PieChart {

	GraphicalView mChartView;
	DefaultRenderer mRenderer;
	CategorySeries mCategorySeries;

	static final int[] COLORS = new int[] { Color.BLUE, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN };

	public interface ChartClickListener {
	}

	public View getChartView(Context context) {

		buildCategorySeries();
		buildCategoryRenderer();
		mChartView = ChartFactory.getPieChartView(context, mCategorySeries, mRenderer);

		mChartView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
				if (seriesSelection != null) {
					SimpleSeriesRenderer ssr = mRenderer.getSeriesRendererAt(seriesSelection.getPointIndex());
					ssr.setHighlighted(!ssr.isHighlighted());
					mChartView.repaint();
				}
				// 点击进入下一个目录，视图重绘
			}
		});
		return mChartView;
	}

	protected void buildCategorySeries() {
		mCategorySeries = new CategorySeries("Vehicles Chart");
	}

	protected void buildCategoryRenderer() {

		mRenderer = new DefaultRenderer();
		int length = mCategorySeries.getItemCount();
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(COLORS[i % COLORS.length]);
			mRenderer.addSeriesRenderer(r);
		}
		// 显示标签
		mRenderer.setShowLabels(true);
		// 不显示底部说明
		mRenderer.setShowLegend(false);
		// 设置标签字体大小
		mRenderer.setLabelsTextSize(35);
		mRenderer.setLabelsColor(Color.YELLOW);
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setZoomEnabled(true);
		mRenderer.setPanEnabled(true);
		mRenderer.setClickEnabled(true);
	}

	public void addSeries(String s, double v) {
		SimpleSeriesRenderer ssr = new SimpleSeriesRenderer();
		ssr.setColor(COLORS[new Random().nextInt(COLORS.length)]);
		mRenderer.addSeriesRenderer(ssr);
		mCategorySeries.add(s + " " + FileUtils.getFileSizeFormat((long) v), v);
		mChartView.repaint();
	}

	public void setTitle(String s, long size) {
		optiSeries(size);
		mRenderer.setChartTitle(s + "  " + FileUtils.getFileSizeFormat(size));
		mRenderer.setChartTitleTextSize(40);
		mChartView.repaint();
	}

	public void optiSeries(long size) {
		long other = 0;
		for (int i = 0; i < mCategorySeries.getItemCount(); i++) {
			double v = mCategorySeries.getValue(i);
			if (v < size / 100) {
				mCategorySeries.remove(i);
				i--;
				other += v;
			}
		}
		addSeries("other " + FileUtils.getFileSizeFormat(other), other);
	}

}
