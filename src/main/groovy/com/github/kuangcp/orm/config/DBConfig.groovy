package com.github.kuangcp.orm.config

import com.github.kuangcp.orm.base.DBType
import com.github.kuangcp.orm.base.ExternalConfig
import groovy.transform.ToString

/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-14  下午9:00
 */
@ToString
class DBConfig {

  String host
  int port
  String database
  String username
  String password
  String type

  /**
   * 从jdbc.yml加载配置
   *
   * @return Optional < DBConfig >  可能为空
   */
  static Optional<DBConfig> buildByYml() {
    return Optional.
        ofNullable(YmlUtil.readFile(DBConfig.class, ExternalConfig.JDBC_CONNECTION_CONFIG))
  }

  boolean isThisType(DBType type) {
    return this.type.compareToIgnoreCase(type.name()) == 0
  }
}
