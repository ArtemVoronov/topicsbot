package com.topicsbot.model;

import com.topicsbot.model.user.User;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import java.util.Properties;

/**
 * Create sql script for updating. Add it to liquibase
 */
public class UpdateSchema {

  public static void main(String... args) {
    Properties props = new Properties();

    props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
    props.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
    props.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/topics_bot?useUnicode=true&amp;characterEncoding=UTF8");
    props.setProperty("hibernate.connection.username", "********");
    props.setProperty("hibernate.connection.password", "********");

    Configuration conf = new Configuration().configure(User.class.getResource("../model.cfg.xml"));
    conf.addProperties(props);

    SchemaUpdate update = new SchemaUpdate(conf);
    update.setOutputFile(args.length > 0 ? args[0] : "update-schema.sql");
    update.setDelimiter(";");
    update.setFormat(true);
    update.execute(false, false);
  }

}