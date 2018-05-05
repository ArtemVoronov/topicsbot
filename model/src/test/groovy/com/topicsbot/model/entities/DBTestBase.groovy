package com.topicsbot.model.entities

import com.topicsbot.model.services.db.DBService
import com.topicsbot.model.services.db.TestDBServiceFactory
import org.junit.Ignore

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

    db?.destroy()
  }

}
