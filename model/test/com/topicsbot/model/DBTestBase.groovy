package com.topicsbot.model

import com.topicsbot.services.db.DBService
import com.topicsbot.services.db.TestDBServiceFactory
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.hibernate.Session
import org.junit.Ignore

/**
 * author: Artem Voronov
 */
@Ignore
class DBTestBase extends GroovyTestCase {
  protected DBService db

  @Override
  protected void setUp() {
    super.setUp()

    db = TestDBServiceFactory.createDBService()
  }

  @Override
  protected void tearDown() {
    super.tearDown()

    db?.shutdown()
  }

  protected <V> V tx(@ClosureParams(value = SimpleType, options = 'org.hibernate.Session') Closure<V> action) {
    def value = null
    Session s = db.openSession()
    try {
      s.beginTransaction()

      value = action(s)

      s.getTransaction().commit()
    } catch (Exception e) {
      s.getTransaction().rollback()
      throw e
    } finally {
      s.close()
    }
    return value
  }

  protected void vtx(@ClosureParams(value = SimpleType, options = 'org.hibernate.Session') Closure action) {
    Session s = db.openSession()
    try {
      s.beginTransaction()

      action(s)

      s.getTransaction().commit()
    } catch (Exception e) {
      s.getTransaction().rollback()
      throw e
    } finally {
      s.close()
    }
  }
}
