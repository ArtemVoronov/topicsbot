package com.topicsbot.services.cache

import com.topicsbot.model.DBTestBase
import com.topicsbot.model.chat.Chat
import com.topicsbot.model.chat.ChatTest
import com.topicsbot.model.statistics.ChatDayStatistics
import com.topicsbot.model.statistics.ChatDayStatisticsTest
import com.topicsbot.model.statistics.CounterType
import com.topicsbot.model.statistics.UserDayStatistics
import com.topicsbot.model.statistics.UserDayStatisticsTest
import com.topicsbot.model.user.User
import com.topicsbot.model.user.UserTest
import com.topicsbot.services.ServicesException
import com.topicsbot.utils.ThreadFactoryWithCounter

import java.time.LocalDate
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

import static org.junit.Assert.assertNotNull

/**
 * Author: Artem Voronov
 */
class CacheServiceTest extends DBTestBase {

  private static ScheduledExecutorService initScheduledExecutorService() throws ServicesException {
    int corePoolSize = 1
    int maximumPoolSize = 20
    long keepAliveTime = 60

    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, new ThreadFactoryWithCounter("ServicesWorker-", 0))
    executor.setMaximumPoolSize(maximumPoolSize)
    executor.setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS)

    return executor
  }

  //TODO
//  void testRestarting() {
//    /*
//    * 1. проверить что подгрузились кэши статистики и пользователям отправляются сообщения с актуальными циферками
//    * */
//    ScheduledExecutorService scheduledExecutorService = initScheduledExecutorService()
//
//    CacheService cache1 = new CacheService(db, scheduledExecutorService) //loading data from storage to cache
//
//    cache1.shutdown() // putting data from cache to storage
//
//    CacheService cache2 = new CacheService(db, scheduledExecutorService) //loading data from storage to cache again
//  }

  void testSaveAndLoadWaiters() {
    ScheduledExecutorService scheduledExecutorService = initScheduledExecutorService()
    String p = CacheServiceTest.class.protectionDomain.codeSource.location.path + "/t1"
    File dir = new File(p)
    if (!dir.exists())
      dir.mkdir()
    File file = new File(p+"/add_topics_waiters.json")
    if (file.exists())
      file.delete()

    CacheService cache1 = new CacheService(p, db, scheduledExecutorService)
    cache1.addWaiter("111", "1")
    cache1.addWaiter("111", "2")
    cache1.addWaiter("111", "3")
    cache1.addWaiter("222", "1")
    cache1.addWaiter("222", "2")

    assertTrue cache1.hasWaiters("111")
    assertTrue cache1.hasWaiters("222")
    assertTrue cache1.hasWaiter("111", "1")
    assertTrue cache1.hasWaiter("111", "2")
    assertTrue cache1.hasWaiter("111", "3")
    assertTrue cache1.hasWaiter("222", "1")
    assertTrue cache1.hasWaiter("222", "2")

    cache1.shutdown()

    CacheService cache2 = new CacheService(p, db, scheduledExecutorService)
    cache2.addWaiter("222", "3")
    assertTrue cache2.hasWaiters("111")
    assertTrue cache2.hasWaiters("222")
    assertTrue cache2.hasWaiter("111", "1")
    assertTrue cache2.hasWaiter("111", "2")
    assertTrue cache2.hasWaiter("111", "3")
    assertTrue cache2.hasWaiter("222", "1")
    assertTrue cache2.hasWaiter("222", "2")
    assertTrue cache2.hasWaiter("222", "3")

    cache2.shutdown()

    CacheService cache3 = new CacheService(p, db, scheduledExecutorService)
    assertTrue cache3.hasWaiters("111")
    assertTrue cache3.hasWaiters("222")
    assertTrue cache3.hasWaiter("111", "1")
    assertTrue cache3.hasWaiter("111", "2")
    assertTrue cache3.hasWaiter("111", "3")
    assertTrue cache3.hasWaiter("222", "1")
    assertTrue cache3.hasWaiter("222", "2")
    assertTrue cache3.hasWaiter("222", "3")
  }

  void testSaveAndLoadChatStatistics() {
    ScheduledExecutorService scheduledExecutorService = initScheduledExecutorService()
    String p = CacheServiceTest.class.protectionDomain.codeSource.location.path + "/t2"
    File dir = new File(p)
    if (!dir.exists())
      dir.mkdir()
    File file = new File(p + "/chat_statistics.json")
    if (file.exists())
      file.delete()

    Chat chat1 = ChatTest.createCorrectChat()
    CacheService cache1 = new CacheService(p, db, scheduledExecutorService)
    cache1.createChatStatistics(chat1, CounterType.START_COMMAND, 1)

    assertNotNull cache1.getChatStatistics(chat1.externalId, chat1.rebirthDate)

    cache1.shutdown()

    Chat chat2 = ChatTest.createCorrectChat(externalId: "1231235")
    CacheService cache2 = new CacheService(p, db, scheduledExecutorService)
    cache2.createChatStatistics(chat2, CounterType.DONATE_COMMAND, 1)
    assertNotNull cache2.getChatStatistics(chat1.externalId, chat1.rebirthDate)
    assertNotNull cache2.getChatStatistics(chat2.externalId, chat2.rebirthDate)

    cache2.shutdown()

    CacheService cache3 = new CacheService(p, db, scheduledExecutorService)
    assertNotNull cache3.getChatStatistics(chat1.externalId, chat1.rebirthDate)
    assertNotNull cache3.getChatStatistics(chat2.externalId, chat2.rebirthDate)
  }

  void testCleaningChatStatistics() {
    ScheduledExecutorService scheduledExecutorService = initScheduledExecutorService()
    String p = CacheServiceTest.class.protectionDomain.codeSource.location.path + "/t3"
    File dir = new File(p)
    if (!dir.exists())
      dir.mkdir()
    File file = new File(p + "/chat_statistics.json")
    if (file.exists())
      file.delete()

    Chat chat = ChatTest.createCorrectChat(rebirthDate: LocalDate.now().minusDays(2))
    vtx{s -> s.save(chat)}
    CacheService cache = new CacheService(p, db, scheduledExecutorService)
    cache.createChatStatistics(chat, CounterType.START_COMMAND, 1)
    def stat1 = cache.getChatStatistics(chat.externalId, chat.rebirthDate)
    assertNotNull stat1
    vtx{s ->
      chat.rebirthDate = chat.rebirthDate.plusDays(1)
      s.saveOrUpdate(chat)
    }
    cache.createChatStatistics(chat, CounterType.DONATE_COMMAND, 1)
    def stat2 = cache.getChatStatistics(chat.externalId, chat.rebirthDate)
    assertNotNull stat2
    vtx{s ->
      chat.rebirthDate = chat.rebirthDate.plusDays(1)
      s.saveOrUpdate(chat)
    }
    cache.createChatStatistics(chat, CounterType.CANCEL_COMMAND, 1)
    def stat3 = cache.getChatStatistics(chat.externalId, chat.rebirthDate)
    assertNotNull stat3

    cache.statisticsCacheCleaner.run()

    assertNotNull stat1.id
    assertNotNull stat2.id

    def loadedStat1 = tx { s -> s.get(ChatDayStatistics, stat1.id) as ChatDayStatistics}
    def loadedStat2 = tx { s -> s.get(ChatDayStatistics, stat2.id) as ChatDayStatistics}

    assertNotNull loadedStat1
    assertNotNull loadedStat2
    ChatDayStatisticsTest.assertChatDayStatisticsEquals stat1, loadedStat1
    ChatDayStatisticsTest.assertChatDayStatisticsEquals stat2, loadedStat2


    chat.rebirthDate = chat.rebirthDate.minusDays(1)
    assertNull cache.getChatStatistics(chat.externalId, chat.rebirthDate)
    chat.rebirthDate = chat.rebirthDate.minusDays(1)
    assertNull cache.getChatStatistics(chat.externalId, chat.rebirthDate)
  }

  void testSaveAndLoadUserStatistics() {
    ScheduledExecutorService scheduledExecutorService = initScheduledExecutorService()
    String p = CacheServiceTest.class.protectionDomain.codeSource.location.path + "/t4"
    File dir = new File(p)
    if (!dir.exists())
      dir.mkdir()
    File file = new File(p + "/user_statistics.json")
    if (file.exists())
      file.delete()

    Chat chat1 = ChatTest.createCorrectChat()
    User user1 = UserTest.createCorrectUser()
    CacheService cache1 = new CacheService(p, db, scheduledExecutorService)
    cache1.createUserStatistics(chat1, user1, CounterType.START_COMMAND, 1)

    assertNotNull cache1.getUserStatistics(chat1, user1)

    cache1.shutdown()

    Chat chat2 = ChatTest.createCorrectChat(externalId: "1231235")
    User user2 = UserTest.createCorrectUser(externalId: "6666")
    CacheService cache2 = new CacheService(p, db, scheduledExecutorService)
    cache2.createUserStatistics(chat2, user2, CounterType.START_COMMAND, 1)
    cache2.createUserStatistics(chat1, user2, CounterType.START_COMMAND, 1)
    assertNotNull cache2.getUserStatistics(chat1, user1)
    assertNotNull cache2.getUserStatistics(chat1, user2)
    assertNotNull cache2.getUserStatistics(chat2, user2)

    cache2.shutdown()

    CacheService cache3 = new CacheService(p, db, scheduledExecutorService)
    assertNotNull cache3.getUserStatistics(chat1, user1)
    assertNotNull cache3.getUserStatistics(chat1, user2)
    assertNotNull cache3.getUserStatistics(chat2, user2)
  }

  void testCleaningUserStatistics() {
    ScheduledExecutorService scheduledExecutorService = initScheduledExecutorService()
    String p = CacheServiceTest.class.protectionDomain.codeSource.location.path + "/t5"
    File dir = new File(p)
    if (!dir.exists())
      dir.mkdir()
    File file = new File(p + "/user_statistics.json")
    if (file.exists())
      file.delete()

    Chat chat = ChatTest.createCorrectChat(rebirthDate: LocalDate.now().minusDays(2))
    User user = UserTest.createCorrectUser()
    vtx{s ->
      s.save(chat)
      s.save(user)
    }
    CacheService cache = new CacheService(p, db, scheduledExecutorService)
    cache.createUserStatistics(chat, user, CounterType.START_COMMAND, 1)
    def stat1 = cache.getUserStatistics(chat, user)
    assertNotNull stat1
    vtx{s ->
      chat.rebirthDate = chat.rebirthDate.plusDays(1)
      s.saveOrUpdate(chat)
    }
    cache.createUserStatistics(chat, user, CounterType.START_COMMAND, 1)
    def stat2 = cache.getUserStatistics(chat, user)
    assertNotNull stat2
    vtx{s ->
      chat.rebirthDate = chat.rebirthDate.plusDays(1)
      s.saveOrUpdate(chat)
    }
    cache.createUserStatistics(chat, user, CounterType.DONATE_COMMAND, 1)
    def stat3 = cache.getUserStatistics(chat, user)
    assertNotNull stat3

    cache.statisticsCacheCleaner.run()

    assertNotNull stat1.id
    assertNotNull stat2.id

    def loadedStat1 = tx { s -> s.get(UserDayStatistics, stat1.id) as UserDayStatistics}
    def loadedStat2 = tx { s -> s.get(UserDayStatistics, stat2.id) as UserDayStatistics}

    assertNotNull loadedStat1
    assertNotNull loadedStat2
    UserDayStatisticsTest.assertUserDayStatisticssEquals stat1, loadedStat1
    UserDayStatisticsTest.assertUserDayStatisticssEquals stat2, loadedStat2


    chat.rebirthDate = chat.rebirthDate.minusDays(1)
    assertNull cache.getUserStatistics(chat, user)
    chat.rebirthDate = chat.rebirthDate.minusDays(1)
    assertNull cache.getUserStatistics(chat, user)
  }
}
