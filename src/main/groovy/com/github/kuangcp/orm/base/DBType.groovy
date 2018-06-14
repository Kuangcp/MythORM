package com.github.kuangcp.orm.base

/**
 * Created by https://github.com/kuangcp
 * 数据库类型, 并设置好驱动类
 * @author kuangcp
 * @date 18-6-14  下午8:42
 */
enum DBType {

  Mysql("mysql", "com.mysql.jdbc.Driver"),
  PostgreSQL("postgresql", "org.postgresql.Driver")


  DBType(String type, String driver) {
    this.type = type
    this.driver = driver
  }
  private String type
  private String driver
}
