package cardgame

import scala.collection.mutable.Queue
import scala.collection.mutable.ArrayBuffer

// https://github.com/evolution-gaming/recruitment/blob/master/backend/GameServer.md

object Games {
    val singleCard: Queue[User] = new Queue[User]()    
}

case class Game(
    val players: ArrayBuffer[User]    
)