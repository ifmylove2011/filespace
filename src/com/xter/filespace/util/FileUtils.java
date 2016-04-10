package com.xter.filespace.util;

import java.io.File;
import java.text.DecimalFormat;

public class FileUtils {
	/**
	 * �õ��ļ���Ŀ¼��С
	 * 
	 * @param f
	 *            �ļ�
	 * @return long ��С
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
}
