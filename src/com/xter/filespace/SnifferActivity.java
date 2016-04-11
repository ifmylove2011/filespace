package com.xter.filespace;

import java.io.File;
import java.io.IOException;

import com.xter.filespace.chart.PieChart;
import com.xter.filespace.chart.PieChart.SeriesClickListener;
import com.xter.filespace.util.FileUtils;
import com.xter.filespace.util.LinuxShell;
import com.xter.filespace.util.LogUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StatFs;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SnifferActivity extends Activity implements SeriesClickListener {

	LinearLayout mSnifferLayout;
	PieChart pie;

	TextView tvScan;

	LinuxShell linux;
	SharedPreferences pre;
	boolean isRoot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sniffer);
		initLayout();
		initData();
	}

	protected void initLayout() {
		pie = new PieChart();

		mSnifferLayout = (LinearLayout) findViewById(R.id.sniffer);

		tvScan = (TextView) findViewById(R.id.tv_scan);
	}

	protected void initData() {
		pre = getSharedPreferences("setting", Context.MODE_PRIVATE);
		isRoot = pre.getBoolean("root", false);
		if (!isRoot)
			fetchRoot();
		linux = new LinuxShell(Runtime.getRuntime());

		String[] sds = FileUtils.getStorageDir(this);
		getSpaceInfo(sds[1]);
		new ScanTask(tvScan, pie, mSnifferLayout, this).execute(sds[0]);
	}

	/**
	 * 获取root权限
	 */
	protected void fetchRoot() {
		try {
			SharedPreferences.Editor editor = pre.edit();
			if (LinuxShell.isRoot(Runtime.getRuntime(), 100)) {
				Toast.makeText(getApplicationContext(), "root successed", Toast.LENGTH_SHORT).show();
				isRoot = true;
				editor.putBoolean("root", true);
			} else {
				Toast.makeText(getApplicationContext(), "root failed", Toast.LENGTH_SHORT).show();
				isRoot = false;
				editor.putBoolean("root", false);
			}
			editor.apply();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 扫描文件任务
	 * 
	 * @author XTER
	 *
	 */
	static class ScanTask extends AsyncTask<String, String, Void> {

		LinearLayout mSnifferLayout;
		TextView tvProgress;
		PieChart pie;
		View view;

		ScanTask(TextView tv, PieChart pieC, LinearLayout layout, Context context) {
			tvProgress = tv;
			pie = pieC;
			mSnifferLayout = layout;
			view = pie.getChartView(context);
		}

		@Override
		protected Void doInBackground(String... params) {
			mSnifferLayout.removeView(view);
			File file = new File(params[0]);
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				if (files != null) {
					int length = files.length;
					// 目录总大小
					long total = 0;
					// 遍历目录中各文件夹或文件
					for (int i = 0; i < length; i++) {
						long size = FileUtils.getFileSize(files[i]);
						pie.addSeries(files[i].getName(), size);
						total += size;
						publishProgress(files[i].getAbsolutePath(), "" + size);
					}
					pie.setTitle(file.getAbsolutePath(), total);
				}
			} else {
				long size = FileUtils.getFileSize(file);
				pie.addSeries(file.getName(), size);
				publishProgress(file.getAbsolutePath(), "" + size);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			tvProgress.setText("scan finished");
			mSnifferLayout.addView(view);
		}

		@Override
		protected void onProgressUpdate(String... values) {
			tvProgress.setText(values[0]);
			LogUtils.i(values[0] + "  " + values[1]);
		}
	}

	/**
	 * 查看已用空间
	 * @param path 路径
	 */
	protected void getSpaceInfo(String path) {
		StatFs sf = new StatFs(path);
		LogUtils.d("已用" + FileUtils.getFileSizeFormat(sf.getTotalBytes() - sf.getAvailableBytes()));
	}

	@Override
	public void onBackPressed() {

	}

	@Override
	public void onSeriesClick(String path) {
		new ScanTask(tvScan, pie, mSnifferLayout, this).execute(path);
	}

}
