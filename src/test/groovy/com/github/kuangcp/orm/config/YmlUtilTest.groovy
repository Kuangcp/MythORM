package com.github.kuangcp.orm.config

import org.junit.Test

/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-14  下午8:53
 */
class YmlUtilTest {
  // 生成yml尚有问题
  @Test
  void testCreateFile() throws Exception {
    DBConfig dbConfig = new DBConfig()
    dbConfig.setHost("333")
    boolean result = YmlUtil.createFile(dbConfig, "src/main/resources/test.yml")
    println result
  }

  @Test
  void testReadFile() throws Exception {
    DBConfig result = YmlUtil.readFile(DBConfig.class, "jdbc.yml")
    println result
  }
}
