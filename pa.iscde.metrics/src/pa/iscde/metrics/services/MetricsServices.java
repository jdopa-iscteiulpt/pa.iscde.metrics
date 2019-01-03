package pa.iscde.metrics.services;

import java.io.File;
import java.util.ArrayList;

import pa.iscde.metrics.demo.Metrics;

public interface MetricsServices {

	ArrayList<Metrics> getMetrics(File file);
	
}
