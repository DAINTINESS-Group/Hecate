package systemTest.v_0_5_1;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import gr.uoi.cs.daintiness.hecate.hecatemanager.HecateBackEndEngineFactory;
import gr.uoi.cs.daintiness.hecate.hecatemanager.IHecateBackEndEngine;

import java.io.File;
import java.io.IOException;

/**
 * @author savvasKost
 * @since v.1
 *
 */
public class HecateBackEndManagerTest_0_5_1 {
 
	// change accordingly to what folder to check!
	private static String inputDirPath = "resources/dotkernel__frontend/schemata"; 
	private static String referenceResultPath = "resources/dotkernel__frontend/results";
	private static String newResultDirPath = "resources/dotkernel__frontend/results";
 
	
	/**
	 * Prepares test to run with Atlas data set. Cleans up previous results.
	 * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		File resultDir = new File(newResultDirPath);
		String[] oldResultFiles = resultDir.list();
		
		
		int countDeleted = 0;
		for(int i=0;i<oldResultFiles.length;i++) {
			File toDel = new File(newResultDirPath + "/" + oldResultFiles[i]);
			if (toDel.delete()) countDeleted++;
		}
		System.out.println("\n\n\nCleanup of " + countDeleted + " old result files complete.");
	}


	/**
	 * Test method for {@link gr.uoi.cs.daintiness.hecate.hecatemanager.HecateBackEndEngine#handleFolderWithSchemaHistory(java.io.File)}.
	 * @throws IOException 
	 */
	@Test
	public final void testHandleFolderWithSchemaHistory() throws IOException {
		String folderOfSchemaHistory = inputDirPath;
		File folderToProcess = new File(folderOfSchemaHistory);
		
		HecateBackEndEngineFactory factory = new HecateBackEndEngineFactory();
		IHecateBackEndEngine engine = factory.createApiExecutioner(folderOfSchemaHistory);
		
		engine.handleFolderWithSchemaHistory(folderToProcess);
		System.out.println("Done with generating results for " + folderOfSchemaHistory);

		Boolean pairwiseFileComparison = true;
		//by now metrics.csv and tables_stats.csv are generated && are ready for comparison
		File refFile = new File(referenceResultPath + File.separator + "metrics.csv");
		File resFile = new File(newResultDirPath + File.separator + "metrics.csv");
		Boolean localComparison = false;
		localComparison = FileUtils.contentEquals(refFile, resFile);
		System.out.println("Identical metrics.csv?: " + localComparison + " for:\n\t" + refFile.getAbsolutePath() + "\n\t" +resFile.getAbsolutePath() + "\n");
		pairwiseFileComparison = Boolean.logicalAnd(pairwiseFileComparison, localComparison);

		refFile = new File(referenceResultPath + File.separator + "table_stats.csv");
		resFile = new File(newResultDirPath + File.separator + "table_stats.csv");
		localComparison = FileUtils.contentEquals(refFile, resFile);
		System.out.println("Identical table_stats.csv?: " + localComparison + " for:\n\t" + refFile.getAbsolutePath() + "\n\t" +resFile.getAbsolutePath() + "\n");
		pairwiseFileComparison = Boolean.logicalAnd(pairwiseFileComparison, localComparison);

		
		refFile = new File(referenceResultPath + File.separator + "tables_DetailedStats.tsv");
		resFile = new File(newResultDirPath + File.separator + "tables_DetailedStats.tsv");
		localComparison = FileUtils.contentEquals(refFile, resFile);
		System.out.println("Identical tableDetailedStats.tsv?: " + localComparison + " for:\n\t" + refFile.getAbsolutePath() + "\n\t" +resFile.getAbsolutePath() + "\n");
		pairwiseFileComparison = Boolean.logicalAnd(pairwiseFileComparison, localComparison);


		refFile = new File(referenceResultPath + File.separator + "SchemaHeartbeat.tsv");
		resFile = new File(newResultDirPath + File.separator + "SchemaHeartbeat.tsv");
		localComparison = FileUtils.contentEquals(refFile, resFile);
		System.out.println("Identical SchemaHeartbeat.tsv?: " + localComparison + " for:\n\t" + refFile.getAbsolutePath() + "\n\t" +resFile.getAbsolutePath() + "\n");
		pairwiseFileComparison = Boolean.logicalAnd(pairwiseFileComparison, localComparison);

		assertEquals(pairwiseFileComparison, true);

	}//end testHandleFolderWithSchemaHistory()

}//end class
