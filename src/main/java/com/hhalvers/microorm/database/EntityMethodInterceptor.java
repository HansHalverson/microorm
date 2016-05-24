package com.hhalvers.microorm.database;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class EntityMethodInterceptor implements MethodInterceptor {

  private Class entityClass;

  EntityMethodInterceptor(Class entityClass) {
    this.entityClass = entityClass;
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
          throws Throwable {
    System.out.println(method.getName() + " invoked");
    return proxy.invokeSuper(obj, args);
  }

}
