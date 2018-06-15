package com.github.kuangcp.orm

import org.junit.Test

/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-15  上午10:35
 */
class MythORMTest {

  @Test
  void testSave() {
    DBAction action = DBAction.INSTANCE.defaultInit()
    action.executeUpdateSQL("delete from user_type")
    for (int i = 0; i < 10; i++) {
      MythORM orm = MythORM.INSTANCE.defaultInit()
      UserType type = new UserType()
      type.a = i + 1
      type.b = i + 2
      boolean result = orm.save(type)
      println result
    }
  }

//  @Test
//  void testUpdate() {
//    boolean result = mythORM.update(null, "condition")
//    assert result == true
//  }
//
  @Test
  void testListAll() {
//    List<UserType> result = MythORM.INSTANCE.defaultInit().listAll(UserType.class)
    List<UserType> result = MythORM.INSTANCE.listAll(UserType.class)
    if (result != null) {
      result.forEach({
        println it
      })
    }
  }
//
//  @Test
//  void testQuery() {
//    List<T> result = mythORM.query("sql", null)
//    assert result == [new T()]
//  }
//
  @Test
  void testClassToTableName() {
    String result = MythORM.convertToUnderLineStyle(MythORMTest.class.getName())
    println MythORMTest.class.getSimpleName()
    println result
  }
}