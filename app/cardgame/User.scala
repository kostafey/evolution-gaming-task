package cardgame

import scala.collection.mutable.ArrayBuffer
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.TimeUnit

case class User(
    val login: String,
    var tokens: AtomicInteger = new AtomicInteger(100)
)

object UserDAO {
    private val lock  = new ReentrantReadWriteLock()
    private val list: ArrayBuffer[User] = new ArrayBuffer[User]()

    def find(login: String): Option[User] = {
        try {
            lock.readLock().tryLock(10, TimeUnit.SECONDS)
            list.find(u => u.login == login)
        } finally {
            lock.readLock().unlock()
        }        
        
    }

    def loginOrAdd(login: String): User = {
        try {
            lock.writeLock().tryLock(10, TimeUnit.SECONDS)
            list.find(u => u.login == login).getOrElse({
                val user: User = User(login)
                list += user
                user
            })
        } finally {
            lock.writeLock().unlock()
        }
    }
}