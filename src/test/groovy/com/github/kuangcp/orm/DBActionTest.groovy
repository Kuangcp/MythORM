package com.github.kuangcp.orm

import com.github.kuangcp.orm.config.DBConfig
import org.junit.Ignore
import org.junit.Test

/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-15  上午9:19
 */
@Ignore
class DBActionTest extends GroovyTestCase {

  /**
   * 测试初始使用
   */
  @Test
  void testInit(){
    Optional<DBConfig> dbConfig = DBConfig.buildByYml()
    dbConfig.ifPresent({
      println dbConfig.get()
      DBAction action = DBAction.initByDBConfig(dbConfig.get())
      println action.getUrl()
      List<String[]> result = action.querySQL('select * from ui')
      result.forEach({
        println "表中数据 : "+it
      })
    })
  }
}
