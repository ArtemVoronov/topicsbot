package com.topicsbot.model.services.db

import com.topicsbot.model.entities.config.ConfigParamsTest
import com.topicsbot.model.services.config.TestConfigParams
import org.hibernate.Session
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.hibernate.tool.schema.TargetType

class TestDBServiceFactory {

  protected static Properties hibernateProperties

  synchronized static HibernateDBService createDBService() throws IOException {
    if (hibernateProperties == null) {
      hibernateProperties = load("test_db.properties")
    }
    final HibernateDBService db = new HibernateDBService(hibernateProperties)

    createEmptyDB(hibernateProperties)

    final Session s = db.openStatefulSession()
    try {
      s.beginTransaction()
      preFillDb(s)
      s.getTransaction().commit()

    } catch (RuntimeException e) {
      s.getTransaction().rollback()
      throw e

    } finally {
      s.close()
    }

    return db
  }

  protected static Properties load(String resource) {
    Properties props = new Properties()
    try {
      InputStream is = TestDBServiceFactory.class.getClassLoader().getResourceAsStream(resource) as InputStream
      props.load(is)
    } catch (IOException e) {
      e.printStackTrace()
    }
    return props
  }

  protected static void createEmptyDB(Properties hibernateProperties) {
    Map<String, Object> settings = new HashMap<>()
    hibernateProperties.stringPropertyNames().each { it ->
      settings.put(it, hibernateProperties.getProperty(it))
    }

    URL config = DBService.class.getClassLoader().getResource("hibernate-model.cfg.xml")

    MetadataSources metadata = new MetadataSources(
        new StandardServiceRegistryBuilder()
            .configure(config)
            .applySettings(settings)
            .build())

    EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.DATABASE)

    SchemaExport export = new SchemaExport()
    export.create(targetTypes, metadata.buildMetadata())
  }

  private static void preFillDb(Session s) {
    s.save(ConfigParamsTest.createConfigParam(paramName: TestConfigParams.TELEGRAM_BOT_TOKEN.key,             paramValue: TestConfigParams.TELEGRAM_BOT_TOKEN.value))
    s.save(ConfigParamsTest.createConfigParam(paramName: "hibernate.query.batch.size",                        paramValue: "1"))
    s.save(ConfigParamsTest.createConfigParam(paramName: "hibernate.query.fetch.size",                        paramValue: "1"))
  }
}