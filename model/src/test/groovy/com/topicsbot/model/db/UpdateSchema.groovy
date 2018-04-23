package com.topicsbot.model.db

import com.topicsbot.model.services.DBService
import groovy.transform.CompileStatic
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.hibernate.tool.schema.TargetType


@CompileStatic
class UpdateSchema {
  static void main(String... args) {
    Map<String, Object> settings = new HashMap<>();
    settings.put("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect")
    settings.put("hibernate.connection.driver_class", "com.mysql.jdbc.Driver")
    settings.put("hibernate.connection.url", "jdbc:mysql://localhost:3306/t2f_test?useUnicode=true&ampcharacterEncoding=UTF8")
    settings.put("hibernate.connection.username", "root")
    settings.put("hibernate.connection.password", "")

    URL config = DBService.class.getClassLoader().getResource("hibernate-model.cfg.xml")

    MetadataSources metadata = new MetadataSources(
        new StandardServiceRegistryBuilder()
            .configure(config)
            .applySettings(settings)
            .build())

    EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.SCRIPT)

    SchemaExport export = new SchemaExport()
    export.setDelimiter(";")
    export.setOutputFile("update-schema.sql")
    export.setFormat(true)
    export.createOnly(targetTypes, metadata.buildMetadata())
  }
}
