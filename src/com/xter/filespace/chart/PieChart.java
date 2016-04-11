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
	 * ���Ƶ�ͼ��
	 */
	GraphicalView mChartView;
	/**
	 * ͼ����Ⱦ��
	 */
	DefaultRenderer mRenderer;
	/**
	 * �����������ݼ�
	 */
	CategorySeries mCategorySeries;
	/**
	 * �������ʱ����
	 */
	SeriesClickListener seriesClickListener;

	/**
	 * ��ɫ����
	 */
	static final int[] COLORS = new int[] { Color.BLUE, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN };

	/**
	 * ��ȡͼ��
	 * 
	 * @param context ������
	 * @return view ���Ƶ�ͼ��
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
					// ���������һ��Ŀ¼����ͼ�ػ�
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
	 * �������ݼ�
	 */
	protected void buildCategorySeries() {
		mCategorySeries = new CategorySeries("FileSpace Chart");
	}

	/**
	 * ������Ⱦ��
	 */
	protected void buildCategoryRenderer() {
		mRenderer = new DefaultRenderer();
		int length = mCategorySeries.getItemCount();
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(COLORS[i % COLORS.length]);
			mRenderer.addSeriesRenderer(r);
		}
		// ��ʾ��ǩ
		mRenderer.setShowLabels(true);
		// ����ʾ�ײ�˵��
		mRenderer.setShowLegend(true);
		// ���ñ�ǩ�����С
		mRenderer.setLabelsTextSize(25);
		mRenderer.setLabelsColor(Color.YELLOW);
		mRenderer.setZoomEnabled(true);
		mRenderer.setPanEnabled(true);
		mRenderer.setClickEnabled(true);
	}

	/**
	 * ������ݶ�--���ݺ���Ⱦ����Ҫ��ӣ����ػ���ͼ
	 * 
	 * @param s ����
	 * @param v ����
	 */
	public void addSeries(String s, double v) {
		SimpleSeriesRenderer ssr = new SimpleSeriesRenderer();
		ssr.setColor(COLORS[new Random().nextInt(COLORS.length)]);
		mRenderer.addSeriesRenderer(ssr);
		mCategorySeries.add(s + " " + FileUtils.getFileSizeFormat((long) v), v);
	}

	/**
	 * ���ñ���
	 * 
	 * @param s ��ǰĿ¼
	 * @param size ��ǰĿ¼��С
	 */
	public void setTitle(String s, long size) {
		optiSeries(size);
		optiRenderer();
		mRenderer.setChartTitle(s + "  " + FileUtils.getFileSizeFormat(size));
		mRenderer.setChartTitleTextSize(35);
		mChartView.repaint();
	}

	/**
	 * �Ż�--�������ļ���С���ܴ�С1%���ļ����ϲ�ΪOther
	 * 
	 * @param size ��С
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
	 * �Ż���Ⱦ��
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
