package cardgame

import scala.collection.mutable.ArrayBuffer

case class User(
    val login: String,
    var tokens: Int = 100
)

object UserDAO {
    private val list: ArrayBuffer[User] = new ArrayBuffer[User]()

    def get(login: String): User = {
        list.find(u => u.login == login).get
    }

    def loginOrAdd(login: String): Option[User] = {
        list.find(u => u.login == login) match {
            case Some(value) => None
            case None => {
                val user: User = User(login)
                list += user
                Some(user)
            }
        }
    }
}