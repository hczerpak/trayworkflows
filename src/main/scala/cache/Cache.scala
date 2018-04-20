package cache

import java.util.concurrent.ConcurrentHashMap

/**
  * Created by Hubert Czerpak on 17/04/2018
  * using 11" MacBook (can't see much on this screen).
  */
trait Cache[T] {
  val cache: ConcurrentHashMap[String, T] = new ConcurrentHashMap[String, T]()
  def del(key: String): T = cache.remove(key)
  def set(key: String, value: T): Boolean = {
    cache.put(key, value)
    true
  }
  def get(key: String): Option[T] = Option(cache.get(key))
}
