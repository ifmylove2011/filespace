package com.xter.filespace;

import java.io.File;
import java.io.IOException;

import com.xter.filespace.chart.PieChart;
import com.xter.filespace.util.FileUtils;
import com.xter.filespace.util.LinuxShell;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SnifferActivity extends Activity {

	LinearLayout mSnifferLayout;
	PieChart pie;

	Button btnAdd;
	TextView tvScan;

	static final String ROOT = "/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sniffer);
		// startActivity(new Intent(this,PieActivity.class));
		// fetchRoot();
		initLayout();
		initData();
	}

	protected void initLayout() {
		pie = new PieChart();
		View pieView = pie.getChartView(this);
		mSnifferLayout = (LinearLayout) findViewById(R.id.sniffer);
		mSnifferLayout.addView(pieView);

		btnAdd = (Button) findViewById(R.id.btn_add);
		tvScan = (TextView) findViewById(R.id.tv_scan);
	}

	protected void initData() {
		new ScanTask(tvScan, pie).execute(Environment.getExternalStorageDirectory().getAbsolutePath());
		// btnAdd.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Log.i("kk", "kkkkk");
		// pie.addSeries("kkk", 22);
		// }
		// });
	}

	/**
	 * 获取root权限
	 */
	protected void fetchRoot() {
		try {
			if (LinuxShell.isRoot(Runtime.getRuntime(), 100)) {
				Toast.makeText(getApplicationContext(), "root successed", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "root failed", Toast.LENGTH_SHORT).show();
			}
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
		protected Void doInBackground(String... params) {
			File file = new File(params[0]);
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				int length = files.length;
				// 目录总大小
				long total = 0;
				for (int i = 0; i < length; i++) {
					long size = FileUtils.getFileSize(files[i]);
					pie.addSeries(files[i].getName(), size);
					total += size;
				}
				pie.setTitle(file.getAbsolutePath(), total);
			} else {
				long size = FileUtils.getFileSize(file);
				pie.addSeries(file.getName() + " " + FileUtils.getFileSizeFormat(size), size);
				publishProgress(file.getName(), "" + size);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			tvProgress.setText("scan finished");
		}

		@Override
		protected void onProgressUpdate(String... values) {
			tvProgress.setText(values[0]);
		}
	}

}
