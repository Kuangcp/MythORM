package com.github.kuangcp.orm.config

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator

/**
 * Created by https://github.com/kuangcp
 * Yaml的读写工具类
 * @author kuangcp
 * @date 18-6-14  下午8:48
 */
class YamlUtil {

  private static YAMLFactory factory = new YAMLFactory()
  private static ObjectMapper mapper = new ObjectMapper(factory)

  /**
   * TODO 项目根目录问题, 为什么是项目的上级目录???
   *
   * @param object 对象  对象的定义切记要有setget, 而且不能重载这些方法, 不然就会稀奇古怪的错误
   * @param filePath 绝对路径,目前存在问题
   * @return true 创建成功
   */
  static boolean createFile(Object object, String filePath) {
    // TODO 路径问题
    System.out.println(filePath)
    factory.setCodec(mapper)
    YAMLGenerator generator
    try {
      generator = factory.createGenerator(new FileOutputStream(filePath), JsonEncoding.UTF8)
      generator.useDefaultPrettyPrinter()
      generator.writeObject(object)
      return true
    } catch (IOException e) {
      e.printStackTrace()
      return false
    }
  }

  /**
   * 读取yml文件
   *
   * @param target 配置文件对应的类
   * @param filePath 配置文件路径
   * @return 对象 , 找不到则是null
   */
  static <T> T readFile(Class<T> target, String filePath) {
    ObjectMapper mapper = new ObjectMapper(factory)
    try {
      ClassLoader classLoader = YamlUtil.class.getClassLoader()
      URL resource = classLoader.getResource(filePath)
      if (resource != null) {
        filePath = resource.getPath()
      }
      return mapper.readValue(new File(filePath), target)
    } catch (Exception e) {
      e.printStackTrace()
      return null
    }
  }
}
