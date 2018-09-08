package com.github.kuangcp.orm.base
/**
 * Created by https://github.com/kuangcp
 * 数据库类型, 并设置好驱动类, 枚举名字就是URL中的名字, 需要严格一一对应
 * @author kuangcp
 * @date 18-6-14  下午8:42
 */
enum DBType {

  Mysql("com.mysql.jdbc.Driver",
      "jdbc:mysql://%s:%s/%s?user=%s&password=%s" +
          "&userUnicode=true&characterEncoding=UTF8&useSSL=false"),

  PostgreSQL("org.postgresql.Driver",
      "jdbc:postgresql://%s:%s/%s?user=%s&password=%s" +
          "&useUnicode=true&characterEncoding=utf-8&useSSL=false")

  /**
   * @param driver driver class
   * @param url use this URL to connect db
   */
  DBType(String driver, String url) {
    this.driver = driver
    this.url = url
  }

  String driver
  String url
}
