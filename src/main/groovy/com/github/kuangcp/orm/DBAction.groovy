package com.github.kuangcp.orm

import com.github.kuangcp.orm.base.DBType
import com.github.kuangcp.orm.config.DBConfig
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import java.sql.*

/**
 * Created by https://github.com/kuangcp
 * TODO 为了避免NPE, 全部使用 Optional
 * 批量执行 https://www.mkyong.com/jdbc/jdbc-preparedstatement-example-batch-update/
 * TODO 数据库连接池, prepare statement 预编译
 * @author kuangcp
 * @date 18-6-14  下午9:02
 */
@Slf4j
@ToString
enum DBAction {

  INSTANCE

  int count = 0
  PreparedStatement ps = null
  Connection cn = null
  ResultSet rs = null
  String driver
  String url

  static DBAction defaultInit() {
    Optional<DBConfig> dbConfig = DBConfig.buildByYml()
    dbConfig.ifPresent({
      initByDBConfig(dbConfig.get())
    })
    return INSTANCE
  }

  static DBAction initByDBConfig(DBConfig config) {
    log.debug("prepare init by {}", config)
    if (Objects.isNull(config)) {
      log.error("please init database config file: jdbc.yml ")
      return INSTANCE
    }

    DBType type
    if (config.isThisType(DBType.Mysql)) {
      type = DBType.Mysql
    } else if (config.isThisType(DBType.PostgreSQL)) {
      type = DBType.PostgreSQL
    } else {
      log.error("not support this database type : " + config.type)
      return null
    }

    INSTANCE.driver = type.getDriver()
    INSTANCE.url = String.format(type.getUrl(), config.host, config.port, config.database,
        config.username, config.password)
    log.debug("init from config: url={}", INSTANCE.url)
    return INSTANCE
  }

  /**
   * 根据参数获取数据库连接对象
   * @return Connection 连接
   */
  Connection getConnection() throws SQLException {
    try {
      Class.forName(driver)
      cn = DriverManager.getConnection(url)
    } catch (SQLException e) {
      log.error("{} attempt get connection failed", url, e)
      throw e
    }
    return cn
  }

  boolean isTableExist(String tableName) {
    def connection = getConnection()
    def tables = connection.getMetaData().getTables(null, null, tableName, null)
    if (tables.next()) {
      return true
    }
    return false
  }

//  querySQL(PreparedStatement ps) throws SQLException{
//    cn.prepareStatement("")
//    rs = ps.executeQuery()
//  }

  /**
   * SQL查询并返回List集合
   *
   * @param sql SQL 语句
   * @return List String数组 一行是一个String[] 按查询的字段顺序 SQL异常返回null
   */
  List<String[]> querySQL(String sql) throws SQLException {
    log.debug("prepare execute query sql: {}", sql)
    ResultSet rs = queryBySQL(sql)
    if (rs == null) {
      return null
    }
    List<String[]> data = new ArrayList<>()

    try {
      int cols = rs.getMetaData().getColumnCount()
      while (rs.next()) {
        String[] row = new String[cols]
        for (int i = 1; i <= cols; i++) {
          row[i - 1] = rs.getString(i)
        }
        data.add(row)
      }
    } catch (SQLException e) {
      log.error("查询异常", e)
      throw e
    } finally {
      this.closeAll()
    }
    return data
  }

  /**
   * 查询全部的操作 返回值是ResultSet 切记使用完后要finally关闭
   */
  ResultSet queryBySQL(String sql) {
    count++
    try {
      loadPreparedStatement(sql)
      rs = ps.executeQuery()
    } catch (Exception e) {
      log.error("execute sql failed ", e)
      return null
    }
    log.debug("this {} query action ", count)
    return rs
  }

  /**
   * 把增删改 合在一起 返回值是 布尔值
   * 各种连接已经关闭了不用再次关闭了
   *
   * @param sql 执行的SQL(只能是一句)
   * @return boolean 是否执行成功
   */
  boolean executeUpdateSQL(String sql) throws SQLException {
    log.debug("prepare execute sql: {}", sql)
    // FIXME flag 是否有必要, twr和finally的问题
    boolean flag = true
    try {
      loadPreparedStatement(sql)
      int i = ps.executeUpdate()
      log.debug("execute success, {} line has influenced ", i)
      if (i != 1) {
        flag = false
      }
    } catch (Exception e) {
      log.error("execute sql error: ", e)
      throw e
    } finally {
      this.closeAll()
    }
    return flag
  }

  // TODO 是否可行
  boolean batchExecuteWithAffair(PreparedStatement ps) {
    Connection cn = ps.getConnection()
    try {
      cn.setAutoCommit(false)
      ps.executeBatch()
      cn.commit()
    } catch (Exception e) {
      log.error(e.getMessage(), e)
      cn.rollback()
    } finally {
      cn.setAutoCommit(true)
    }
    return true
  }
  /**
   * 事务性, 执行多条SQL
   *
   * @param sqlArray SQL的String数组
   * @return boolean 是否成功
   */
  boolean batchInsertWithAffair(String[] sqlArray) {
    boolean success = true
    try {
      cn = getConnection()
      cn.setAutoCommit(false)
      for (int i = 0; i < sqlArray.length; i++) {
        ps = cn.prepareStatement(sqlArray[i])
        ps.execute()
        log.debug("the {} execute success, sql={}", i, sqlArray[i])
      }

      log.info("batch execute sql success, commit it ")
      cn.commit()
    } catch (Exception e) {
      success = false
      try {
        cn.rollback()
      } catch (SQLException e1) {
        e1.printStackTrace()
      }
      log.error("execute sql error", e)
    } finally {
      try {
        cn.setAutoCommit(true)
      } catch (SQLException e) {
        e.printStackTrace()
      }
      this.closeAll()
    }
    return success
  }

  void closeAll() {
    try {
      if (rs != null) {
        rs.close()
      }
      if (ps != null) {
        ps.close()
      }
      if (cn != null) {
        cn.close()
      }
    } catch (SQLException e) {
      log.error("connection close error", e)
      return
    }
    log.debug("closing connection successful")
  }

  private void loadPreparedStatement(String sql) throws SQLException {
    try {
      getConnection()
    } catch (SQLException e) {
      throw e
    }
    ps = cn.prepareStatement(sql)
  }
}
