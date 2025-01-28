package tw.com.demo.dao;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tw.com.demo.annotation.MyId;

public class EntityManager implements AutoCloseable {

	/**
	 * 存放該執行緒的資料庫連線
	 */
	public static ThreadLocal<Connection> pools = new ThreadLocal<>();
	private final static String DB_URL = "jdbc:mysql://localhost:3306/demo";
	private final static String DB_USER = "root";
	private final static String DB_PWD = "root";
	private final static String DB_DRIVER = "com.mysql.cj.jdbc.Driver"; // oracle.jdbc.driver.OracleDriver
	
	public static Connection getConnection() {
		System.out.println("EntityManager getConnection...");
		Connection c = pools.get();
		if (c == null) {
			c = openConnection();
			pools.set(c);
		}
		return c;
	}
	
	public static Connection openConnection() {
		System.out.println("EntityManager openConnection...");
		Connection conn = null;
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PWD);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} 
		System.out.println("EntityManager openConnection success");
		return conn;
	}
	
	/**
	 * 實作將物件存入資料庫
	 * @param o
	 */
	public void save(Object o) throws Exception {
		Object idValue = getIdValue(o);
		if (null == findById(idValue, o.getClass())) {
			insert(o);
		} else {
			update(o, idValue);
		}
	}
	
	private void insert(Object o) throws Exception {
		Connection conn = getConnection();
		String tableName = o.getClass().getSimpleName();
		StringBuilder sql = new StringBuilder();
		StringBuilder column = new StringBuilder();
		StringBuilder placeholder = new StringBuilder();
		List<Object> values = new ArrayList<>();
		for (Field f : o.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			Object val = f.get(o);
			if (val != null) {
				if (column.length() > 0) {
					column.append(", ");
					placeholder.append(", ");
				}
				column.append(f.getName());
				placeholder.append("?");
				values.add(val);
			}
		}
		sql.append("INSERT INTO ").append(tableName);
		sql.append(" ( ").append(column).append(") ");
		sql.append(" VALUES ( ").append(placeholder).append(") ");
		PreparedStatement ps = conn.prepareStatement(sql.toString());
		int colCount = 1;
		for (Object val : values) {
			ps.setObject(colCount, val);			
			colCount++;
		}
		System.out.println("insert SQL ：" + replacePlaceholders(sql.toString(), values));
		int res = ps.executeUpdate();
		System.out.println("新增筆數：" + res + " 筆");
	}
	
	private void update(Object o, Object id) throws Exception {
		Connection conn = getConnection();
		String tableName = o.getClass().getSimpleName();
		String idFieldName = getIdFieldName(o.getClass());
		StringBuilder sql = new StringBuilder();
		StringBuilder placeholder = new StringBuilder();
		List<Object> values = new ArrayList<>();
		for (Field f : o.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			Object val = f.get(o);
			if (val != null) {
				if (placeholder.length() > 0) {
					placeholder.append(", ");
				}
				placeholder.append(f.getName());
				placeholder.append(" = ?");
				values.add(val);
			}
		}
		sql.append("UPDATE ").append(tableName);
		sql.append(" SET ").append(placeholder);
		sql.append(" WHERE ").append(idFieldName).append(" = ?");
		PreparedStatement ps = conn.prepareStatement(sql.toString());
		int colCount = 1;
		values.add(id);
		for (Object val : values) {
			ps.setObject(colCount, val);			
			colCount++;
		}
		System.out.println("update SQL ：" + replacePlaceholders(sql.toString(), values));
		int res = ps.executeUpdate();
		System.out.println("更新筆數：" + res + " 筆");
	}
	
	/**
	 * 實作依主鍵查出該筆資料
	 * @param <T>
	 * @param id
	 * @param t
	 * @return
	 */
	public <T> T findById(Object id, Class<T> t) throws Exception {
		Connection conn = getConnection();
		String tableName = t.getSimpleName();
		String idFieldName = getIdFieldName(t);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(tableName);
		sql.append(" WHERE ").append(idFieldName).append(" = ?");
		PreparedStatement ps = conn.prepareStatement(sql.toString());
		System.out.println("findById SQL ：" + replacePlaceholders(sql.toString(), Arrays.asList(id)));
		ps.setObject(1, id);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			T instance = (T) t.getDeclaredConstructor().newInstance();
			ResultSetMetaData metaData = rs.getMetaData();
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				String columnName = metaData.getColumnName(i);
				Object value = rs.getObject(i);
				Field field = t.getDeclaredField(columnName);
				field.setAccessible(true);
				field.set(instance, value);
			}
			return instance;
		}
		return null ;
	}
	
	/**
	 * 實作查出指定的資料表全部資料
	 * @param <T>
	 * @param t
	 * @return
	 */
	public <T> List<T> findAll(Class<T> t) throws Exception {
		List<T> dataList = new ArrayList<>();
		Connection conn = getConnection();
		String tableName = t.getSimpleName();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(tableName);
		Statement stat = conn.createStatement();
		System.out.println("findAll SQL ：" + replacePlaceholders(sql.toString(), Arrays.asList("")));
		ResultSet rs = stat.executeQuery(sql.toString());
		while (rs.next()) {
			T instance = (T) t.getDeclaredConstructor().newInstance();
			ResultSetMetaData metaData = rs.getMetaData();
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				String columnName = metaData.getColumnName(i);
				Object value = rs.getObject(i);
				Field field = t.getDeclaredField(columnName);
				field.setAccessible(true);
				field.set(instance, value);
			}
			dataList.add(instance);
		}
		return dataList ;
	}
	
	/**
	 * 實作依主鍵刪除該筆資料
	 * @param id
	 */
	public void deleteById(Object o) throws Exception {
		Connection conn = getConnection();
		String tableName = o.getClass().getSimpleName();
		String idFieldName = getIdFieldName(o.getClass());
		Object idValue = getIdValue(o);
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append(tableName).append(" WHERE ");
		sql.append(idFieldName).append(" = ?");
		PreparedStatement ps = conn.prepareStatement(sql.toString());
		ps.setObject(1, idValue);
		System.out.println("deleteById SQL ：" + replacePlaceholders(sql.toString(), Arrays.asList(idValue)));
		int res = ps.executeUpdate();
		System.out.println("刪除筆數：" + res + " 筆");
	}
	
	/**
	 * 取得id欄位名稱
	 * @param t
	 * @return
	 */
	private String getIdFieldName(Class<?> t) {
		String idFieldName = "";
		for (Field f : t.getDeclaredFields()) {
			if (f.isAnnotationPresent(MyId.class)) {
				idFieldName = f.getName();
			}
		}
		if (idFieldName.isBlank()) {
			throw new RuntimeException(t.getName() + "沒有id");
		}
		return idFieldName;
	}
	
	private Object getIdValue(Object obj) {
		Class<?> clazz = obj.getClass();
		for (Field f : clazz.getDeclaredFields()) {
			if (f.isAnnotationPresent(MyId.class)) {
				f.setAccessible(true);
				try {
					return f.get(obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public void close() throws Exception {
		Connection c = pools.get();
		if (c != null) {
			c.close();
			pools.remove();
		}
	}
	
	public static void beginTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
    }
	
    public static void commit() throws SQLException {
        Connection conn = getConnection();
        if (conn != null && !conn.isClosed()) {
            conn.commit();
        }
    }

    public static void rollback() throws SQLException {
        Connection conn = getConnection();
        if (conn != null && !conn.isClosed()) {
            conn.rollback();
        }
    }
	
	// 打印SQL
	private String replacePlaceholders(String query, List<Object> params) {
        for (Object param : params) {
            String value = (param instanceof String) ? "'" + param + "'" : String.valueOf(param);
            query = query.replaceFirst("\\?", value);
        }
        return query;
    }
}
