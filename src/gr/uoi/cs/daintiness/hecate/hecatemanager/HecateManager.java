package gr.uoi.cs.daintiness.hecate.hecatemanager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import gr.uoi.cs.daintiness.hecate.diff.DiffResult;
import gr.uoi.cs.daintiness.hecate.diff.DifferenceExtractor;
import gr.uoi.cs.daintiness.hecate.diff.DifferenceExtractorFactory;
import gr.uoi.cs.daintiness.hecate.output.FileExporter;
import gr.uoi.cs.daintiness.hecate.output.FileExporterFactory;
import gr.uoi.cs.daintiness.hecate.parser.ParserFactory;
import gr.uoi.cs.daintiness.hecate.parser.SqlInputParser;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.transitions.Transitions;

public class HecateManager implements HecateAPI{

	private FileExporter exportManager;
	private SqlInputParser parser;
	private DifferenceExtractor differenceExtractor;

	private String path;
	
	public HecateManager(String path) {

		FileExporterFactory fileExporterFactory = new FileExporterFactory();
		exportManager = fileExporterFactory.createExportManger(path);

		ParserFactory parserFactory = new ParserFactory();
		parser = parserFactory.createHecateParser();

		DifferenceExtractorFactory diffExtractor = new DifferenceExtractorFactory();
		differenceExtractor = diffExtractor.createSqlDifferenceExtractor();
	
		this.path = path;
		
		// clearing the file outputErrors.txt
		if (path!= null) {
			File grandparent = (new File(path)).getParentFile();
			File directory = new File(grandparent.getAbsolutePath() + File.separator + "results");
			
			if (directory.exists()) {
				FileWriter fileWriter = null; 

				try {
					fileWriter = new FileWriter(grandparent.getAbsolutePath() + File.separator + "results"+File.separator+"outputErrors.txt");
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			    try {
					bufferedWriter.write("\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			    try {
			    	bufferedWriter.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}


	@Override
	public Schema parse(String path) {
		Schema schema = null; 
		try {
			schema = parser.parse(path);
		} catch (Exception e) {
			String s = e.toString();

			System.out.println(this.path + "________Parser exception: " + "\t" + s + "\n");
			e.printStackTrace();
		}
		return schema;
	}

	@Override
	public void export(DiffResult diffResult, Transitions transitions,
			String operation) {

		if(operation.equals("metrics")){
			exportManager.exportMetrics(diffResult);
		}
		else if(operation.equals("tables")){
			//System.out.println("HecateManager#export(): tablesInfo #tables " + diffResult.getTableInfo().getTables().size());
			//System.out.println("HecateManager#export(): tablesInfo #versions " + diffResult.getTableInfo().getNumVersions());

			exportManager.exportTableMetrics(diffResult.getMetrics().getNumRevisions()+1, 
					diffResult.getTableInfo());
		}
		else if(operation.equals("transitions")){
			exportManager.exportTransitionChanges(transitions, diffResult);
		}
		else if(operation.equals("heartbeat")){
			exportManager.exportSchemaHeartbeatFile();
		}

		else{
			try {
				throw new Exception("Wrong Operation Type! Please choose one of "
						+ "the following:\n- metrics\n- tables\n- xml ...");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public DiffResult getDifference(Schema schema1, Schema schema2) {
		return differenceExtractor.getDifference(schema1, schema2);
	}


}
