package com.topicsbot.model.db

import com.topicsbot.model.services.DBService
import com.topicsbot.model.services.TestDBServiceFactory
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
