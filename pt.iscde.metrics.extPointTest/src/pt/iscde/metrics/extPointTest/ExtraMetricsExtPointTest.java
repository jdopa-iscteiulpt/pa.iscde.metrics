package pt.iscde.metrics.extPointTest;

import java.util.ArrayList;

import pa.iscde.metrics.demo.Metrics;
import pa.iscde.metrics.extensibility.ExtraMetrics;

public class ExtraMetricsExtPointTest implements ExtraMetrics{
	@Override
	public void addClassMetric(ArrayList<Metrics> extraClassMetrics) {
		addNewMetricsToClassTable("testiiiiiiiiiiii", 20, extraClassMetrics);
		addNewMetricsToClassTable("test2iiiiiiiiiii", 34,  extraClassMetrics);
	}
	
	@Override
	public void addPackageMetric(ArrayList<Metrics> extraPackageMetrics) {
		addNewMetricsToPackageTable("test2", 34,  extraPackageMetrics);
	}

	public void addNewMetricsToClassTable(String name, int value, ArrayList<Metrics> extraClassMetrics) {

		Metrics m = new Metrics(name, value);
		extraClassMetrics.add(m);

	}
	
	public void addNewMetricsToPackageTable(String name, int value, ArrayList<Metrics> extraPackageMetrics) {

		Metrics m = new Metrics(name, value);
		extraPackageMetrics.add(m);

	}
}
