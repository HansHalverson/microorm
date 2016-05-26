package com.hhalvers.microorm.database;

import com.hhalvers.microorm.annotation.Column;
import com.hhalvers.microorm.annotation.Table;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class EntityMethodInterceptor implements MethodInterceptor {

  private Class entityClass;
  private Connection conn;

  private String tableName;
  private SortedMap<String, Field> columnFields = new TreeMap<>();

  private String insertStatement;

  EntityMethodInterceptor(Class entityClass, Connection conn) {
    this.entityClass = entityClass;
    this.conn = conn;

    extractDatabaseInformation();
  }

  private void extractDatabaseInformation() {
    Table tableAnnotation = (Table) entityClass.getAnnotation(Table.class);
    this.tableName = tableAnnotation.table();

    for (Field field : entityClass.getDeclaredFields()) {
      Column columnAnnotation = field.getDeclaredAnnotation(Column.class);
      if (columnAnnotation != null) {
        columnFields.put(columnAnnotation.column(), field);
      }
    }

    this.insertStatement = createInsertStatement(columnFields.size());
  }

  private String createInsertStatement(int numColumns) {
    StringBuilder insertBuilder = new StringBuilder("INSERT INTO ");
    insertBuilder.append(tableName);
    insertBuilder.append(" (");
    int i = 0;
    for (String column : columnFields.keySet()) {
      if (i > 0) {
        insertBuilder.append(",");
      }
      insertBuilder.append(column);
      i++;
    }

    insertBuilder.append(") VALUES (");

    for (i = 0; i < numColumns; i++) {
      if (i > 0) {
        insertBuilder.append(",");
      }
      insertBuilder.append("?");
    }

    insertBuilder.append(");");

    return insertBuilder.toString();
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
          throws Throwable {
    Map<String, Object> columnState = new HashMap<>();
    saveState(obj, columnState);

    Object invokeReturn = proxy.invokeSuper(obj, args);

    if (stateDiffers(obj, columnState)) {
      saveStateToDb(obj);
    }

    return invokeReturn;
  }

  private void saveState(Object obj, Map<String, Object> fieldState) throws IllegalAccessException {
    for (Field field : columnFields.values()) {
      field.setAccessible(true);
      fieldState.put(field.getName(), field.get(obj));
    }
  }

  private boolean stateDiffers(Object obj, Map<String, Object> fieldState) throws IllegalAccessException {
    for (Field field : columnFields.values()) {
      Object savedValue = fieldState.get(field.getName());
      field.setAccessible(true);
      if (!field.get(obj).equals(savedValue)) {
        return true;
      }
    }

    return false;
  }

  private void saveStateToDb(Object obj) throws SQLException, IllegalAccessException {
    try (PreparedStatement prep = conn.prepareStatement(insertStatement)) {
      prep.setString(1, tableName);

      int i = 1;
      for (String column : columnFields.keySet()) {
        Field field = columnFields.get(column);
        field.setAccessible(true);

        prep.setObject(i, field.get(obj));
        i++;
      }

      prep.execute();
    }
  }

}
