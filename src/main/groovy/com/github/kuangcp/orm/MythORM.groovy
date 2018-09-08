package com.github.kuangcp.orm

import com.github.kuangcp.orm.config.DBConfig
import groovy.util.logging.Slf4j

import java.lang.reflect.Method
import java.sql.ResultSet
import java.sql.SQLException
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by https://github.com/kuangcp
 *  实现了一个查询记录，插入记录 的 单例 ORM
 * @author kuangcp
 * @date 18-6-14  下午9:05
 */
@Slf4j
enum MythORM {

  MYTH_ORM

  private DBConfig dbConfig = null

  /**
   * 采用默认配置进行初始化
   * @return
   */
  static MythORM defaultInit() {
    if (MYTH_ORM.dbConfig == null) {
      Optional<DBConfig> config = DBConfig.buildByYml()

      if (config.isPresent()) {
        MYTH_ORM.dbConfig = config.get()
      } else {
        log.error("从默认配置文件加载数据库配置失败")
      }
    } else {
      log.warn("数据库默认配置已经加载, 无需重复加载")
    }
    return MYTH_ORM
  }

  // TODO 更改为prepareStatement
  boolean saveAll(Collection collection) throws SQLException {
    List<String> list = new ArrayList<>()
    collection.forEach({
      list.add(createSaveSQL(it))
    })
    log.debug("size={}", list.size())

    return DBAction.DB_ACTION.initByDBConfig(dbConfig).
        batchInsertWithAffair(list.toArray() as String[])
  }

  /**
   * 将输入对象转换成SQL语句
   * 不能有除了属性的get方法之外的get方法，不然这里的SQL拼接会失败
   *
   * @param obj 输入对象, 属性为空就默认为空字符串和0  字段的类型暂时只支持 long int Integer String Date
   * @return boolean 是否成功
   */
  boolean save(Object obj) throws SQLException {
    String sql = createSaveSQL(obj)
    return DBAction.DB_ACTION.initByDBConfig(dbConfig).executeUpdateSQL(sql)
  }

