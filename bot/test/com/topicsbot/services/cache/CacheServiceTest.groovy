package com.topicsbot.services.cache

/**
 * Author: Artem Voronov
 */
class CacheServiceTest extends GroovyTestCase {

  void testSaveAndLoad() {
    String p = CacheServiceTest.class.protectionDomain.codeSource.location.path
    File file = new File(p+"/add_topics_waiters.json")
    if (file.exists())
      file.delete()

    CacheService cache1 = new CacheService(p)
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

    CacheService cache2 = new CacheService(p)
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

    CacheService cache3 = new CacheService(p)
    assertTrue cache3.hasWaiters("111")
    assertTrue cache3.hasWaiters("222")
    assertTrue cache3.hasWaiter("111", "1")
    assertTrue cache3.hasWaiter("111", "2")
    assertTrue cache3.hasWaiter("111", "3")
    assertTrue cache3.hasWaiter("222", "1")
    assertTrue cache3.hasWaiter("222", "2")
    assertTrue cache3.hasWaiter("222", "3")
  }
}
