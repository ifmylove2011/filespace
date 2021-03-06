package com.xter.filespace.chart;

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
	CategorySeries mOtherSeries;
	/**
	 * 点击分区时监听
	 */
	SeriesClickListener seriesClickListener;

	/**
	 * 是否就绪
	 */
	boolean isFinished;

	/**
	 * 颜色区分
	 */
	static final int[] COLORS = new int[] { Color.BLUE, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN };

	public PieChart() {
		buildCategorySeries();
		buildCategoryRenderer();
	}

	/**
	 * 获取图表
	 * 
	 * @param context 上下文
	 * @return view 绘制的图表
	 */
	public View getChartView(Context context) {
		seriesClickListener = (SeriesClickListener) context;
		mChartView = ChartFactory.getPieChartView(context, mCategorySeries, mRenderer);
		mChartView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
				if (seriesSelection != null) {
					int index = seriesSelection.getPointIndex();
					// if(index == mRenderer.getSeriesRendererCount()-1){
					//
					// }
					SimpleSeriesRenderer ssr = mRenderer.getSeriesRendererAt(index);
					ssr.setHighlighted(!ssr.isHighlighted());
					mChartView.repaint();
					// 点击进入下一个目录，视图重绘
					if (!ssr.isHighlighted())
						seriesClickListener.onSeriesClick(FileUtils.getFilePathFromChart(mRenderer.getChartTitle(),
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
		mOtherSeries = new CategorySeries("FileSpace Other");
	}

	/**
	 * 构建渲染器
	 */
	protected void buildCategoryRenderer() {
		mRenderer = new DefaultRenderer();
		// 显示标签
		mRenderer.setShowLabels(true);
		// 不显示底部说明
		mRenderer.setShowLegend(true);
		// 设置标签字体大小
		mRenderer.setLabelsTextSize(25);
		mRenderer.setLabelsColor(Color.YELLOW);
		mRenderer.setZoomEnabled(true);
		mRenderer.setZoomButtonsVisible(true);
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
		mOtherSeries.clear();
		long otherSize = 0;
		if (mCategorySeries.getItemCount() > 3) {
			for (int i = 0; i < mCategorySeries.getItemCount(); i++) {
				double v = mCategorySeries.getValue(i);
				if (v < size / 200) {
					LogUtils.d(mCategorySeries.getCategory(i) + "," + mCategorySeries.getValue(i));
					mOtherSeries.add(mCategorySeries.getCategory(i), mCategorySeries.getValue(i));
					mCategorySeries.remove(i);
					i--;
					otherSize += v;
				}
			}
			if (otherSize > size / 100)
				addSeries("other", otherSize);
		}
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

	/**
	 * 获取other分区信息
	 * 
	 * @return long 大小
	 */
	public long getOther() {
		int length = mOtherSeries.getItemCount();
		long size = 0;
		for (int i = 0; i < length; i++) {
			size += mOtherSeries.getValue(i);
			addSeries(mOtherSeries.getCategory(i), mOtherSeries.getValue(i));
		}
		return size;
	}

	/**
	 * 隐藏图表--重置图表数据
	 */
	public void reset() {
		mChartView.setVisibility(View.GONE);
		mRenderer.removeAllRenderers();
		mCategorySeries.clear();
		isFinished = false;
	}

	/**
	 * 显示图表--刷新图表
	 */
	public void show() {
		mChartView.repaint();
		mChartView.setVisibility(View.VISIBLE);
		isFinished = true;
	}

	/**
	 * 获取标题
	 * 
	 * @return string 当前标题
	 */
	public String getTitle() {
		return FileUtils.getFilePathFromChartTitle(mRenderer.getChartTitle());
	}

	/**
	 * 是否处于就绪状态
	 * 
	 * @return
	 */
	public boolean isFinish() {
		return isFinished;
	}
}
