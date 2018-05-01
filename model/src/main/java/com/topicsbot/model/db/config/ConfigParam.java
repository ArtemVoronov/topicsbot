package com.topicsbot.model.db.config;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

/**
 * author: Artem Voronov
 */
@Entity(name = "config_param")
@Table(name = "config_params")
@DynamicUpdate
public class ConfigParam {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private Integer id;

  @Column(name = "param_name", unique = true, nullable = false)
  @NotBlank(message = "Param name is blank")
  private String paramName;

  @Column(name = "param_value", unique = false, nullable = false)
  @NotBlank(message = "Param value is blank")
  private String paramValue;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getParamName() {
    return paramName;
  }

  public void setParamName(String paramName) {
    this.paramName = paramName;
  }

  public String getParamValue() {
    return paramValue;
  }

  public void setParamValue(String paramValue) {
    this.paramValue = paramValue;
  }
}
