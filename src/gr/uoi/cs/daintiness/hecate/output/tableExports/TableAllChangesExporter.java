package gr.uoi.cs.daintiness.hecate.output.tableExports;

import gr.uoi.cs.daintiness.hecate.metrics.tables.Changes;
import gr.uoi.cs.daintiness.hecate.metrics.tables.MetricsOverVersion;

public class TableAllChangesExporter extends TableMetricsExporter{

	public TableAllChangesExporter(String path) {
		super(path, "_perVersion_tables_ConcatAllMetrics.csv");
	}

	@Override
	public void writeChanges(MetricsOverVersion metricsOverVersion, int i) {
		Changes c = metricsOverVersion.getChanges(i);
		writeText(metricsOverVersion.getSize(i) + "[" + c.toString() +
		"]" + ";");
	}

	@Override
	public void writeLastColumn() {
		writeText("\n");
	}

	@Override
	public void writeEmptyCells() {
		writeText("0|-|-|-|-;");
		
	}

	@Override
	public void writeHeader(int versions) {
		super.writeVersionsLine(versions);
		
	}

}
