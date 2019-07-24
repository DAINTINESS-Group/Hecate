package gr.uoi.cs.daintiness.hecate.output.tableExports;

import gr.uoi.cs.daintiness.hecate.metrics.tables.Changes;
import gr.uoi.cs.daintiness.hecate.metrics.tables.MetricsOverVersion;


public class TableInsertionExporter extends TableMetricsExporter{

	
	public TableInsertionExporter(String path){
		super(path, "_perVersion_tables_AttrIns.csv");
	}
	
	@Override
	public void writeChanges(MetricsOverVersion metricsOverVersion, int i) {
		Changes c = metricsOverVersion.getChanges(i);
		writeText(c.getInsertions() + ";");
	}

	@Override
	public void writeLastColumn() {
		writeText("\n");	
	}

	@Override
	public void writeEmptyCells() {
		writeText("-;");
	}

	@Override
	public void writeHeader(int versions) {
		writeVersionsLine(versions);
	}
}
