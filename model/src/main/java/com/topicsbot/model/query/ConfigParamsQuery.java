package com.topicsbot.model.query;

import com.topicsbot.model.entities.config.ConfigParam;

import org.hibernate.*;
import org.hibernate.criterion.Restrictions;

/**
 * author: Artem Voronov
 */
public class ConfigParamsQuery {

  public static Criteria all(Session s) {
    Criteria c = s.createCriteria(ConfigParam.class);
    return c;
  }

  public static Criteria byId(Integer id, Session s) {
    return all(s).add(Restrictions.idEq(id));
  }

  public static Criteria byParamName(String paramName, Session s) {
    return all(s).add(Restrictions.eq("paramName", paramName));
  }

}
