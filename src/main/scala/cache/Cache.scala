package cache

import java.util.concurrent.ConcurrentHashMap

/**
  * A simple cache wrapper around null values from Java Map
  */
class Cache[T] {
  val cache: java.util.Map[String, T] = new ConcurrentHashMap[String, T]()
  def set(key: String, value: T): T = cache.put(key, value)
  def get(key: String): Option[T] = Option(cache.get(key))

  def removeAllIf(p: T => Boolean): Unit = {
    val it = cache.keySet().iterator()
    while (it.hasNext) {
      val key = it.next()
      val exec = cache.get(key)
      if (p(exec)) it.remove()
    }
  }
}
