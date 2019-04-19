package systemTest;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import gr.uoi.cs.daintiness.hecate.gui.swing.DiffWorker;

public class DiffWorkerTest {

	private static DiffWorker task;
	private static String referenceDir = "resources/Egee/referenceResults_v0.3.1";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String path = "resources/Egee/schemata";
		File directory = new File(path);
		task = new DiffWorker(null, directory);
	
	}

	@Test
	public final void testDiffWorkerMainPanelFile() {
		try {
			String resultDirPath = "resources/Egee/results";
			File resultDir = new File(resultDirPath);

			//kill old results
			String[] oldResultFiles = resultDir.list();
			int countDeleted = 0;
			for(int i=0;i<oldResultFiles.length;i++) {
				File toDel = new File(resultDirPath + "/" + oldResultFiles[i]);
				if (toDel.delete()) countDeleted++;
			}
			System.out.println("Cleanup of " + countDeleted + " old result files complete.");
			
			task.processFolderInBackground(null);
			
			String [] resultFiles = resultDir.list();
			java.util.Arrays.sort(resultFiles);
			String refResultDirPath = referenceDir;
			File refResultDir = new File(refResultDirPath);
			String[] refResultFiles = refResultDir.list();
			java.util.Arrays.sort(refResultFiles);
			
			Boolean pairwiseFileComparison = true;
			for(int i=0;i<refResultFiles.length;i++) {
				String refFilePath = refResultDirPath + "/" + refResultFiles[i];
				String resFilePath = resultDirPath + "/" + refResultFiles[i];//resultFiles[i];
				System.out.print("Comparing " + refFilePath + " to " + resFilePath + "\t\t");
				File refFile = new File(refFilePath);
				File resFile = new File(resFilePath);
				Boolean localComparison = FileUtils.contentEquals(refFile, resFile);
				System.out.println(localComparison);
				
				pairwiseFileComparison = Boolean.logicalAnd(pairwiseFileComparison, localComparison);
			}
			
			//TODO: Fix that the metrics.csv is outputed WITHOUT a header!
			assertEquals(pairwiseFileComparison, true);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
}
