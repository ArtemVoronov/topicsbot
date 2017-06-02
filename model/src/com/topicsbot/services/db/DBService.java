package com.topicsbot.services.db;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

public class DBService {
  private final static Logger logger = Logger.getLogger("TOPICS_BOT_MODEL");

  private SessionFactory sf;

  @SuppressWarnings("deprecation")
  public DBService(Properties hibernateProperties, String hibernateAddCfg) {
    Configuration conf = new Configuration().configure("/com/topicsbot/model/model.cfg.xml");
    if (hibernateAddCfg != null)
      conf.configure(hibernateAddCfg);

    conf.addProperties(hibernateProperties);

    sf = conf.buildSessionFactory();
  }

  public void shutdown() {
    sf.close();
  }

  public Session openSession() {
    return sf.openSession();
  }

  /**
   * Выполняет некоторое действие в новой сессии, которая по окончании коммитается
   * @param action действие
   * @param <T> результат
   * @return результат
   */
  public <T> T tx(DBAction<T> action) {
    T value = null;

    Session session = openSession();
    try {
      session.beginTransaction();

      value = action.action(session);

      session.getTransaction().commit();

    } catch (HibernateException e) {
      session.getTransaction().rollback();
      throw e;

    } finally {
      session.close();
    }

    return value;
  }

  /**
   * Выполняет некоторое действие в новой сессии, которая по окончании коммитается
   * @param action действие
   */
  public void vtx(DBVoidAction action) {
    Session session = openSession();
    try {
      session.beginTransaction();

      action.action(session);

      session.getTransaction().commit();

    } catch (HibernateException e) {
      session.getTransaction().rollback();
      throw e;

    } finally {
      session.close();
    }
  }

  public interface DBAction<T> {
    public T action(Session s);
  }

  public interface DBVoidAction {
    public void action(Session s);
  }
}
