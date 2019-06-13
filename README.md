# Hecate

Hecate is a tool to extract the history of relational database schema.

The input needed is a folder with the snapshots of the DDL files that include the <code>CREATE TABLE</code> statements of the databases.
The output is the extraction of transitions from one snapshot ("version") to its next, along with statistics on the types of changes.
 
## Usage
Use the jar's of the <code>target</code> folder. Specifically:
* **HecateRunable** is the jar that executes the <code>Hecate.java</code> with the GUI

	* You can either compare two SQL files, or, 
	* Extract the history of an entire folder 9and see the metrics too)
	
* **HecateFolderProcessing** is a jar for parsing an entire folder from the command line, without the GUI

	 * Assume you have the project <code>PRJ</code> its should contain a folder <code>PRJ/schemata</code> with the schema history.	 
	 * You should pass the <code>PRJ/schemata</code> as the parameter needed at args[0] 
  	
## License
See the [copyright](copyright.md) file.	


## Credits and history

**v.04 [2019-06]**

Refactoring of back-end to have an API, client for cmd-line invocation, new stats for tables.
* *Panos Vassiliadis* 

**v.03 [2017]**

Gang of 3 refactors Hecate with cleanups and restructuring (esp. metrics)
* *Nikos Koufos*
* *Thanos Pappas*
* *Michalis Sotiriou*

**v.01, v.02 [2013]**
* *Ioannis Skoulis* creates the first version of Hecate. 


## ToDo

Refactorings and improvements
- [ ] Refactor the IHecateBackEndEngine interface. Too inclusive, to allow the DiffWorker of the GUI to incrementally report on its progress. Extract a small, tight mama interface (mainly for folder processing) and keep the current one for the GUI
- [ ] Once this is done, consider merging intermediate steps in the GUI, to restrict the necessary method calls  

Extensions
- [ ] More stats as metrics
- [ ] Detect renamings as a first class citizen (SMO) operator