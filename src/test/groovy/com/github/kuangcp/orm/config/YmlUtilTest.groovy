package com.github.kuangcp.orm.config

import org.junit.Test

/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-14  下午8:53
 */
class YmlUtilTest {

  /**
   * 从src开始的目录, 但是实际的接口使用中也是 resources 目录下, 应该是idea的问题
   */
  private String createFile = "src/main/resources/test.yml"

  /**
   * 加载文件时, 默认目录为resources
   */
  private String loadFile = "jdbc.yml"

  /**
   * create yml file
   * @throws Exception
   */
  @Test
  void testCreateFile() throws Exception {
    DBConfig dbConfig = new DBConfig()
    dbConfig.setHost("33334")
    boolean result = YmlUtil.createFile(dbConfig, createFile)
    println result
  }

  /**
   * load a yml file
   * @throws Exception
   */
  @Test
  void testReadFile() throws Exception {
    DBConfig result = YmlUtil.readFile(DBConfig.class, loadFile)
    println result
  }
}
