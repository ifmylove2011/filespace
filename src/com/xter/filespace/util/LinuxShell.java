package com.xter.filespace.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LinuxShell {
	
	public final Runtime shell;
	
	public LinuxShell(Runtime runtime) {
		shell = runtime;
	}
	
	/**
	 * ���Ի�ȡrootȨ��
	 * 
	 * @param r ���л���
	 * @param wait �ȴ�ʱ��
	 * @return boolean ���
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static boolean isRoot(Runtime r, long wait) throws IOException, InterruptedException {
		boolean root = false;
		Process p = null;
		BufferedReader errReader = null;
		p = Runtime.getRuntime().exec("su");
		Thread.sleep(wait);
		errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		if (!errReader.ready()) {
			root = true;
			p.destroy();
		}
		return root;
	}

	public static Process runCmd(Runtime r, String cmd) throws IOException {
		return r.exec(cmd);
	}
	
	public static void chmod(Runtime r ){
		try {
			r.exec("su");
			r.exec(new String[]{"chmod","-R","777","/data"});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
