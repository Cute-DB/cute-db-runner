/**
 *
 */
package io.github.cutedb.runner.utils;

/*
 * #%L
 * nppb-v2
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 Mairie de Nouméa
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import liquibase.Liquibase;
import liquibase.database.core.H2Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitaire pour l'initialisation de la base de test
 *
 * @author barmi83
 */
public class H2SqlDatabase {
	private static final String CHANGE_LOG_INIT = "src/test/resources/db.changelog-initdata.xml";

	public static final String CONNECTION_STRING = "jdbc:h2:mem:runnerTest;SCHEMA=PUBLIC;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0";
	public static final String DBA_USER_NAME = "sa";
	public static final String DBA_PASSWORD = "";
	public static final Logger LOG = LoggerFactory.getLogger(H2SqlDatabase.class);

	private Liquibase liquibase;

	public void setUp(String contexts) {
		try {
			initData(contexts);
		} catch (Exception ex) {
			LOG.error("Error during database initialization", ex);
			throw new RuntimeException("Error during database initialization", ex);
		}
	}

	private Connection getConnectionImpl(String user, String password) throws SQLException {
		return DriverManager.getConnection(CONNECTION_STRING, user, password);
	}

	// Init DB, create users, schemas...
	private void initData(String contexts) {
		Connection holdingConnection;

		try {
			ResourceAccessor resourceAccessor = new FileSystemResourceAccessor();

			holdingConnection = getConnectionImpl(DBA_USER_NAME, DBA_PASSWORD);
			JdbcConnection conn = new JdbcConnection(holdingConnection);

			H2Database database = new H2Database();
			database.setConnection(conn);

			liquibase = new Liquibase(CHANGE_LOG_INIT, resourceAccessor, database);
			liquibase.update(contexts);

			conn.close();

		} catch (Exception ex) {
			LOG.error("Error during database initialization", ex);
			throw new RuntimeException("Error during database initialization", ex);
		}


	}




}
