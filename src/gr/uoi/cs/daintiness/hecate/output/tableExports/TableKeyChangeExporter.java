package gr.uoi.cs.daintiness.hecate.output.tableExports;

import gr.uoi.cs.daintiness.hecate.metrics.tables.Changes;
import gr.uoi.cs.daintiness.hecate.metrics.tables.MetricsOverVersion;

public class TableKeyChangeExporter extends TableMetricsExporter{

	public TableKeyChangeExporter(String path) {
		super(path, "_perVersion_tables_AttrKeyUpd.csv");
	}

	@Override
	public void writeChanges(MetricsOverVersion metricsOverVersion, int i) {
		Changes c = metricsOverVersion.getChanges(i);
		writeText(c.getKeyChange() + ";");	
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
