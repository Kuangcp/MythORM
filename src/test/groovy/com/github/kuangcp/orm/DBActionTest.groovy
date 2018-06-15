package com.github.kuangcp.orm

import com.github.kuangcp.orm.base.DBType
import com.github.kuangcp.orm.config.DBConfig
import org.junit.Test

/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-15  上午9:19
 */
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
      List<String[]> result = action.queryReturnList('select * from user_type')
      result.forEach({
        println "表中数据 : "+it
      })
    })
  }

  @Test
  void testStringFormat(){
    Optional<DBConfig> dbConfig = DBConfig.buildByYml()
    dbConfig.ifPresent({
      DBConfig config = dbConfig.get()
      println String.format(DBType.Mysql.getUrl(), config.host, config.port, config.database,
          config.username, config.password).toString()
    })

    String format = "jdbc:Mysql://%s:::"
    String result = String.format(format, 1)
    println result
  }
}
