/**
 * 
 */
package gr.uoi.cs.daintiness.hecate.metrics.tables;

import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author iskoulis
 *
 */
public class MetricsOverVersion extends TreeMap<Integer, TableMetrics> {

	private static final long serialVersionUID = 6222279078841584012L;
	
	private Changes lastVersionChanges = null; 
	private int totalUpdVersions = 0;
	
	public Changes getChanges(int version) {
		return get(version).getChanges();
	}

	public int getSize(int version) {
		return get(version).getSize();
	}

	public int getBirth() {
		return this.firstKey();
	}

	public int getLastKnownVersion() {
		return this.lastKey();
	}

	public int getLife() {
		return this.size();
	}

	public int getBirthSize() {
		return this.firstEntry().getValue().getSize();
	}

	public int getLastKnownVersionSize() {
		return this.lastEntry().getValue().getSize();
	}

	public Changes getTotalChanges() {
		if (this.lastVersionChanges != null) return this.lastVersionChanges;
		Changes c = new Changes();
		for (Entry<Integer, TableMetrics> e : this.entrySet() ) {
			Changes versionChanges = e.getValue().getChanges();
			c.addInsertion(versionChanges.getInsertions());
			c.addDeletion(versionChanges.getDeletions());
			c.addAttrTypeChange(versionChanges.getAttrTypeChange());
			c.addKeyChange(versionChanges.getKeyChange());
		}
		this.lastVersionChanges = c;
		return this.lastVersionChanges;
	}//end getTotalChanges
	
	
	/**
	 * Returns the number of transitions that included any change for the table under investigation
	 *
	 * Passes all changes of the table's TableMetrics list and checks their total changes
	 *  
	 * @return an int with the total number of versions for which the table underwent changes
	 */
	public int getNumberOfVersionsWithChanges() {
		for (Entry<Integer, TableMetrics> e : this.entrySet() ) {
			Changes versionChanges = e.getValue().getChanges();
			if (versionChanges.getTotal() > 0)
				this.totalUpdVersions ++ ;
		}
		return this.totalUpdVersions;
	}
}//end class
