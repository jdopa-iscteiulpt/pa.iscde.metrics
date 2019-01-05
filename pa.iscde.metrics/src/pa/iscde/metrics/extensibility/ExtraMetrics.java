package pa.iscde.metrics.extensibility;

import java.util.ArrayList;

import pa.iscde.metrics.demo.Metrics;

/**
 *
 * Interface used to implement the extraMetrics extension-point. 
 *
 */


public interface ExtraMetrics {

	
	/**
	 * Function used to add a class metrics to the current table.
	 * @param extraClassMetrics, represents the array where the metric should be added.
	 *
	 */
	
	
	default public void addClassMetric(ArrayList<Metrics> extraClassMetrics) {}
	
	
	/**
	 * Function used to add a package metrics to the current table.
	 * @param extraClassMetrics, represents the array where the metric should be added.
	 *
	 */
	
	
	default public void addPackageMetric(ArrayList<Metrics> extraPackageMetrics) {}
	
	
}
