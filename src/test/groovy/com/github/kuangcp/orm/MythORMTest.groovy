package com.github.kuangcp.orm

import com.github.kuangcp.time.GetRunTime
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Created by https://github.com/kuangcp
 * @author kuangcp
 * @date 18-6-15  上午10:35
 */
class MythORMTest {

  DBAction action
  MythORM orm
  GetRunTime countTime = GetRunTime.INSTANCE

  @Before
  void init() {
    action = DBAction.INSTANCE.defaultInit()
    orm = MythORM.INSTANCE.defaultInit()

    def exist = action.isTableExist("user_type")


    if (exist) {
      action.executeUpdateSQL("drop table user_type")
    }
    action.executeUpdateSQL("create table user_type(a int, b int)")
  }

  @Test
  void testSave() {

    List<UserType> userTypeList = new ArrayList<>()

    countTime.startCount()
    for (int i = 0; i < 10; i++) {
      UserType type = new UserType()
      type.a = i
      type.b = i + 2
      userTypeList.add(type)
//      boolean result = orm.save(type)
//      assert result
    }
    boolean result = orm.saveAll(userTypeList)
    countTime.endCount("batch affair")
    assert result
  }

  @Test
  void testPrepare() {

    def statement = action.getConnection().prepareStatement("insert into user_type values(?, ?)")


    countTime.startCount()
    for (int i = 0; i < 10; i++) {
      statement.setInt(1, i)
      statement.setInt(2, i + 1)
      def update = statement.executeUpdate()
      println update
    }
    countTime.endCount("prepare statement")
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
  @Ignore
  void testClassToTableName() {
    String result = MythORM.convertToUnderLineStyle(MythORMTest.class.getName())
    println MythORMTest.class.getSimpleName()
    println result
  }
}