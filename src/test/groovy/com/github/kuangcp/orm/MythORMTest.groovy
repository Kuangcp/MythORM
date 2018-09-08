package com.github.kuangcp.orm


import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static com.github.kuangcp.orm.DBAction.DB_ACTION
import static com.github.kuangcp.orm.MythORM.MYTH_ORM
import static com.github.kuangcp.time.GetRunTime.GET_RUN_TIME
/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-15  上午10:35
 */
@Ignore
class MythORMTest {

  DBAction action
  MythORM orm

  @Before
  void init() {
    action = DB_ACTION.defaultInit()
    orm = MYTH_ORM.defaultInit()

    def exist = action.isTableExist("user_type")

    if (exist) {
      action.executeUpdateSQL("drop table user_type")
    }
    action.executeUpdateSQL("create table user_type(a int, b int)")
  }

  @Test
  void testSave() {

    List<UserType> userTypeList = new ArrayList<>()

    GET_RUN_TIME.startCount()
    for (int i = 0; i < 10; i++) {
      UserType type = new UserType()
      type.a = i
      type.b = i + 2
      userTypeList.add(type)
//      boolean result = orm.save(type)
//      assert result
    }
    boolean result = orm.saveAll(userTypeList)
    GET_RUN_TIME.endCount("batch affair")
    assert result
  }

  @Test
  void testPrepare() {
    def statement = action.getConnection().prepareStatement("insert into user_type values(?, ?)")

    GET_RUN_TIME.startCount()
    for (int i = 0; i < 10; i++) {
      statement.setInt(1, i)
      statement.setInt(2, i + 1)
      def update = statement.executeUpdate()
      println update
    }
    GET_RUN_TIME.endCount("prepare statement")
  }

//  @Test
//  void testUpdate() {
//    boolean result = mythORM.update(null, "condition")
//    assert result == true
//  }
//
  @Test
  @Ignore
  void testListAll() {
//    List<UserType> result = MythORM.MYTH_ORM.defaultInit().listAll(UserType.class)
    List<UserType> result = MYTH_ORM.listAll(UserType.class)
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
  @Ignore
  void testClassToTableName() {
    String result = MythORM.convertToUnderLineStyle(MythORMTest.class.getName())
    println MythORMTest.class.getSimpleName()
    println result
  }
}