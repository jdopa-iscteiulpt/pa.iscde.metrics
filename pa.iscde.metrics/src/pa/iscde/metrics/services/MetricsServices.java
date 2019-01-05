package pa.iscde.metrics.services;

import java.io.File;
import java.util.ArrayList;

import pa.iscde.metrics.demo.Metrics;

public interface MetricsServices {

	
	/**
	 * Creates an array with class Metrics from a file.
	 * @param file, represents the file that's going to get his Metrics analyzed.
	 * @return an array with all analyzed class Metrics.
	 * 
	 */
	
	ArrayList<Metrics> getClassMetrics(File file);
	
	
	
	/**
	 * Creates an array with package Metrics from a file.
	 * @param file, represents the file that's going to get his Metrics analyzed.
	 * @return an array with all analyzed package Metrics.
	 * 
	 */
	
	
	ArrayList<Metrics> getPackageMetrics(File file);
	
}
