package com.github.kuangcp.orm.config

import com.github.kuangcp.orm.base.ExternalConfig

/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-14  下午9:00
 */

class DBConfig {

  String host
  int port
  String database
  String username
  String password
  String driver

  /**
   * 从yml加载配置,
   *
   * @return Optional < DBConfig >  可能为空
   */
  static Optional<DBConfig> buildByYaml() {
    return Optional.
        ofNullable(YamlUtil.readFile(DBConfig.class, ExternalConfig.JDBC_CONNECTION_CONFIG))
  }
}
