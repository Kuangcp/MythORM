package com.github.kuangcp.orm

import com.github.kuangcp.orm.base.DBType
import com.github.kuangcp.orm.config.DBConfig
import groovy.util.logging.Slf4j

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-14  下午9:02
 */
@Slf4j
enum DBAction {

  INSTANCE

  private int count = 0
  private PreparedStatement ps = null
  private Connection cn = null
  private ResultSet rs = null
  private String driver
  private StringBuilder url = new StringBuilder()

  DBAction() {
  }

  DBAction initByDBConfig(DBConfig config, DBType type) {
    INSTANCE.driver = type.getDriver()
    INSTANCE.url.append("jdbc:").append(type.name()).append("://").append(config.getHost()).
        append(":").append(config.getPort())
        .append("/").append(config.getDatabase()).append("?user=")
        .append(config.getUsername()).append("&password=").append(config.getPassword())
        .append("&userUnicode=true&characterEncoding=UTF8")
    return INSTANCE
  }
  /**
   * 初始化 MySQL
   */
  static DBAction buildByMysql(DBConfig config) {
    return INSTANCE.initByDBConfig(config, DBType.Mysql)
  }

  /**
   * 初始化 PostgreSQL
   */
  static DBAction buildByPostgreSQL(DBConfig config) {
    return INSTANCE.initByDBConfig(config, DBType.PostgreSQL)
  }

  /**
   * 根据参数获取数据库连接对象
   * @return Connection 连接
   */
  Connection getConnection() {
    try {
      Class.forName(driver)
      cn = DriverManager.getConnection(url.toString())
    } catch (Exception e) {
      log.error(url.toString() + " 获取连接，异常！", e)
    }
    return cn
  }

  private void loadPreparedStatement(String sql) throws SQLException {
    getConnection()
    ps = cn.prepareStatement(sql)
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
      e.printStackTrace()
    }
    log.debug("这是第" + count + "次查询操作")
    return rs
  }

  /**
   * SQL查询并返回List集合
   *
   * @param sql SQL 语句
   * @return List String数组 一行是一个String[] 按查询的字段顺序
   */
  List<String[]> queryReturnList(String sql) throws SQLException {
    log.debug("查询SQL " + sql)
    int cols
    List<String[]> data = new ArrayList<>(0)
    ResultSet rs = queryBySQL(sql)
    try {
      cols = rs.getMetaData().getColumnCount()
      while (rs.next()) {
        String[] row = new String[cols]
        for (int i = 0; i < cols; i++) {
          row[i] = rs.getString(++i)
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
   * 把增删改 合在一起 返回值是 布尔值
   * 各种连接已经关闭了不用再次关闭了
   * SQL只能输一句，不能多句运行
   *
   * @param sql 执行的SQL
   * @return boolean 是否执行成功
   */
  boolean executeUpdateSQL(String sql) throws SQLException {
    log.debug("执行SQL " + sql)
    boolean flag = true
    try {
      loadPreparedStatement(sql)
      int i = ps.executeUpdate()
      log.debug("    增删改查成功_" + i + "_行受影响-->")
      if (i != 1) {
        flag = false
      }
    } catch (Exception e) {
      log.error("增删改查失败", e)
      throw e
    } finally {
      this.closeAll()
    }
    return flag
  }

  /**
   * 插入多条数据并采用了事务
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
        ps.addBatch()
        log.debug("第" + i + "条记录插入成功")
      }
      ps.executeBatch()
      log.info("批量操作无异常, 全部提交")
      cn.commit()
    } catch (Exception e) {
      success = false
      try {
        cn.rollback()
      } catch (SQLException e1) {
        e1.printStackTrace()
      }
      log.error("增删改查失败", e)
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

  /**
   * 关闭数据库资源
   */
  void closeAll() {
    //关闭资源 后打开先关闭
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
      log.error("资源关闭异常", e)
    }
    log.debug("正常-关闭资源")
  }
}
