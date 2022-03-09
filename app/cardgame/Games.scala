package cardgame

import scala.collection._
import scala.collection.mutable.Queue
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

// https://github.com/evolution-gaming/recruitment/blob/master/backend/GameServer.md

object Games {
    val USERS_PER_GAME = 2

    val singleCardQueue: Queue[User] = new Queue[User]()

    def addSingleCardGameUser(user: User): Boolean = {
        singleCardQueue.enqueue(user)
        if (singleCardQueue.length >= USERS_PER_GAME) {
            val game = new Game((1 to USERS_PER_GAME).map(_ =>
                new Player(singleCardQueue.dequeue())))
            true
        } else {
            false
        }
    }
}

sealed abstract case class Action(name: String)
object Play extends Action("Play")
object Fold extends Action("Fold")

class Player(
    login: String,
    tokens: Int = 100,
    var card: Option[Card],
    var action: Option[Action]) extends User(login, tokens) {

    def this(user: User) = {
        this(login = user.login,
             tokens = user.tokens,
             card = None,
             action = None)
    }        
}

class PlayerTurnState(
    login: String,
    tokens: Int,
    card: Card,
    action: Action) extends Player(login, tokens, Some(card), Some(action)) {

    def this(player: Player) = {
        this(login = player.login,
             tokens = player.tokens,
             card = player.card.get,
             action = player.action.get)
    }
}

class Turn(
    val index: Int,
    val playersStates: Seq[PlayerTurnState])

class Game(val players: Seq[Player]) {    

    val turnes: ArrayBuffer[Turn] = new ArrayBuffer[Turn]
    // Init game state
    {
        turnes += new Turn(
            index = 0,
            playersStates = players.map(p => new PlayerTurnState(p)).toSeq)
        dealCards()
    }

    def calculation(): Unit = {
        val playersPlay: Seq[Player] = players.filter(_.action == Play).toSeq
        val playersFold: Seq[Player] = players.filter(_.action == Fold).toSeq

        if (playersPlay.isEmpty) {
            players.foreach(_.tokens += -1)
        } else if (playersPlay.length == 1) {
            playersPlay.head.tokens += 3
            playersFold.foreach(_.tokens += -3)
        } else {
            val maxCard: Card = playersPlay.map(_.card.get).max            
            playersPlay.foreach(p => 
                if (p.card == maxCard) {
                    p.tokens += 10
                } else {
                    p.tokens += -10
                })
            playersFold.foreach(_.tokens += -3)
        }
    }

    def dealCards(): Unit = {
        val currentDeck: mutable.Set[Card] = new mutable.HashSet() ++ Cards.cardDeck
        val random = new Random
        players.foreach(p => {
            val newCard: Card = currentDeck.toSeq(random.nextInt(currentDeck.size))
            currentDeck -= newCard
            p.card = Some(newCard)
        })
    }    

    def userTakesAction(user: User, action: Action): Boolean = {
        players
            .find(p => p.login == user.login)
            .map(p => p.action = Some(action))

        if (!players.exists(p => p.action.isEmpty)) {
            turnes += new Turn(
                index = turnes.length,
                playersStates = players.map(p => new PlayerTurnState(p)).toSeq)
            calculation()            
            dealCards()
            players.foreach(_.action = None)
            true
        } else {
            false
        }
    }
}