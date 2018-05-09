package com.topicsbot.model.query;

import com.topicsbot.model.entities.config.ConfigParam;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.*;
import org.hibernate.query.Query;

/**
 * author: Artem Voronov
 */
public class ConfigParamsQuery {

  public static Query<ConfigParam> all(Session s) {
    CriteriaBuilder builder = s.getCriteriaBuilder();
    CriteriaQuery<ConfigParam> query = builder.createQuery(ConfigParam.class);
    Root<ConfigParam> root = query.from(ConfigParam.class);
    query.select(root);
    return s.createQuery(query);
  }

}
