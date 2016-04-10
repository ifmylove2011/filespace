package com.xter.filespace;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class PieActivity extends Activity {
	private CategorySeries mSeries;
	private DefaultRenderer mRenderer;
	private GraphicalView mChartView;

	private LinearLayout chartLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pie);
		initLayout();
	}

	protected void initLayout() {
		initView();
		chartLayout = (LinearLayout) findViewById(R.id.chart_pie);
		mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);

		mChartView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
				if (seriesSelection != null) {
					for (int i = 0; i < mSeries.getItemCount(); i++) {
						mRenderer.getSeriesRendererAt(i).setHighlighted(i == seriesSelection.getPointIndex());
					}
					mChartView.repaint();
				}
			}
		});
		chartLayout.addView(mChartView);
	}

	public void initView() {
		initCategorySeries();
		initCategoryRenderer();
	}

	protected void initCategorySeries() {
		mSeries = new CategorySeries("Vehicles Chart");
		mSeries.add("金", 12);
		mSeries.add("木", 14);
		mSeries.add("水", 11);
		mSeries.add("火", 10);
		mSeries.add("土", 19);
		
		
	}

	protected void initCategoryRenderer() {
		int[] colors = new int[] { Color.BLUE, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN };// 每块饼图的颜色
		mRenderer = new DefaultRenderer();
		for (int color : colors) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(color);
			mRenderer.addSeriesRenderer(r);
		}
		// 显示标签
		mRenderer.setShowLabels(true);
		// 不显示底部说明
		mRenderer.setShowLegend(true);
		// 设置标签字体大小
		mRenderer.setLabelsTextSize(35);
		mRenderer.setLabelsColor(Color.YELLOW);
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setZoomEnabled(true);
		mRenderer.setPanEnabled(false);
		mRenderer.setClickEnabled(true);
	}

}
