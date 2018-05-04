package com.wang.tripdiaryapp.util;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UploadUtil {
	public static String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
	public static String APP_NAME = "TripDiary";

	/**
	 * 检查是否存在SDCard
	 * @return
	 */
	public static boolean hasSdcard(){
		String state = Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 获得文章图片保存路径
	 * @return
	 */
	public static String getPictureDir(){
		String imageCacheUrl = SDCardRoot + APP_NAME + File.separator;
		File file = new File(imageCacheUrl);
		if(!file.exists())
			file.mkdir();  //如果不存在则创建
		return imageCacheUrl;
	}

	/**
	 * 图片保存到SD卡
	 * @param bitmap
	 * @return
	 */
	public static String saveToSdCard(Bitmap bitmap) {
		String imageUrl = getPictureDir() + System.currentTimeMillis() + ".png";
		File file = new File(imageUrl);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}

	public static String getFileName(String pathandname){
		int start=pathandname.lastIndexOf("/");
		int end=pathandname.lastIndexOf(".");
		if (start!=-1 && end!=-1) {
			return pathandname.substring(start+1, end);
		}
		else {
			return null;
		}
	}

	/** 删除文件 **/
	public static boolean deleteFile(String filePath) {
		File file = new File(filePath);
		boolean isOk = false;
		if (file.isFile() && file.exists())
			isOk = file.delete(); // 删除文件
		return isOk;
	}

}
