package com.topicsbot.model.services;

import com.google.common.collect.Maps;
import org.hibernate.*;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.Properties;

@SuppressWarnings("Duplicates")
public class DBServiceImpl implements DBService {

  private SessionFactory sf;

  public DBServiceImpl(Properties hibernateProperties) {
    StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
    builder.configure("hibernate-model.cfg.xml");
    StandardServiceRegistry registry = builder.applySettings(Maps.fromProperties(hibernateProperties)).build();

    sf = (new MetadataSources(registry)).buildMetadata().buildSessionFactory();
  }

  @Override
  public void destroy() {
    sf.close();
  }

  @Override
  public <T> T tx(Tx<T> action) {
    T value = null;

    Session session = openStatefulSession();
    try {
      session.beginTransaction();

      value = action.run(session);

      session.getTransaction().commit();

    } catch (HibernateException e) {
      session.getTransaction().rollback();
      throw e;

    } finally {
      session.close();
    }

    return value;
  }

  @Override
  public void vtx(VoidTx action) {
    Session session = openStatefulSession();
    try {
      session.beginTransaction();

      action.run(session);

      session.getTransaction().commit();

    } catch (HibernateException e) {
      session.getTransaction().rollback();
      throw e;

    } finally {
      session.close();
    }
  }

  @Override
  public <T> T stateless(Stateless<T> action) {
    T value = null;

    StatelessSession session = openStatelessSession();
    try {
      session.beginTransaction();

      value = action.run(session);

      session.getTransaction().commit();

    } catch (HibernateException e) {
      session.getTransaction().rollback();
      throw e;

    } finally {
      session.close();
    }

    return value;
  }

  @Override
  public void vstateless(VoidStateless action) {
    StatelessSession session = openStatelessSession();
    try {
      session.beginTransaction();

      action.run(session);

      session.getTransaction().commit();

    } catch (HibernateException e) {
      session.getTransaction().rollback();
      throw e;

    } finally {
      session.close();
    }
  }

  Session openStatefulSession() {
    return sf.openSession();
  }

  StatelessSession openStatelessSession() {
    return sf.openStatelessSession();
  }
}
