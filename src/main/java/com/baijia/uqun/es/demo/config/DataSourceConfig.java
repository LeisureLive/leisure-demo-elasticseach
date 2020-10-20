package com.baijia.uqun.es.demo.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class DataSourceConfig {

  /**
   * description: 创建um_data库数据源.
   *
   * @return um_data库数据源
   */
  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.mysql-data")
  public DataSource dataSource() {
    return DruidDataSourceBuilder.create().build();
  }

  /**
   * description: 为jdbc提供的template.
   *
   * @param dataSource 数据源.
   * @return JdbcTemplate
   */
  @Bean("umDataJdbcTemplate")
  public JdbcTemplate umDataJdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  /**
   * description: 为jdbc提供的template.
   *
   * @param dataSource 数据源
   * @return NamedParameterJdbcTemplate
   */
  @Bean("umDataNamedParameterJdbcTemplate")
  public NamedParameterJdbcTemplate umDataNamedParameterJdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }

}
