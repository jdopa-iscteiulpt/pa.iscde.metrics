package pa.iscde.metrics.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import pa.iscde.metrics.extensibility.ExtraMetrics;
import pa.iscde.metrics.services.MetricsServices;
import pt.iscte.pidesco.extensibility.PidescoView;
import pt.iscte.pidesco.javaeditor.service.JavaEditorListener;
import pt.iscte.pidesco.javaeditor.service.JavaEditorServices;

public class Testview implements PidescoView, MetricsServices{


	private Table classTable;
	private Table packageTable;
	private final String [] collumns = new String [] {"Metric                                          ", "Total"};
	private String [] baseMetricsForClass = new String [] {"Number of Static Methods", "Total Lines of Code", 
			"Number of Attributes", "Number of Overriden Methods", 
			"Number of Static Attributes", "Number of Methods", "Number of Parameters"};

	//Base Array for the metrics writen in the file
	private ArrayList<String> linesClassA = new ArrayList<>();

	private final String [] baseMetricsForPackage = new String [] {"Number of Classes", "Number of Interfaces"};

	//Array where the new Metrics are added
	private ArrayList<Metrics> extraClassMetrics = new ArrayList<>();	

	private ArrayList<Metrics> extraPackageMetrics = new ArrayList<>();

	private String aux="";

	private String fileAbsoPath="";
	private ArrayList<File> ar = new ArrayList<>();
	private ArrayList<String> checklist = new ArrayList<>();
	private String packagePath = "";
	private String lastPackageVisited = "";
	private Button button1;


	private ArrayList<Button> buttonList = new ArrayList<>();

	//Array with all class metrics
	private ArrayList<Metrics> metricListClass = new ArrayList<>();

	private ArrayList<Metrics> metricListPackage = new ArrayList<>();

	@Override
	public void createContents(Composite viewArea, Map<String, Image> imageMap) {

		BundleContext context = Activator.getContext();
		ServiceReference<JavaEditorServices> serviceReference = context.getServiceReference(JavaEditorServices.class);
		JavaEditorServices PBS = context.getService(serviceReference);

		loadMetrics();

		viewArea.setLayout(new RowLayout());

		for(int i = 0; i<baseMetricsForClass.length; i++) {
			linesClassA.add(baseMetricsForClass[i]);
		}
		
		
		if(!PBS.getOpenedFile().equals(null)) {
			getProjectMetricsOnViewedClass(PBS.getOpenedFile());
			makeMetricsTableClass(viewArea);
			getProjectMetricsOnPackage();
			makeMetricsTablePackage(viewArea);
			buttonMake(viewArea,PBS.getOpenedFile());
			getNewContent();
			remaker(viewArea, PBS.getOpenedFile());
			getMetricListClass();
		}

		viewArea.layout();


		PBS.addListener(new JavaEditorListener() {

			@Override
			public void selectionChanged(File file, String text, int offset, int length) {

			}

			@Override
			public void fileSaved(File file) {

			}

			@Override
			public void fileOpened(File file) {
				reset();
				remaker(viewArea, file);
				viewArea.layout();
			}

			@Override
			public void fileClosed(File file) {

			}
		});
	}

	private void buttonMake(Composite viewArea, File file) {
		button1 = new Button(viewArea, SWT.PUSH);
		button1.setText("Write to File");

		button1.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				addNewMetricsToFile();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}



	private void countTotalLines(File file) throws IOException {

		BufferedReader buff = new BufferedReader(new FileReader(file));
		while(buff.readLine() != null) {
			for(int i = 0; i<metricListClass.size(); i++) {
				if(metricListClass.get(i).getName().equals("Total Lines of Code")) {
					metricListClass.get(i).setValue(metricListClass.get(i).getValue() + 1);
				}
			}
		}
		buff.close();
	}

