package com.topicsbot.model.entities.config

import com.topicsbot.model.entities.DBTestBase
import org.hibernate.exception.ConstraintViolationException
import org.junit.Test

import static org.junit.Assert.assertNotNull

/**
 * author: Artem Voronov
 */
class ConfigParamsTest extends DBTestBase {

  static ConfigParam createConfigParam(Map overrides = [:]) {
    def defaultFields = [
        paramName : 'test param name',
        paramValue : 'test param value'
    ]
    return new ConfigParam(defaultFields + overrides)
  }

  @Test
  void testSaveAndLoad() {
    def param = createConfigParam()

    db.tx { s ->
      s.save(param)
    }

    assertNotNull(param.id)

    def loaded = db.tx { s -> s.get(ConfigParam, param.id) as ConfigParam}

    assertNotNull(loaded)
    assertUsersEquals(param, loaded)
  }

  @Test
  void testDuplicate() {
    def param = createConfigParam()
    def duplicate = createConfigParam()

    def msg = shouldFail(ConstraintViolationException) {
      db.tx { s ->
        s.save(param)
        s.save(duplicate)
      }
    }

    assertEquals "could not execute statement", msg
  }

  static void assertUsersEquals(ConfigParam expected, ConfigParam actual) {
    assertEquals expected.paramName, actual.paramName
    assertEquals expected.paramValue, actual.paramValue
  }

}
