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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SnifferActivity extends Activity implements SeriesClickListener {

	LinearLayout mSnifferLayout;
	PieChart pie;
	TextView tvScan;

	SharedPreferences pre;
	boolean isRoot;

	String[] sdDirs;
	long exitTime;

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
		mSnifferLayout.addView(pie.getChartView(this));
	}

	protected void initData() {
		sdDirs = FileUtils.getStorageDir(this);
		pre = getSharedPreferences("setting", Context.MODE_PRIVATE);
		isRoot = pre.getBoolean("root", false);
		if (!isRoot)
			fetchRoot();
		new ScanTask(tvScan, pie).execute(sdDirs[0]);
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
		TextView tvProgress;
		PieChart pie;

		ScanTask(TextView tv, PieChart pieC) {
			tvProgress = tv;
			pie = pieC;
		}

		@Override
		protected void onPreExecute() {
			pie.hide();
			pie.reset();
		}

		@Override
		protected Void doInBackground(String... params) {
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
				if (params[0].endsWith("other")) {
					pie.setTitle(params[0], pie.getOther());
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			tvProgress.setText("scan finished");
			pie.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			tvProgress.setText(values[0]);
			LogUtils.i(values[0] + "  " + values[1]);
		}
	}

	@Override
	public void onBackPressed() {
		String path = pie.getTitle();
		LogUtils.d("back000" + path);
		long time = System.currentTimeMillis();
		for (int i = 0; i < sdDirs.length; i++) {
			if (path.equalsIgnoreCase(sdDirs[i])) {
				if (time - exitTime > 2000) {
					exitTime = time;
					Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
				} else {
					super.onBackPressed();
				}
			} else {
				new ScanTask(tvScan, pie).execute(FileUtils.getParentFilePath(path));
			}
		}
	}

	@Override
	public void onSeriesClick(String path) {
		File f = new File(path);
		if (f.isDirectory() || path.endsWith("other")) {
			new ScanTask(tvScan, pie).execute(path);
		} else {
			Toast.makeText(getApplicationContext(), path + " is not a dir", Toast.LENGTH_SHORT).show();
		}
	}

}
