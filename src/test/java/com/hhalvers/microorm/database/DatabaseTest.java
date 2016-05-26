package com.hhalvers.microorm.database;

import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.INVALID_ACTIVITY;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseTest {

  private static Database db;

  @BeforeClass
  public static void setupClass() throws ClassNotFoundException, SQLException, DatabaseMapException {
    Class.forName("org.sqlite.JDBC");
    Connection conn = DriverManager.getConnection("jdbc:sqlite:data/smallTestDb.sqlite3");
    db = new Database(conn);
    db.registerEntity(Person.class);
  }

  @AfterClass
  public static void teardownClass() throws Exception {
    db.close();
  }

  @Test(expected = DatabaseMapException.class)
  public void registerNoEntityAnnotation() throws DatabaseMapException {
    db.registerEntity(InvalidEntities.NoEntityAnnotation.class);
  }

  @Test(expected = DatabaseMapException.class)
  public void registerNoTableAnnotation() throws DatabaseMapException {
    db.registerEntity(InvalidEntities.NoTableAnnotation.class);
  }

  @Test(expected = DatabaseMapException.class)
  public void registerNoIdAnnotation() throws DatabaseMapException {
    db.registerEntity(InvalidEntities.NoIdAnnotation.class);
  }

  @Test(expected = DatabaseMapException.class)
  public void registerMultipleIdAnnotations() throws DatabaseMapException {
    db.registerEntity(InvalidEntities.MultipleIdAnnotations.class);
  }

  @Test
  public void addTest() throws Exception {
    Person p = new Person(1, "one");
    p = (Person) db.add(p);

    assertEquals(1, p.getId());
    assertEquals("one", p.getName());

    p.setId(2);
    p.setName("two");
  }

}
