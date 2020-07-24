package gr.uoi.cs.daintiness.hecate.parser;

import gr.uoi.cs.daintiness.hecate.sql.Attribute;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.sql.Table;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;


/**
 * This class uses the parser of the generated ANTLR grammar to parse
 * the file given to the constructor.
 * @author giskou
 */
public class HecateParser implements SqlInputParser{
	static Schema s;
	ThrowingErrorListener throwListener;
	
	static class UnMatched {
		Table orT;
		DDLParser.ForeignContext ctx;
		
		public UnMatched(Table orT, DDLParser.ForeignContext ctx) {
			this.orT = orT;
			this.ctx = ctx;
		}
	}

	
	/**
	 * 
	 * @param filePath The path of the file to be parsed.
	 * @throws IOException
	 * @throws RecognitionException
	 */
	public Schema parse(String filePath){
		CharStream      charStream = null;
		File file = new File(filePath);
		
		File grandparent = (new File(filePath)).getParentFile().getParentFile();
		File directory = new File(grandparent.getPath() + File.separator + "results");
		if (!directory.exists()) {
			directory.mkdir();
		}
		
			
		// ---------------- printing all errors in a separate text file with name outputErrors.txt . This file is created inside the folder of the examined files.
		
		String outputFilePath = directory.getPath() + "\\" + "outputErrors.txt";
		HashMap<String, String> errorMap = fillMap(file.getName());  // this function creates and fills the error HashMap  that is used inside ThrowingErrorListener Class
		throwListener = new ThrowingErrorListener(file.getName(),outputFilePath,errorMap);
		
		try {
			charStream = new ANTLRFileStream(filePath);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		DDLLexer        lexer = new DDLLexer(charStream);
		// --------------------------------------------------
		lexer.removeErrorListeners();
		lexer.addErrorListener(throwListener);
		// -------------------------------------------------- 
		
		
		TokenStream     tokenStream = new CommonTokenStream(lexer);
		DDLParser       parser = new DDLParser(tokenStream);
		// --------------------------------------------------
		parser.removeErrorListeners();
		parser.addErrorListener(throwListener);
		// -------------------------------------------------- 
		
		ParseTree       root = parser.start();
		SchemaLoader    loader = new SchemaLoader(file.getName(),outputFilePath);
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(loader, root);
		
		s.setTitle(file.getName());
		
		return s;
	}
	
	// this method fills the hashmap of errors with the appropriate text
	public static HashMap<String,String> fillMap(String fileName){
		HashMap<String,String> errorMap = new HashMap<String,String>();
		String invalidComText = " Error: This command is not allowed in the Hecate Tool. [major]";
		String createAlterText = " Error: Commands 'CREATE' and 'ALTER' do not accept this keyword. [major]";
		String keywordText = " Error: This keyword is not allowed in the Hecate Tool. [minor]";
		String keywordMax = " Error: The keyword 'MAX' is not valid in the variable size definition. [major]";
		String keywordDollar = " Error: The keyword '$' can only be used as a special character and not inside a command. [minors]";
		String keywordEqual = " Error: The keyword '=' is not allowed with 'DEFAULT' and 'SET' keywords inside a command. [major]";
		String keywordBSize = " Error: The keyword 'KEY_BLOCK_SIZE'" +" is not valid. Perhaps you meant 'KEY_BSIZE' instead. [major]";
		String keywordCom = " Error: The keyword ','" +" is not allowed inside the command. [minor]";
		
		errorMap.put("SELECT",invalidComText);errorMap.put("SHOW",invalidComText);errorMap.put("BEGIN",invalidComText);errorMap.put("START",invalidComText);errorMap.put("DELETE",invalidComText);
		errorMap.put("REVOKE",invalidComText);errorMap.put("DO",invalidComText);errorMap.put("GRANT",invalidComText);errorMap.put("ANALYZE",invalidComText);
		errorMap.put("SEQUENCE",createAlterText);errorMap.put("TYPE",createAlterText);errorMap.put("EXTENSION",createAlterText);errorMap.put("SCHEMA",createAlterText);
		errorMap.put(".","");errorMap.put("on","");errorMap.put("NONCLUSTERED","");errorMap.put("MODIFY","");errorMap.put("PROCEDURE","");errorMap.put("USING","");
		errorMap.put("|",keywordText);errorMap.put("?",keywordText);errorMap.put("[",keywordText);errorMap.put("]",keywordText);
		errorMap.put(";",keywordText);errorMap.put("IDENTITY",keywordText);errorMap.put("MAX",keywordMax);errorMap.put("$",keywordDollar);errorMap.put("=",keywordEqual);
		errorMap.put("KEY_BLOCK_SIZE",keywordBSize);errorMap.put(",",keywordCom);
		
		return errorMap;
	}

	private static class SchemaLoader extends DDLBaseListener {
		
		private String[] tokenNames = {
				"<INVALID>", "STRING_G", "STRING_Q", "STRING_D", "WS", "COMMENT", "LINE_COMMENT", 
				"'='", "'('", "')'", "';'", "','", "'''", "'_'", "'@'", "'-'", "'#'", 
				"'/'", "'*'", "'\"'", "'`'", "':'", "'.'", "' '", "'$'", "'|'", "'+'", 
				"'<<<<<<<'", "'>>>>>>>'", "CONFLICT", "ACTION", "ADD", "ALTER", "AS", 
				"ASC", "AUTO_INC", "BIGINT", "BINARY", "BIT", "BTREE", "CASCADE", "CHARACTER", 
				"CHAR", "CHECK", "COLLATE", "COL_FORMAT", "COMMIT", "CONSTRAINT", "CREATE", 
				"DATABASE", "DECIMAL", "DEFAULT", "DEFERRABLE", "DELETE", "DESC", "DISK", 
				"DOUBLE", "DROP", "DYNAMIC", "END", "ENUM", "EXISTS", "FIXED", "FOREIGN", 
				"FULLTEXT", "FUNCTION", "HASH", "IF", "IGNORE", "INDEX", "INSERT", "INTEGER", 
				"INTO", "IS", "KEY", "KEY_BSIZE", "MEMORY", "NO", "NOT", "NULL", "NUMERIC", 
				"ON", "OR", "PRECISION", "PROCEDURE", "PRIMARY", "PURGE", "REAL", "REFERENCES", 
				"REPLACE", "RESTRICT", "SCHEMA", "SET", "SIGNED", "SMALLINT", "SPATIAL", 
				"STORAGE", "TABLE", "TEMPORARY", "TRIGGER", "UNIQUE", "UNSIGNED", "UPDATE", 
				"USE", "USING", "VALUES", "VARBINARY", "VARCHAR", "VIEW", "WHERE", "YEAR", 
				"ZEROFILL", "INT", "ID", "OTHER"
		};

		private String fileName;
		private String outputFilePath = "";   // this is the path of the file that we write the errors
		private Table t;
		private Attribute a;
		boolean foundTableConst = false;
		boolean foundAlterConst = false;
		boolean foundLineConst = false;
		String alteringTable = null;
		List<UnMatched> unMached = new ArrayList<HecateParser.UnMatched>();

		
		public SchemaLoader(String fileName,String outputFilePath) {
			this.fileName = fileName;
			this.outputFilePath = outputFilePath;
		}
		
		
		public void enterStart (DDLParser.StartContext ctx) {
			s = new Schema();
		}
		public void exitStart (DDLParser.StartContext ctx) {
			processUnmached();
		}

		public void enterTable (DDLParser.TableContext ctx) {
			String tableName = removeQuotes(ctx.table_name().getText());
			t = new Table(tableName);
			int indexName = ctx.getText().indexOf(ctx.table_name().getText());
			
			if (tableName.contains(".")) {
				showError(fileName,ctx.start.getLine(),"Error: Packages are not allowed in Hecate Tool. [major] ");
			}
			if (ctx.getText().charAt(indexName+ctx.table_name().getText().length()) == '.') {         // check if that place has a dot (.). IF it has then packages which are not allowed
				showError(fileName,ctx.start.getLine(),"Error: Packages are not allowed in Hecate Tool. [major] ");
			}
			
		}
		public void exitTable (DDLParser.TableContext ctx) {
			s.addTable(t);
		}

		public void enterColumn (DDLParser.ColumnContext ctx) {
			if (ctx.getText().contains("CHECK")) {
				showError(fileName,ctx.start.getLine(),"Error: Keyword 'CHECK' is not valid in a column field keyword. [major] ");
			}
			String colName = removeQuotes(ctx.col_name().getText());
			String colType = ctx.data_type().getText();
			colType = colType.toUpperCase();
			a = new Attribute(colName, colType);
		}

		public void exitColumn (DDLParser.ColumnContext ctx) {
			t.addAttribute(a);
		}

		public void enterLine_constraint(DDLParser.Line_constraintContext ctx) {
			foundLineConst = true;
		}
		public void exitLine_constraint(DDLParser.Line_constraintContext ctx) {
			foundLineConst = false;
		}
		
		
		@Override 
		public void enterView(@NotNull DDLParser.ViewContext ctx) {
			if (ctx.getText().contains("ifNOTexists") || ctx.getText().contains("ifnotexists") || ctx.getText().contains("IFNOTEXISTS") ) {
				showError(fileName,ctx.start.getLine(),"Error: Create View statement does not support IF NOT EXISTS option. [major] ");
			}
		}
		
		
		@Override
		public void enterSet(@NotNull DDLParser.SetContext ctx) {			
			
			if (ctx.EQ() != null) {
				if (ctx.ident() != null && ctx.ident().get(1) != null) {    // ctx.ident().get(1) is everything after the '=' keyword in set
					if (Character.isDigit(ctx.ident().get(1).getText().charAt(0))) {
						showError(fileName,ctx.start.getLine(),"Error: Keyword '"+ctx.ident().get(1).getText().charAt(0)+"'."+"Digits outside quotes are not allowed in a 'SET' command. [major] ");
					} else if (ctx.ident().get(1).getText().equals("on")) {
						showError(fileName,ctx.start.getLine(),"Error: Keyword '"+ctx.ident().get(1).getText()+"'."+"The keyword 'ON' is not allowed in a 'SET' command. [minor] ");
					}
				}
			}
		}
		
		
		@Override 
		public void enterCreate_statement(@NotNull DDLParser.Create_statementContext ctx) {
			
			if (ctx.getText().contains("TYPE")) {
				showError(fileName,ctx.start.getLine(),"Error: Command CREATE does not support action TYPE, only {'TABLE','DATABASE','VIEW','TRIGGER','INDEX','pl_sql'}. [major] ");
			}
			
			if (ctx.getText().contains("SEQUENCE")) {
				showError(fileName,ctx.start.getLine(),"Error: Command CREATE does not support action SEQUENCE, only {'TABLE','DATABASE','VIEW','TRIGGER','INDEX','pl_sql'}. [major] ");
			}
			
			
			if (ctx.getText().contains("PROCEDURE")) {
				showError(fileName,ctx.start.getLine(),"Error: Command CREATE does not support action PROCEDURE, only {'TABLE','DATABASE','VIEW','TRIGGER','INDEX','pl_sql'}. [major] ");
			}
			
			// the below if statement checks if any of the following rules activated,and if not then error creation
			if (ctx.table() == null && ctx.pl_sql() == null && ctx.database() == null && ctx.triger() == null && ctx.index() == null && ctx.view() == null) {
				showError(fileName,ctx.start.getLine(),"Error: Command CREATE does not support this action, only {'TABLE','DATABASE','VIEW','TRIGGER','INDEX','pl_sql'}. [major] ");
			}
		}
		
		
		
		@Override
		public void enterIndex(@NotNull DDLParser.IndexContext ctx) {
			if (ctx.getText().contains("ifNOTexists") || ctx.getText().contains("ifnotexists") || ctx.getText().contains("IFNOTEXISTS") ) {
				showError(fileName,ctx.start.getLine(),"Error: Create Index statement does not support IF NOT EXISTS option. [major]");
			}
			
			if (ctx.parNameList() != null) {
				String parNameFixed = ctx.parNameList().getText().substring(1, ctx.parNameList().getText().length()-1);  // skip the parenthesis
				String[] parNameVariables = parNameFixed.split(",");
				
				
				for (int i = 0; i < parNameVariables.length; i++) {
					if (checkTokenList(tokenNames,parNameVariables[i].toUpperCase())) {
						showError(fileName,ctx.start.getLine(),"Error: Maybe missing quotes (`  `) in command CREATE INDEX. [minor] ");
					}
				}
			}
			
			if (ctx.getText().contains("NONCLUSTERED") || ctx.getText().contains("nonclustered")) {
				showError(fileName,ctx.start.getLine(),"Error: Create Index statement does not support 'NONCLUSTERED' option. [major] ");
			}
			
			// the below code checks the validity of the using option inside the create index command.
			if (ctx.getText().contains("USING")) {
				int usingIndex = ctx.getText().indexOf("USING");
				String usingText =  ctx.getText().substring(usingIndex -1 , ctx.getText().length()); 
				checkUSINGvalidity(fileName,ctx.start.getLine(),usingText);
			}
		}
		
		@Override 
		public void enterDrop_statement(@NotNull DDLParser.Drop_statementContext ctx) {
			
			if (ctx.getText().contains("PROCEDURE")) { 
				showError(fileName,ctx.start.getLine(),"Error: Command DROP does not support action PROCEDURE, only {'TABLE','DATABASE'}. [major] ");
			}
			
			
			if (ctx.nameList() != null) {
				if ((ctx.nameList().getText()).contains(".")) {
					showError(fileName,ctx.start.getLine(),"Error: Packages are not allowed in Hecate Tool. Command 'DROP'. [major] ");
				}
			}
		}
		
		
		public void enterAlter_statement(DDLParser.Alter_statementContext ctx) {
			
			if (ctx.getText().contains("MODIFY")) {
				showError(fileName,ctx.start.getLine(),"Error: SQL ALTER command does not support keyword 'MODIFY'. [major] ");
			}
			
			
			if (ctx.ADD().size() > 1) {
				showError(fileName,ctx.start.getLine(),"Error: SQL ALTER command does not accept multiple ADD CONSTRAINT commands. [major] ");
			}
			
			if (ctx.getText().contains(".")) {
				showError(fileName,ctx.start.getLine(),"Error: SQL ALTER command does not support packages in the table name. [major] ");
			}
			
			if (ctx.getText().contains("ONLY") || ctx.getText().contains("only")) {
				showError(fileName,ctx.start.getLine(),"Error: Keyword 'ONLY' is not allowed after the command 'ALTER'. "+ctx.getText() +" [major] ");
			}
						
			alteringTable = ctx.table_name().getText();
		}
		public void exitAlter_statement(DDLParser.Alter_statementContext ctx) {
			alteringTable = null;
		}

		public void enterTable_constraint(DDLParser.Table_constraintContext ctx) {
			foundTableConst = true;
		}
		public void exitTable_constraint(DDLParser.Table_constraintContext ctx) {
			foundTableConst = false;
		}

		public void enterAlter_constraint(DDLParser.Alter_constraintContext ctx) {
			foundAlterConst = true;
		}
		public void exitAlter_constraint(DDLParser.Alter_constraintContext ctx) {
			foundAlterConst = true;
		}

		public void enterPrimary (DDLParser.PrimaryContext ctx) {
			if (foundTableConst) {
				String todo = ctx.parNameList().getText();
				todo = todo.substring(1, todo.length()-1);
				String[] names = todo.split(",");
				for (String s : names) {
					if (t.getAttrs().get(s) != null){
						t.addAttrToPrimeKey(t.getAttrs().get(s));
					}
				}
			} else if (foundAlterConst) {
			} else if (foundLineConst){
				t.addAttrToPrimeKey(a);
			} else {}
		}

		public void enterForeign (DDLParser.ForeignContext ctx) {
			Table orTable = null, reTable = null;
			Attribute[] or, re;
			String reTableName = ctx.reference_definition().table_name().getText();
			if (foundTableConst) {
				orTable = t;
				if (reTableName.compareTo(orTable.getName()) == 0) {
					reTable = t;
				} else {
					reTable = s.getTables().get(reTableName);
					if (reTable == null) {
						unMached.add(new UnMatched(orTable, ctx));
						return;
					}
				}
			} else if (foundAlterConst) {  // savvas work
				
				// this if statement checks if there is any keyword between the alter table command and the table name
				if (!checkEquality(s.getTables().keySet(),removeQuotes(alteringTable))) {
					String context = (ctx.getRuleContext().getParent()).getParent().getText();
					String contextTail  = (ctx.getRuleContext().getParent()).getParent().getText();
					context = context.substring(0, context.indexOf(alteringTable));
					context+=((ctx.getRuleContext().getParent()).getParent().getText()).
							substring(contextTail.indexOf(alteringTable)+alteringTable.length(),contextTail.length());
					alteringTable = getTableNameFromRelation(context,s.getTables().keySet());
				}
				
				orTable = s.getTables().get(removeQuotes(alteringTable));
				reTable = s.getTables().get(removeQuotes(reTableName));
			} else {
				// this is not supposed to happen

			}
			or = getNames(ctx.parNameList().getText(), orTable);
			re = getNames(ctx.reference_definition().parNameList().getText(), reTable);
			for (int i = 0; i < or.length; i++) {
				if (or[i] == null || re[i] == null) {
					// sql typo???
					continue;
				}
//				System.out.println(orTable + "." + or[i] + "->" + reTable + "." + re[i] + "\n");
				orTable.getForeignKey().addReference(or[i], re[i]);
			}
		}

		public void enterReference (DDLParser.ReferenceContext ctx) {
			Table orTable = t;
			Table reTable = s.getTables().get(ctx.reference_definition().table_name().getText());
			Attribute or = a;
			Attribute[] re = getNames(ctx.reference_definition().parNameList().getText(), reTable);
			orTable.getForeignKey().addReference(or, re[0]);
		}

		private void processUnmached() {
			for (UnMatched item : unMached) {
				DDLParser.ForeignContext ctx = item.ctx;
				Table orTable = item.orT;
				String reTableName = ctx.reference_definition().table_name().getText();
				Table reTable = s.getTables().get(reTableName);
				if (reTable == null) {
					// still not found ... ignore
					continue;
				}
				Attribute[] or = getNames(ctx.parNameList().getText(), orTable);
				Attribute[] re = getNames(ctx.reference_definition().parNameList().getText(), reTable);
				for (int i = 0; i < or.length; i++) {
					if (or[i] == null || re[i] == null) {
						// sql typo???
						continue;
					}
//					System.out.println(orTable + "." + or[i] + "->" + reTable + "." + re[i] + "\n");
					orTable.getForeignKey().addReference(or[i], re[i]);
				}
			}
		}

		private Attribute[] getNames(String s, Table table) {
			s = s.substring(1, s.length()-1);
			String[] names = s.split(",");
			Attribute[] res = new Attribute[names.length];
			for (int i = 0; i < names.length; i++) {
				res[i] = table.getAttrs().get(names[i]);
			}
			return res;
		}

		private boolean hasQuotes(String s) {
			switch (s.charAt(0)) {
				case '\'':
				case '\"':
				case '`':
					return true;
				default:
					return false;
			}
		}

		private String removeQuotes(String s) {
			if (hasQuotes(s)) {
				String res = null;
				res = s.substring(1, s.length()-1);
				return res;
			}
			return s;
		}
		
		// method  to handle alter table command with foreign key
		private String getTableNameFromRelation(String context, Set<String> tablesSet) {
			String result = "";
			String con = "ALTERTABLE";
						
			
			if (context.contains("ALTERTABLE") && context.contains("ADDCONSTRAINT")) {
				result = context.substring(context.indexOf("ALTERTABLE")+con.length(),context.indexOf("ADDCONSTRAINT"));
			}else if (context.contains("altertable") && context.contains("constraint")) {
				result = context.substring(context.indexOf("altertable")+con.length(),context.indexOf("addconstraint"));
			}else if (context.contains("altertable") && context.contains("ADDCONSTRAINT")) {
				result = context.substring(context.indexOf("altertable")+con.length(),context.indexOf("ADDCONSTRAINT"));
			}else if (context.contains("ALTERTABLE") && context.contains("constraint")) {
				result = context.substring(context.indexOf("ALTERTABLE")+con.length(),context.indexOf("addconstraint"));
			}
			
			if (checkEquality(tablesSet,removeQuotes(result))) {
    			return result;
    		}else {   // this is not suppose to happen
    			System.out.println("Error: Command ALTER TABLE is not parsed correctly!");
    			System.exit(1);
    		}
			return "ERROR";
		}
		
		
		// this method checks if the table name is actually a table name!
		private static boolean checkEquality(Set<String> tablesSet, String tableName) {
			boolean flag = false;
			for (String table : tablesSet) {
				if (table.equals(tableName)) {
					flag = true;
					break;
				}
			}
			return flag;
		}
		
		
		private void checkUSINGvalidity(String fileName,int lineNumber,String usingText) {
			if (usingText.charAt(0) != '(' || usingText.contains("missing") || usingText.charAt(usingText.length() - 1) != ')') {
				showError(fileName,lineNumber,"Error: USING option is not spelled correctly. The right syntax is this: (USING(BTREE|HASH)), [major] ");
			}
		}
		
		// this method shows each given error
		private void showError(String fileName,int line,String messageError) {
			String errorMessage = "File: "+fileName+" "+"Line: "+line+" "+messageError;
			
			
			FileWriter fileWriter = null; 
			try {
				fileWriter = new FileWriter(outputFilePath, true);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		    
		    
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
		
		// this method checks if the token is a bound word inside the token names.
		private static Boolean checkTokenList(String[] boundTokens, String offText) {
			Boolean flag = false;
			for (int i=0; i < boundTokens.length; i++) {
				if (boundTokens[i].equals(offText)) {
					return true;
				}
			}
			return flag;
		}
	}
}
