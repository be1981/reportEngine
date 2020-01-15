package com.cvicse.inforreport.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.cvicse.inforreport.dto.DBDatasource;
import com.cvicse.inforreport.dto.TXTDatasource;
import com.sun.org.apache.xpath.internal.operations.Equals;

import junit.framework.TestCase;

public class ReportDatasourceTest extends TestCase {
	
	ReportDatasource rds = null;

	protected void setUp() throws Exception {
		super.setUp();
		rds = new ReportDatasource();
	}

	public void testAddDBDS() {
		DBDatasource ds  = new DBDatasource();
		ds.setId("ds1");
		ds.setDriverClass("aaa");
		ds.setUrl("bbb");
		ds.setUser("user");
		ds.setPassword("pw");
		rds.addDBDS(ds);
		rds.store();
		assertEquals(ds.getDriverClass(), ((DBDatasource)rds.getDS("ds1")).getDriverClass());
		assertEquals(ds.getUrl(), ((DBDatasource)rds.getDS("ds1")).getUrl());
		assertEquals(ds.getUser(), ((DBDatasource)rds.getDS("ds1")).getUser());
		assertEquals(ds.getPassword(), ((DBDatasource)rds.getDS("ds1")).getPassword());
		
		ds.setId("ds2");
		ds.setDriverClass("ccc");
		ds.setUrl("ddd");
		ds.setUser("user2");
		ds.setPassword("pw2");
		rds.addDBDS(ds);
		rds.store();
		assertEquals(ds.getDriverClass(), ((DBDatasource)rds.getDS("ds2")).getDriverClass());
		assertEquals(ds.getUrl(), ((DBDatasource)rds.getDS("ds2")).getUrl());
		assertEquals(ds.getUser(), ((DBDatasource)rds.getDS("ds2")).getUser());
		assertEquals(ds.getPassword(), ((DBDatasource)rds.getDS("ds2")).getPassword());
		
		
	}
	
	public void testAddTXTDS(){
		TXTDatasource ds = new TXTDatasource();
		ds.setId("txt");
		ds.setUrl("path1");
		rds.addTXTDS(ds);
		rds.store();
		assertEquals(ds.getUrl(), ((TXTDatasource)rds.getDS("txt")).getUrl());
	
	}

	public void testDeleteDS() {
		rds.deleteDS("db1");
		assertNull(rds.getDS("db1"));
	}

	public void testGetDBDS() {
		DBDatasource ds = (DBDatasource)rds.getDS("ds1");
		assertEquals("aaa",ds.getDriverClass());
		assertEquals("bbb",ds.getUrl());
		assertEquals("user",ds.getUser());
		assertEquals("pw",ds.getPassword());
		
	}
	
	public void testGetTXTDS(){
		TXTDatasource ds = (TXTDatasource)rds.getDS("txt");
		assertEquals("path1",ds.getUrl());
		
	}

	public void testGetDSList() {
		List list = rds.getDSList();
		assertEquals(3,list.size());
		assertEquals("ds1",((DBDatasource)list.get(0)).getId());
		assertEquals("aaa",((DBDatasource)list.get(0)).getDriverClass());
		assertEquals("bbb",((DBDatasource)list.get(0)).getUrl());
		assertEquals("user",((DBDatasource)list.get(0)).getUser());
		assertEquals("pw",((DBDatasource)list.get(0)).getPassword());
	}

	public void testGetDriver() {
		assertEquals("aaa",rds.getDriverClass("ds1"));
		assertEquals("ccc",rds.getDriverClass("ds2"));
		assertNull(rds.getDriverClass("ds3"));
	}

	public void testGetPassword() {
		assertEquals("pw",rds.getPassword("ds1"));
		assertEquals("pw2",rds.getPassword("ds2"));
		assertNull(rds.getPassword("ds3"));
	}

	public void testGetUrl() {
		assertEquals("bbb",rds.getUrl("ds1"));
		assertEquals("ddd",rds.getUrl("ds2"));
		assertNull(rds.getUrl("ds3"));
	}

	public void testGetUsername() {
		assertEquals("user",rds.getUser("ds1"));
		assertEquals("user2",rds.getUser("ds2"));
		assertNull(rds.getUser("ds3"));
	}

	public void testSetDriver() {
		rds.setDriverClass("ds1", "ds1driver");
		assertEquals("ds1driver",rds.getDriverClass("ds1"));
	}

	public void testSetPassword() {
		rds.setPassword("ds1", "ds1pw");
		assertEquals("ds1pw",rds.getPassword("ds1"));
	}

	public void testSetUrl() {
		rds.setUrl("ds1", "ds1url");
		assertEquals("ds1url",rds.getUrl("ds1"));
	}

	public void testSetUsername() {
		rds.setUser("ds1", "ds1user");
		assertEquals("ds1user",rds.getUser("ds1"));
	}

	public void testTestDS() {
		DBDatasource ds = new DBDatasource();
		ds.setId("db1");
		ds.setDriverClass("sun.jdbc.odbc.JdbcOdbcDriver");
		ds.setUrl("jdbc:odbc:db1");
		ds.setUser("admin");
		ds.setPassword("");
		rds.addDBDS(ds);
		rds.store();		
		assertTrue(rds.testDS("db1"));
	}
	
	public void testGetConnection(){ 
		Connection conn = rds.getConnection("db1");
		assertNotNull(conn);		
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
