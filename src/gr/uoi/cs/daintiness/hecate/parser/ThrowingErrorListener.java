package gr.uoi.cs.daintiness.hecate.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class ThrowingErrorListener extends BaseErrorListener {

	private String fileName;
	private String filePath;
	private HashMap<String,String> errorMap;
	
	public ThrowingErrorListener(String fileName,String filePath,HashMap<String,String> errorMap) {
		this.fileName = fileName;
		this.filePath = filePath;
		this.errorMap = errorMap;
	}
	
	// this method prints the messages for each error that arises.
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
		String errorMessage = "";  // in this temporary string will be stored the text of the error in order to be written in the file
		// open the file in order to write the error that has risen
		
		File f = new File(filePath);
		FileWriter fileWriter = null; 
		try {
			fileWriter = new FileWriter(f, true);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		String offendingText = offendingSymbol.toString();
		
		ArrayList<Integer> quotePlaces = new ArrayList<Integer>();
		for (int i = 0; i < offendingText.length(); i++) {
			if (offendingText.charAt(i) == '\'') {
				quotePlaces.add(i);
			}
		}
		
		if (quotePlaces.size() == 4) {
			offendingText = offendingText.substring(quotePlaces.get(0) + 1, quotePlaces.get(quotePlaces.size() -1)); 
		}else {
			offendingText = offendingText.substring(offendingText.indexOf("'") + 1);
			offendingText = offendingText.substring(0, offendingText.indexOf("'"));
		}	
		
		// check if exists in error Map and if so write the appropriate warning message
		if (errorMap.containsKey(offendingText)) {
	    	errorMessage = "File: "+fileName+" Line: "+line + errorMap.get(offendingText)+ " Keyword: '"+offendingText+"'";
	    }else {
	    	errorMessage = "File: "+fileName+" Line: "+line+" Error: The keyword '"+offendingText+"'"+" is invalid. Keyword: '"+offendingText+"'" + ",[minor]";
	    }
		
		
		// Writes the content to the file
		
	    try {
			bufferedWriter.write(errorMessage + "\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    try {
			bufferedWriter.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