	private void makeMetricsTableClass(Composite viewArea) {

		classTable = new Table(viewArea, SWT.MULTI | SWT.FULL_SELECTION);
		classTable.setLinesVisible(true);
		classTable.setHeaderVisible(true);

		for(int i = 0; i<collumns.length; i++){
			TableColumn tc = new TableColumn(classTable, SWT.PUSH);
			tc.setText(collumns[i]);
			classTable.getColumn(i).pack();
		}

		for(int i = 0; i<linesClassA.size();i++) {
			TableItem ti = new TableItem(classTable, SWT.PUSH);
			aux = String.valueOf(metricListClass.get(i).getValue());
			ti.setText(new String [] {linesClassA.get(i), aux});
		}
		classTable.setSize(classTable.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void makeMetricsTablePackage(Composite viewArea) {
		packageTable = new Table(viewArea, SWT.MULTI | SWT.FULL_SELECTION);
		packageTable.setLinesVisible(true);
		packageTable.setHeaderVisible(true);

		for(int i = 0; i<collumns.length; i++){
			TableColumn tc = new TableColumn(packageTable, SWT.PUSH);
			tc.setText(collumns[i]);
			packageTable.getColumn(i).pack();
		}

		for(int i = 0; i<metricListPackage.size();i++) {
			TableItem ti = new TableItem(packageTable, SWT.PUSH);

			aux = String.valueOf(metricListPackage.get(i).getValue());

			ti.setText(new String [] {metricListPackage.get(i).getName(), aux});
		}
		packageTable.setSize(packageTable.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}



	private void getProjectMetricsOnPackage(){

		MetricSearcher checker = new MetricSearcher();
		FileContentScanner FCS = new FileContentScanner();

		for(int i = 0 ; i<ar.size();i++) {
			FCS.parse(ar.get(i).getAbsolutePath(), checker);
		}
		ar.clear();

	}


	private void getProjectMetricsOnViewedClass(File file) {

		MetricSearcher checker = new MetricSearcher();
		FileContentScanner FCS = new FileContentScanner();

		try {
			countTotalLines(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		fileAbsoPath = file.getAbsolutePath();

		packagePath = fileAbsoPath.substring(0, file.getAbsolutePath().lastIndexOf('\\'));

		FCS.parse(fileAbsoPath, checker);

	}

	private class MetricSearcher extends ASTVisitor {

		//visit package
		@Override
		public boolean visit(PackageDeclaration node) {

			File something = new File (packagePath);
			if(!lastPackageVisited.equals(something.getName().toString())) {
				lastPackageVisited = something.getName().toString();
				for(File fi : something.listFiles()) {
					ar.add(fi);
				}
			}

			return false;
		}

		// visits class/interface declaration
		@Override
		public boolean visit(TypeDeclaration node) {
			if(!checklist.contains(node.getName().toString())) {
				for(int i = 0 ; i < metricListPackage.size(); i++) {

					if(node.isInterface()) {
						if(metricListPackage.get(i).getName().equals("Number of Interfaces")) {
							metricListPackage.get(i).setValue(metricListPackage.get(i).getValue() + 1);
						}
					}
					else {
						if(metricListPackage.get(i).getName().equals("Number of Classes")) {
							metricListPackage.get(i).setValue(metricListPackage.get(i).getValue() + 1);
						}
					}
				}
				checklist.add(node.getName().toString());
			}
			return true;
		}

		// visits attributes
		@Override
		public boolean visit(FieldDeclaration node) {

			// loop for several variables in the same declaration

			for(Object o : node.fragments()) {
				for(int i = 0; i<metricListClass.size(); i++) {
					if(Modifier.isStatic(node.getModifiers())) {
						if(metricListClass.get(i).getName().equals("Number of Static Attributes")) {
							metricListClass.get(i).setValue(metricListClass.get(i).getValue() + 1);
						}
					}
					if(metricListClass.get(i).getName().equals("Number of Attributes")) {
						metricListClass.get(i).setValue(metricListClass.get(i).getValue() + 1);
					}
				}
			}
			return false; // false to avoid child VariableDeclarationFragment to be processed again
		}

		// visits Methods

		@Override
		public boolean visit(MethodDeclaration node) {

			if(Modifier.isStatic(node.getModifiers())) {
				for(int i = 0; i<metricListClass.size(); i++) {
					if (metricListClass.get(i).getName().equals("Number of Static Methods")) {
						metricListClass.get(i).setValue(metricListClass.get(i).getValue() + 1);
					}
				}
			}

			if(node.modifiers().toString().contains("@Override")) {

				for(int i = 0; i<metricListClass.size(); i++) {
					if (metricListClass.get(i).getName().equals("Number of Overriden Methods")) {
						metricListClass.get(i).setValue(metricListClass.get(i).getValue() + 1);
					}
				}
			}

			for(int i = 0; i<metricListClass.size(); i++) {
				if (metricListClass.get(i).getName().equals("Number of Parameters")) {
					metricListClass.get(i).setValue(metricListClass.get(i).getValue() + node.parameters().size());
				}
				if (metricListClass.get(i).getName().equals("Number of Methods")) {
					metricListClass.get(i).setValue(metricListClass.get(i).getValue() + 1);
				}
			}

			return true;
		}

	}

	private void reset() {

		for(int i = 0; i<metricListPackage.size(); i++) {
			if(!extraPackageMetrics.contains(metricListPackage.get(i))) {
				metricListPackage.get(i).setValue(0);
			}
			for(int j = 0 ; j<extraPackageMetrics.size(); j++) {
				if(metricListPackage.get(i).getName().equals(extraPackageMetrics.get(j).getName())) {
					metricListPackage.get(i).setValue(extraPackageMetrics.get(j).getValue());
				}
			}
		}

		for(int i = 0; i<metricListClass.size(); i++) {
			if(!extraClassMetrics.contains(metricListClass.get(i))) {
				metricListClass.get(i).setValue(0);
			}
			for(int j = 0 ; j<extraClassMetrics.size(); j++) {
				if(metricListClass.get(i).getName().equals(extraClassMetrics.get(j).getName())) {
					metricListClass.get(i).setValue(extraClassMetrics.get(j).getValue());
				}
			}
		}
		button1.dispose();
		for(int i = 0; i < buttonList.size(); i++) {
			buttonList.get(i).dispose();
		}
		checklist.clear();
		lastPackageVisited="";
		classTable.dispose();
		packageTable.dispose();
	}
	
	
	

	private void loadMetrics() {
		
		
		String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		
		String [] aux = workspace.split("/");
		
		String aux2 = "";
		
		String aux3 = "";
		
		for(int i = 0; i<aux.length-1; i++) {
			aux2 = aux2 + aux[i] + "/";
		}
		
		aux3 = aux2;
		
		aux2 = aux2 + "pa.iscde.metrics/src/MetricsClass.txt";
		
		aux3 = aux3 + "pa.iscde.metrics/src/MetricsPackage.txt";
		
		try {
			BufferedReader scan = new BufferedReader(new FileReader(aux2));
			String thisLine;

			while((thisLine = scan.readLine())!=null) {
				Metrics m = new Metrics(thisLine, 0);
				metricListClass.add(m);
			}

			scan.close();

			scan = new BufferedReader(new FileReader(aux3));

			while((thisLine = scan.readLine())!=null){
				Metrics m = new Metrics(thisLine, 0);
				metricListPackage.add(m);
			}

			scan.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	private void addNewMetricsToFile() {

		String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		
		String [] aux = workspace.split("/");
		
		String aux2 = "";
		
		for(int i = 0; i<aux.length-1; i++) {
			aux2 = aux2 + aux[i] + "/";
		}
		
		aux2 = aux2 + "pa.iscde.metrics/src/MetricsClass.txt";
		
		try {
			File file = new File(aux2);
			PrintWriter print = new PrintWriter(file, "UTF-8");
			for(int i = 0; i < linesClassA.size(); i++) {
				print.println(linesClassA.get(i));
			}
			print.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void remaker(Composite viewArea, File file) {
		reset();
		getProjectMetricsOnViewedClass(file);
		makeMetricsTableClass(viewArea);
		getProjectMetricsOnPackage();
		makeMetricsTablePackage(viewArea);
		buttonMake(viewArea, file);
		viewArea.layout();
	}


	private void getNewContent() {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = reg.getConfigurationElementsFor("pa.iscde.metrics.extraMetrics");
		for(int i = 0 ; i < elements.length ; i++) {
			try {
				ExtraMetrics action = (ExtraMetrics) elements[i].createExecutableExtension("class");
				action.addClassMetric(extraClassMetrics);
				action.addPackageMetric(extraPackageMetrics);

				for(int j = 0; j<extraClassMetrics.size();j++) {

					if(!metricListClass.contains(extraClassMetrics.get(j))) {
						metricListClass.add(extraClassMetrics.get(j));
						linesClassA.add(extraClassMetrics.get(j).getName());
					}
				}

				for(int j = 0; j<extraPackageMetrics.size(); j++) {
					metricListPackage.add(extraPackageMetrics.get(j));
				}

			} catch (CoreException e1) {
				e1.printStackTrace();
			}

		}

	}

	public ArrayList<Metrics> getMetricListClass() {

		ArrayList<Metrics> newArray = new ArrayList<>();

		for(int i  =  0; i < metricListClass.size(); i++) {
			newArray.add(metricListClass.get(i));
		}

		return newArray;
	}

	public ArrayList<Metrics> getMetricListPackage() {

		ArrayList<Metrics> newArray = new ArrayList<>();

		for(int i  =  0; i < metricListPackage.size(); i++) {
			newArray.add(metricListPackage.get(i));
		}

		return newArray;
	}
	
	
	@Override
	public ArrayList<Metrics> getMetrics(File file){
		
		getProjectMetricsOnViewedClass(file);
		
		ArrayList<Metrics> newArray = new ArrayList<>();

		for(int i  =  0; i < metricListClass.size(); i++) {
			newArray.add(metricListClass.get(i));
		}

		return newArray;
	}

	
}
