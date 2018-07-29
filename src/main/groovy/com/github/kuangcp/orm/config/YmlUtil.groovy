package com.github.kuangcp.orm.config

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import groovy.util.logging.Slf4j

/**
 * Created by https://github.com/kuangcp
 * Yml的读写工具类
 * @author kuangcp
 * @date 18-6-14  下午8:48
 */
@Slf4j
class YmlUtil {

  private static YAMLFactory factory = new YAMLFactory()
  private static ObjectMapper mapper = new ObjectMapper(factory)

  /**
   * TODO  idea 中为了简洁, 使用的是多个module共享一个project, 导致各种路径问题
   * @param object 对象  对象的定义切记要有setget, 而且不能重载这些方法, 不然就会稀奇古怪的错误
   * @param filePath 绝对路径,目前存在问题
   * @return true 创建成功
   */
  static boolean createFile(Object object, String filePath) {
//    filePath = "src/main/resources/"+filePath
//    ClassLoader classLoader = YmlUtil.class.getClassLoader()
//    logger.debug(classLoader.getResource("logback.xml") as String)
    log.debug("生成配置文件 filePath={}", filePath)
    factory.setCodec(mapper)
    YAMLGenerator generator
    try {
//      log.debug(new File(filePath).getAbsolutePath())
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
      ClassLoader classLoader = YmlUtil.class.getClassLoader()
      URL resource = classLoader.getResource(filePath)
      if (resource != null) {
        filePath = resource.getPath()
        log.debug("读取yml : " + filePath)
      }
      return mapper.readValue(new File(filePath), target)
    } catch (FileNotFoundException e) {
      log.error("加载yml配置文件, 配置文件找不到", e)
    }catch (Exception e) {
      log.error("加载yml配置文件出错", e)
      return null
    }
  }
}
