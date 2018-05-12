package com.topicsbot.model.services.config

import com.topicsbot.model.entities.DBTestBase
import com.topicsbot.model.entities.config.ConfigParam
import com.topicsbot.model.query.ConfigParamsQuery
import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.hibernate.Session
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

import static com.topicsbot.model.services.config.TestConfigParams.TELEGRAM_BOT_TOKEN

/**
 * Author: Artem Voronov
 */
@CompileStatic
@RunWith(MockitoJUnitRunner.Silent.class)
class ConfigParamsHolderBasicTest extends DBTestBase {

  ConfigParamsHolderBasic configParamsHolder

  @Before
  void setUp() {
    super.setUp()
    MockitoAnnotations.initMocks(this)
    configParamsHolder = new ConfigParamsHolderBasic(db, Logger.getLogger("TEST_CONFIG_PARAMS_HOLDER"))
  }

  @After
  void shutdown() {
    //nothing
  }

  @Test
  void testConfigParamsUpdating() {
    def newBotToken = "another"
    def initialBotToken = configParamsHolder.getConfigParam(TELEGRAM_BOT_TOKEN.key)

    db.vtx { Session s ->
      ConfigParam configParam = ConfigParamsQuery.byParamName(TELEGRAM_BOT_TOKEN.key, s).uniqueResult() as ConfigParam
      configParam.paramValue = newBotToken
    }

    def loadedBotTokenBeforeConfigUpdating = configParamsHolder.getConfigParam(TELEGRAM_BOT_TOKEN.key)

    configParamsHolder.configParamsReader.run()

    def loadedBotTokenAfterConfigUpdating = configParamsHolder.getConfigParam(TELEGRAM_BOT_TOKEN.key)

    assertEquals initialBotToken, TELEGRAM_BOT_TOKEN.value
    assertEquals loadedBotTokenBeforeConfigUpdating, TELEGRAM_BOT_TOKEN.value
    assertEquals loadedBotTokenAfterConfigUpdating, newBotToken
  }
}
