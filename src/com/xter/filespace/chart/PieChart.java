package com.xter.filespace.chart;

import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import com.xter.filespace.util.FileUtils;
import com.xter.filespace.util.LogUtils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;

public class PieChart {

	public interface SeriesClickListener {
		void onSeriesClick(String path);
	}

	/**
	 * 绘制的图表
	 */
	GraphicalView mChartView;
	/**
	 * 图形渲染器
	 */
	DefaultRenderer mRenderer;
	/**
	 * 绘制所用数据集
	 */
	CategorySeries mCategorySeries;
	/**
	 * 点击分区时监听
	 */
	SeriesClickListener seriesClickListener;

	/**
	 * 颜色区分
	 */
	static final int[] COLORS = new int[] { Color.BLUE, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN };

	/**
	 * 获取图表
	 * 
	 * @param context 上下文
	 * @return view 绘制的图表
	 */
	public View getChartView(Context context) {
		seriesClickListener = (SeriesClickListener) context;
		buildCategorySeries();
		buildCategoryRenderer();
		mChartView = ChartFactory.getPieChartView(context, mCategorySeries, mRenderer);
		mChartView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
				if (seriesSelection != null) {
					int index = seriesSelection.getPointIndex();
					SimpleSeriesRenderer ssr = mRenderer.getSeriesRendererAt(index);
					ssr.setHighlighted(!ssr.isHighlighted());
					mChartView.repaint();
					// 点击进入下一个目录，视图重绘
					seriesClickListener.onSeriesClick(FileUtils.getFilePathFromChart(mRenderer.getChartTitle(),
							mCategorySeries.getCategory(index)));
					LogUtils.d(FileUtils.getFilePathFromChart(mRenderer.getChartTitle(),
							mCategorySeries.getCategory(index)));
				}
			}
		});
		return mChartView;
	}

	/**
	 * 构建数据集
	 */
	protected void buildCategorySeries() {
		mCategorySeries = new CategorySeries("FileSpace Chart");
	}

	/**
	 * 构建渲染器
	 */
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
		mRenderer.setShowLegend(true);
		// 设置标签字体大小
		mRenderer.setLabelsTextSize(25);
		mRenderer.setLabelsColor(Color.YELLOW);
		mRenderer.setZoomEnabled(true);
		mRenderer.setPanEnabled(true);
		mRenderer.setClickEnabled(true);
	}

	/**
	 * 添加数据对--数据和渲染器均要添加，且重绘视图
	 * 
	 * @param s 标题
	 * @param v 数据
	 */
	public void addSeries(String s, double v) {
		SimpleSeriesRenderer ssr = new SimpleSeriesRenderer();
		ssr.setColor(COLORS[new Random().nextInt(COLORS.length)]);
		mRenderer.addSeriesRenderer(ssr);
		mCategorySeries.add(s + " " + FileUtils.getFileSizeFormat((long) v), v);
	}

	/**
	 * 设置标题
	 * 
	 * @param s 当前目录
	 * @param size 当前目录大小
	 */
	public void setTitle(String s, long size) {
		optiSeries(size);
		optiRenderer();
		mRenderer.setChartTitle(s + "  " + FileUtils.getFileSizeFormat(size));
		mRenderer.setChartTitleTextSize(35);
		mChartView.repaint();
	}

	/**
	 * 优化--将零碎文件（小于总大小1%的文件）合并为Other
	 * 
	 * @param size 大小
	 */
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
		addSeries("other", other);
	}

	/**
	 * 优化渲染器
	 */
	public void optiRenderer() {
		SimpleSeriesRenderer[] ssrs = mRenderer.getSeriesRenderers();
		int size = ssrs.length;
		for (int i = 0; i < size; i++) {
			ssrs[i].setChartValuesTextAlign(Align.LEFT);
			ssrs[i].setColor(COLORS[i % COLORS.length]);
		}
	}

	public synchronized void reset() {
		mCategorySeries.clear();
		mRenderer.removeAllRenderers();
		mChartView.repaint();
	}
}
