package com.github.kuangcp.orm.config

import org.junit.Test

/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-14  下午8:53
 */
class YamlUtilTest {
  @Test
  void testCreateFile() throws Exception {
    DBConfig dbConfig = new DBConfig()
    dbConfig.setHost("333")
    boolean result = YamlUtil.createFile(dbConfig, "test.yml")
    println result
  }

  @Test
  void testReadFile() throws Exception {
    DBConfig result = YamlUtil.readFile(DBConfig.class, "jdbc.yml")
    println result
  }
}
