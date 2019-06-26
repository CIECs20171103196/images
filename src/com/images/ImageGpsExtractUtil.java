package com.images;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

public class ImageGpsExtractUtil {
	
	private static double pi = Math.PI;
	private static double a = 6378245.0;
	private static double ee = 0.00669342162296594323;

	/**
	 * 坐标转高德地图坐标
	 * @param wgLat
	 * @param wgLng
	 * @return
	 */
	public static double[] transLatLng(double wgLat, double wgLng) {
		double[] ds = new double[2];
		double dLat = transLat(wgLng - 105.0, wgLat - 35.0, pi);
		double dLng = transLng(wgLng - 105.0, wgLat - 35.0, pi);
		double radLat = wgLat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		ds[0] = wgLat + dLat;
		ds[1] = wgLng + dLng;
		return ds;
	}

	/**
	 * 纬度转换
	 * @param x
	 * @param y
	 * @param pi
	 * @return
	 */
	private static double transLat(double x, double y, double pi) {
		double ret;
		ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2
				* Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}
	
	/**
	 * 经度转换
	 * @param x
	 * @param y
	 * @param pi
	 * @return
	 */
	private static double transLng(double x, double y, double pi) {
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
				* Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
				* pi)) * 2.0 / 3.0;
		return ret;
	}
	
	/**
	 * 转换为API可识别的坐标
	 * @param primaryLati
	 * @return
	 */
	public static double formatGps(String primaryLati) {
		String[] number = primaryLati.split(" ");
		String[] hs = number[0].split("/");
		String[] ms = number[1].split("/");
		String[] ss = number[2].split("/");
		double md = Double.valueOf(ms[0])/Double.valueOf(ms[1]) + Double.valueOf(ss[0])/(Double.valueOf(ss[1])*60);// 秒转分
		double sd = Double.valueOf(hs[0])/Double.valueOf(hs[1]) + md/60;// 分转度
		return sd;
	}
	
	/**
	 * 获取高德地图坐标系的参数
	 * @param filePath
	 * @return
	 */
	public static double[] getGaoDeParam(File file) {
		Metadata metaData = null;
		try {
			metaData = ImageMetadataReader.readMetadata(file);
		} catch (Exception e) {
			return null;
		}
		Collection<GpsDirectory> directory = (Collection<GpsDirectory>) metaData.getDirectory(GpsDirectory.class);
		Iterator<GpsDirectory> it = directory.iterator();
		double wgLat = 0.00;
		double wgLng = 0.00;
		while(it.hasNext()){
			GpsDirectory next = it.next();
			String primary = next.getString(GpsDirectory.TAG_GPS_ALTITUDE);
			wgLat = formatGps(primary);
			primary = next.getString(GpsDirectory.TAG_GPS_ALTITUDE);
			wgLng = formatGps(primary);
		}
		double[] rs = transLatLng(wgLat, wgLng);
		return rs;
	}

}








