package gr.uoi.cs.daintiness.hecate.output.tableExports;

import java.util.ArrayList;
/**
 * This Factory class generates the creators of the stats files for the tables of the schema under investigation
 * 
 * Remember that we produce (a) metrics for the entire schema, (b) metrics per table, and, (c) transitions.
 * Here we are dealing with (b): stats for individual tables. 
 * For each output file that we create, there is a respective class that concretely extends the abstract class TableMetricsExporter.
 * So, there is a collection of such classes in the package output.
 * For each of them, we have a method that creates a new object. Importantly, we also have the method <code>createExporters</code> 
 * that generates all of them 
 * 
 * @author koufos, pappas, sotiriou, pvassil
 * @version 0.3.1
 * @since unknown
 */
public class TableMetricsExporterFactory {

	public TableMetricsExporter createTableAllExporter(String path) {
		return new TableAllChangesExporter(path);
	}
	
	public TableMetricsExporter createTableInsertionExporter(String path) {
		return new TableInsertionExporter(path);
	}

	public TableMetricsExporter createTableDeletionExporter(String path) {
		return new TableDeletionExporter(path);
	}

	public TableMetricsExporter createTableTypeChangeExporter(String path) {
		return new TableTypeChangeExporter(path);
	}

	public TableMetricsExporter createTableKeyChangeExporter(String path) {
		return new TableKeyChangeExporter(path);
	}

	public TableMetricsExporter createTableSizeExporter(String path) {
		return new TableSizeExporter(path);
	}

	public TableMetricsExporter createTableStatsExporter(String path,int versions) {
		return new TableStatsExporter(path, versions);
	}

	public TableMetricsExporter createDetailedTableStatsExporter(String path,int versions) {
		return new TableDetailedStatsExporter(path, versions);
	}

	/**
	 * Creates all the file Exporters for the table metrics of the schema under investigation 
	 * and puts them into an Arraylist , s.t., ExportManager#exportTableMetrics can iterate over it and produce all reports
	 *  
	 * @param path A String with the path where the absPathOfSchemaHistoryFolder
	 * @param versions a int with the number of versions in the history of the schema
	 * @return an ArraList of materializations of the abstract class TableMetrics Exporter
	 */
	public ArrayList<TableMetricsExporter> createExporters(String path, int versions) {
		ArrayList<TableMetricsExporter> tableMetricsExporters = new ArrayList<TableMetricsExporter>();
		tableMetricsExporters.add(createTableAllExporter(path));
		tableMetricsExporters.add(createTableDeletionExporter(path));
		tableMetricsExporters.add(createTableInsertionExporter(path));
		tableMetricsExporters.add(createTableKeyChangeExporter(path));
		tableMetricsExporters.add(createTableTypeChangeExporter(path));
		tableMetricsExporters.add(createTableSizeExporter(path));
		tableMetricsExporters.add(createTableStatsExporter(path,versions));
		tableMetricsExporters.add(createDetailedTableStatsExporter(path,versions));

		return tableMetricsExporters;
	}

}
