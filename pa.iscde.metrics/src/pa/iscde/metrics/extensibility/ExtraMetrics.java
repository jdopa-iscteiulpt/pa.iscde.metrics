package pa.iscde.metrics.extensibility;

import java.util.ArrayList;

import pa.iscde.metrics.demo.Metrics;

public interface ExtraMetrics {

	default public void addClassMetric(ArrayList<Metrics> extraClassMetrics) {}
	
	default public void addPackageMetric(ArrayList<Metrics> extraPackageMetrics) {}
	
	
}
