package com.xter.filespace.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

import android.content.Context;
import android.os.storage.StorageManager;

public class FileUtils {
	/**
	 * 得到文件或目录大小
	 * 
	 * @param f 文件
	 * @return long 大小
	 */
	public static long getFileSize(File f) {
		long size = 0;
		if (f.isDirectory()) {
			File[] fs = f.listFiles();
			if (fs != null) {
				for (int i = 0; i < fs.length; i++) {
					size = size + getFileSize(fs[i]);
				}
			}
		} else {
			return f.length();
		}
		return size;
	}

	/**
	 * 简单化表示存储容量
	 * 
	 * @param size 大小
	 * @return string 字符
	 */
	public static String getFileSizeFormat(long size) {
		DecimalFormat df = new DecimalFormat("#.00");
		if (Math.log10(size) > 9) {
			return df.format(size / Math.pow(1024, 3)) + "gb";
		} else if (Math.log10(size) > 6) {
			return df.format(size / Math.pow(1024, 2)) + "mb";
		} else if (Math.log10(size) > 3) {
			return df.format(size / Math.pow(1024, 1)) + "kb";
		} else {
			return size + "bytes";
		}
	}

	/**
	 * 得到当前挂载的SD卡
	 * 
	 * @param context 上下文
	 * @return
	 */
	public static String[] getStorageDir(Context context) {
		StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
		try {
			Class<?>[] paramClasses = {};
			Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
			getVolumePathsMethod.setAccessible(true);
			Object[] params = {};
			Object invoke = getVolumePathsMethod.invoke(storageManager, params);
			return (String[]) invoke;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getFilePathFromChart(String title, String renderer) {
		return title.substring(0, title.lastIndexOf(" ") - 1) + File.separator
				+ renderer.substring(0, renderer.lastIndexOf(" "));
	}

	public static String getFilePathFromChartTitle(String title) {
		return title.substring(0, title.lastIndexOf(" ") - 1);
	}

}
