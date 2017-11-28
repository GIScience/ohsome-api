package org.heigit.bigspatialdata.ohsome.springBootWebAPI.eventHolder;

import java.sql.SQLException;

import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_H2;
import org.springframework.stereotype.Component;


/**
 * Bean class, which is used to establish a database connection on startup of the server.
 * @author kowatsch
 *
 */
@Component
public class EventHolderBean {
	/**
	 * 0: oshdb 1: keytables
	 */
	private OSHDB_H2[] dbConnObjects;
    
	/**
	 * Database connection method.
	 * 
	 * @param dbPath
	 *            Path to the database where to perform the search.
	 * @param keytablesPath
	 *            Path to the keytables database.
	 * @param multiThread
	 *            Parameter to apply multithreaded processing (true) or not (false).
	 */
	public void dbConn(String dbPath, String keytablesPath, boolean multiThread) {
		OSHDB_H2 oshdb;
		OSHDB_H2 oshdbKeytables;
		dbConnObjects = new OSHDB_H2[2];

		// check if the parameters are empty
		if (dbPath == null || dbPath.isEmpty() || keytablesPath == null || keytablesPath.isEmpty()) {
			throw new RuntimeException("Paths to database cannot be null or empty.");
		}

		try {
			oshdb = (new OSHDB_H2(dbPath)).multithreading(multiThread);
			oshdbKeytables = new OSHDB_H2(keytablesPath);
			dbConnObjects[0] = oshdb;
			dbConnObjects[1] = oshdbKeytables;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	public OSHDB_H2[] getDbConnObjects() {
		return dbConnObjects;
	}
}
