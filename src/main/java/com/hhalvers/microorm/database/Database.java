package com.hhalvers.microorm.database;

import com.hhalvers.microorm.annotation.Table;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Database implements AutoCloseable {

  private final Connection conn;
  private Set<Class> registeredClasses = new HashSet<>();
  private Map<Class, MethodInterceptor> methodInterceptors = new HashMap<>();

  public Database(Connection conn) {
    this.conn = conn;
  }

  public void registerEntity(Class entityClass) {
    if (entityClass.getAnnotation(Table.class) != null && !registeredClasses.contains(entityClass)) {
      MethodInterceptor invokeHandler = new EntityMethodInterceptor(entityClass);
      methodInterceptors.put(entityClass, invokeHandler);
      registeredClasses.add(entityClass);
    }
  }

  private void copyFields(Object toCopy, Object copy) throws IllegalAccessException {
    for (Field field : toCopy.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      field.set(copy, field.get(toCopy));
    }
  }

  public Object add(Object entity) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
    Class entityClass = entity.getClass();
    if (entityClass.getAnnotation(Table.class) != null && registeredClasses.contains(entityClass)) {
      MethodInterceptor interceptor = methodInterceptors.get(entityClass);

      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(entityClass);
      enhancer.setCallback(interceptor);

      Object proxy = enhancer.create();
      copyFields(entity, proxy);

      return proxy;
    } else {
      return null;
    }
  }

  @Override
  public void close() throws Exception {
    conn.close();
  }

}
