package com.hhalvers.microorm.database;

import com.hhalvers.microorm.annotation.*;

@Entity
@Table(table = "person")
public class Person {

  @Id
  @Column(column = "id", type = ColumnType.INT)
  private int id;

  @Column(column = "name", type = ColumnType.STRING)
  private String name;

  Person() {}

  Person(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() { return id; }

  public void setId(int id) { this.id = id; }

  public String getName() { return name; }

  public void setName(String name) { this.name = name; }

}
