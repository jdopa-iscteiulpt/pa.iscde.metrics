package pa.iscde.metrics.demo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Metrics {
	
	private String name;
	private int value;
	
	public Metrics(String name, int value) {
		
		this.name = name;
		this.value = value;
		
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	
	
}