  private String createSaveSQL(Object obj) {
    if (!isConfigNotNull()) {
      return false
    }
    Class target = obj.getClass()
    StringBuilder sqlBuilder = new StringBuilder("insert into ")
    StringBuilder valueBuilder = new StringBuilder("values(")

    Method[] methods = target.getDeclaredMethods()
    String className = target.getName()
    String tableName = convertToUnderLineStyle(className)
    sqlBuilder.append(tableName).append(" (")

    for (Method method : methods) {
      String mName = method.getName()
      // TODO 如何有效的正确筛选字段
//      log.debug(mName+" = "+method.genericReturnType.toString())
      if (mName.startsWith("get") && !mName.startsWith("getClass") &&
          !mName.startsWith("getMeta") && !mName.startsWith("getProperty")) {
        String colName = convertToUnderLineStyle(mName.substring(3, mName.length()))
        sqlBuilder.append(colName).append(",")
        Class returnType = method.getReturnType()
        // TODO 优化类型
        try {
          if (returnType == String.class) {
            String p = (String) method.invoke(obj)
            if (p == null) {
              p = " "
            }
            valueBuilder.append("'").append(p).append("',")
          } else if (returnType == int.class) {
            long temp = (int) method.invoke(obj)
            valueBuilder.append(temp).append(",")
          } else if (returnType == long.class) {
            long temp = (long) method.invoke(obj)
            valueBuilder.append(temp).append(",")
          } else if (returnType == Integer.class) {
            Integer temp = (Integer) method.invoke(obj)
            if (temp != null) {
              valueBuilder.append(temp).append(",")
            }
          }
          // TODO Date 类型
//                    else if( returnType==Date.class){
//                        Date temp = (Date)method.invoke(obj);
//                        StringBuilder pp = new StringBuilder(new SimpleDateFormat("YYYY-MM-dd HH:MM:SS").format(temp));
//                        valueBuilder.append("'").append(pp.delete(pp.length() - 9, pp.length())).append("',");
//                    }
        } catch (Exception e) {
          e.printStackTrace()
        }
      }
    }
    sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length())
    valueBuilder.delete(valueBuilder.length() - 1, valueBuilder.length())
    sqlBuilder.append(")")
    valueBuilder.append(");")
    String sql = sqlBuilder.toString() + valueBuilder.toString()
    return sql
  }
  /**
   * 传入对象更新对应不为空的值
   *
   * @param obj 实体
   * @param condition 自定义条件 可以为空
   * @return boolean
   * @throws SQLException
   */
  boolean update(Object obj, String condition) throws SQLException {
    if (!isConfigNotNull()) {
      return false
    }
    Class target = obj.getClass()
    StringBuilder sqlBuilder = new StringBuilder("update ")

    Method[] methods = target.getMethods()
    String className = target.getName()
    //通过正则表达式来截取类名，赋值给表名
    String tableName = convertToUnderLineStyle(className)
    sqlBuilder.append(tableName).append(" set ")

    for (Method method : methods) {
      String mName = method.getName()
      if (mName.startsWith("get") && !mName.startsWith("getClass")) {
        String colName = convertToUnderLineStyle(mName.substring(3, mName.length()))
        Class returnType = method.getReturnType()
        // TODO 优化类型
        try {
          if (returnType == String.class) {
            String p = (String) method.invoke(obj)
            if (p != null) {
              sqlBuilder.append(colName).append(" = ").append("'").append(p).append("',")
            }
          } else if (returnType == int.class) {
            long temp = (int) method.invoke(obj)
            sqlBuilder.append(colName).append(" = ").append(temp).append(",")
          } else if (returnType == long.class) {
            long temp = (long) method.invoke(obj)
            sqlBuilder.append(colName).append(" = ").append(temp).append(",")
          } else if (returnType == Integer.class) {
            Integer temp = (Integer) method.invoke(obj)
            if (temp != null) {
              sqlBuilder.append(colName).append(" = ").append(temp).append(",")
            }
          }
        } catch (Exception e) {
          e.printStackTrace()
        }
      }
    }
    sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length())
    sqlBuilder.append(" ").append(condition)
    System.out.println(" 更新  " + sqlBuilder.toString())
    return DBAction.DB_ACTION.initByDBConfig(dbConfig).executeUpdateSQL(sqlBuilder.toString())
  }

  /**
   * 获取指定的class对应的表全部的记录
   *
   * @param target 类对象
   * @return List 对象集合没有泛型
   */
  def <T> List<T> listAll(Class<T> target) {
    String tableName = convertToUnderLineStyle(target.getName())
    return query("select * from " + tableName, target)
  }

  def <T> List<T> query(String sql, Class<T> target) {
    if (!isConfigNotNull()) {
      return null
    }
    List<T> list = new ArrayList<>()
    T obj
    DBAction db = null
    try {
      db = DBAction.DB_ACTION.initByDBConfig(dbConfig)
      ResultSet resultSet = db.queryBySQL(sql)
      Method[] methods = target.getMethods()
      while (resultSet.next()) {
        obj = target.newInstance()
        for (Method method : methods) {//获取所有方法
          String methodName = method.getName()
          if (methodName.startsWith("set") && !methodName.startsWith("setProperty")) {
//将所有set开头的方法取出来
            //根据方法名字自动提取表中对应的列名
            String colName = convertToUnderLineStyle(methodName.substring(3, methodName.length()))
            //得到方法的参数类型
            Class[] params = method.getParameterTypes()
//							System.out.print(" : "+resultSet.getString(colName)+"\n");
            //根据相应的类型来给对象赋值 TODO case 或者设计模式
            if (params[0] == String.class) {
              method.invoke(obj, resultSet.getString(colName))
            } else if (params[0] == int.class) {
              method.invoke(obj, resultSet.getInt(colName))
            } else if (params[0] == long.class) {
              method.invoke(obj, resultSet.getLong(colName))
            } else if (params[0] == Date.class) {
              method.invoke(obj, resultSet.getDate(colName))
            }
          }
        }
        list.add(obj)
      }

    } catch (Exception e) {
      e.printStackTrace()
    } finally {
      if (db != null) {
        db.closeAll()
      }
    }
    return list
  }

  /**
   * 将驼峰风格字符串转换为下划线风格
   * @param origin 类名或者属性名
   */
  static String convertToUnderLineStyle(String origin) {
    String tableName = origin.split("\\.")[origin.split("\\.").length - 1]
    // 如果首字母大小就先将首字母小写
    Pattern pattern = Pattern.compile("^[A-Z]")
    Matcher matcher = pattern.matcher(tableName)
    if (matcher.find()) {
      tableName = tableName.charAt(0).toLowerCase().toString() +
          tableName.substring(1, tableName.length())
    }
    // 将其余的大写字母前追加_ 最后整个字符串转为小写
    pattern = Pattern.compile("[A-Z]")
    matcher = pattern.matcher(tableName)
    while (matcher.find()) {
      String target = matcher.group().trim()
      tableName = tableName.replace(target, "_" + target)
    }
    return tableName.toLowerCase()
  }

  boolean isConfigNotNull() {
    if (dbConfig == null) {
      log.info("未手动初始化DBConfig, 将采用默认配置")
      defaultInit()
      if (dbConfig == null) {
        log.error("尚未初始化 数据库配置 DBConfig, 尝试加载默认配置也失败")
        return false
      }
    }
    return true
  }
}
