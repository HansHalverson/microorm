package com.hhalvers.microorm.database;

import com.hhalvers.microorm.annotation.Column;
import com.hhalvers.microorm.annotation.Id;
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
  private String idColumnName;
  private Field idField;
  private SortedMap<String, Field> columnFields = new TreeMap<>();

  private String insertStatement;
  private String updateStatement;

  EntityMethodInterceptor(Class entityClass, Connection conn) {
    this.entityClass = entityClass;
    this.conn = conn;

    extractDatabaseInformation();
  }

  private void extractDatabaseInformation() {
    Table tableAnnotation = (Table) entityClass.getAnnotation(Table.class);
    this.tableName = tableAnnotation.table();

    for (Field field : entityClass.getDeclaredFields()) {
      Column columnAnnotation = field.getAnnotation(Column.class);
      if (columnAnnotation != null) {
        columnFields.put(columnAnnotation.column(), field);

        if (field.getAnnotation(Id.class) != null) {
          idColumnName = columnAnnotation.column();
          idField = field;
        }
      }
    }

    this.insertStatement = QueryTemplateCreator.createInsertStatement(columnFields.keySet(), tableName);
    this.updateStatement = QueryTemplateCreator.createUpdateStatement(columnFields.keySet(), idColumnName, tableName);
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
    try (PreparedStatement prep = conn.prepareStatement(updateStatement)) {

      int i = 1;
      for (String column : columnFields.keySet()) {
        Field field = columnFields.get(column);
        field.setAccessible(true);

        prep.setObject(i, field.get(obj));
        i++;
      }

      prep.setObject(i, idField.get(obj));

      prep.execute();
    }
  }

}
