package com.github.kuangcp.orm

import com.github.kuangcp.orm.config.DBConfig
import org.junit.Ignore
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.github.kuangcp.time.GetRunTime.GET_RUN_TIME

/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-15  上午9:19
 */
@Ignore
class DBActionTest {

  static Logger log = LoggerFactory.getLogger(DBActionTest.class)

  /**
   * 测试初始使用 PostgreSQL
   */
  @Test
  void testQuery() {
    def sql = 'select * from ui limit 4 offset 0 '

    Optional<DBConfig> dbConfig = DBConfig.buildByYml()

    dbConfig.ifPresent({
      log.info(' config={}', dbConfig.get())

      DBAction action = DBAction.initByDBConfig(dbConfig.get())
      log.info('url={}', action.getUrl())

      List<String[]> result = action.querySQL(sql)
      result.forEach({
        log.info('data={}', it)
      })

    })
  }

  /**
   * 用于对比 数据库count 还是全拎进内存进行size
   * 显然的, 如果数据量小, 并且数据一直频繁的停留在内存中需要使用, 那自然是后者, 但是大数据量的话, 内存就是一个负担
   * 而且大数据量的传输, 速度要慢于 count, 即使 count 是要注意性能优化问题, 那也要比后者快了很多
   */
  @Test
  void testCount() {
    def sql = 'select * from ui'

    Optional<DBConfig> dbConfig = DBConfig.buildByYml()
    dbConfig.ifPresent({
      log.info(' config={}', dbConfig.get())

      DBAction action = DBAction.initByDBConfig(dbConfig.get())
      log.info('url={}', action.getUrl())

      // load all data and invoke size method
      GET_RUN_TIME.startCount()
      List<String[]> result = action.querySQL(sql)
      log.info('size={}', result.size())
      GET_RUN_TIME.endCount("size")

      // just invoke count function in db
      GET_RUN_TIME.startCount()
      sql = 'select count(*) from ui'
      result = action.querySQL(sql)
      log.info('count size={}', result.size())
      GET_RUN_TIME.endCount("count")

    })
  }
}
