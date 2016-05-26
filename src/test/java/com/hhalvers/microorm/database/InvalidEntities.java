package com.hhalvers.microorm.database;

import com.hhalvers.microorm.annotation.Entity;
import com.hhalvers.microorm.annotation.Id;
import com.hhalvers.microorm.annotation.Table;

public class InvalidEntities {

  @Table(table = "person")
  public static class NoEntityAnnotation {}

  @Entity
  public static class NoTableAnnotation {}

  @Entity
  @Table(table = "person")
  public static class NoIdAnnotation {
    private int id = 1;
  }

  @Entity
  @Table(table = "person")
  public static class MultipleIdAnnotations {
    @Id
    private int id1 = 1;

    @Id
    private int id2 = 2;
  }

}
