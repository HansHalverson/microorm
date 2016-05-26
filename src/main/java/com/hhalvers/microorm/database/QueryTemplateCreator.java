package com.hhalvers.microorm.database;

import java.util.Set;

public class QueryTemplateCreator {

  static String createInsertStatement(Set<String> columnNames, String tableName) {
    StringBuilder insertBuilder = new StringBuilder("INSERT INTO ");
    insertBuilder.append(tableName);
    insertBuilder.append(" (");
    int i = 0;
    for (String column : columnNames) {
      if (i > 0) {
        insertBuilder.append(",");
      }
      insertBuilder.append(column);
      i++;
    }

    insertBuilder.append(") VALUES (");

    for (i = 0; i < columnNames.size(); i++) {
      if (i > 0) {
        insertBuilder.append(",");
      }
      insertBuilder.append("?");
    }

    insertBuilder.append(");");

    return insertBuilder.toString();
  }

  static String createUpdateStatement(Set<String> columnNames, String idColumn, String tableName) {
    StringBuilder updateBuilder = new StringBuilder("UPDATE ");
    updateBuilder.append(tableName);
    updateBuilder.append(" SET ");

    int i = 0;
    for (String column : columnNames) {
      if (i > 0) {
        updateBuilder.append(",");
      }
      updateBuilder.append(column);
      updateBuilder.append("=?");
      i++;
    }

    updateBuilder.append(" WHERE ");
    updateBuilder.append(idColumn);
    updateBuilder.append("=?;");

    return updateBuilder.toString();
  }

}
