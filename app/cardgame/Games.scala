package cardgame

import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import scala.collection._
import scala.collection.mutable.Queue
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import io.circe.syntax._
import io.circe.generic.semiauto._
import io.circe.parser.decode
import io.circe.Encoder


// https://github.com/evolution-gaming/recruitment/blob/master/backend/GameServer.md

object GameDAO {
    val USERS_PER_GAME = 2
    private val lock  = new ReentrantReadWriteLock()

    private val singleCardQueue: Queue[User] = new Queue[User]()
    private val doubleCardQueue: Queue[User] = new Queue[User]()
    private val games: ArrayBuffer[Game] = new ArrayBuffer[Game]()

    def askForGame(user: User, gameType: GameType): Option[String] = {
        val game: Option[Game] =
            try {
                lock.readLock().tryLock(10, TimeUnit.SECONDS)
                games.find(g => {
                    g.gameType == gameType && 
                    g.users.exists(_.login == user.login)})
            } finally {
                lock.readLock().unlock()
            }
        game match {
            case Some(g) => Some(g.id)
            case None => addSingleCardGameUser(user, gameType).map(_.id)
        }
    }

    def find(gameId: String): Option[Game] = {
        try {
            lock.readLock().tryLock(10, TimeUnit.SECONDS)
            games.find(_.id == gameId)
        } finally {
            lock.readLock().unlock()
        }
    }

    def addSingleCardGameUser(user: User, gameType: GameType): Option[Game] = {
        val queue = gameType match {
            case SingleCardGame => singleCardQueue
            case DoubleCardGame => doubleCardQueue
            case _ => throw new Exception("Unknown game type")
        }
        try {
            lock.writeLock().tryLock(10, TimeUnit.SECONDS)            
            if (!queue.exists(u => u.login == user.login)) {
                queue.enqueue(user)
            }
            if (queue.length >= USERS_PER_GAME) {
                val game = new Game((1 to USERS_PER_GAME).map(_ =>
                    queue.dequeue()), gameType)
                games += game
                Some(game)
            } else {
                None
            }            
        } finally {
            lock.writeLock().unlock()
        }
    }
}

sealed case class Action(val name: String)
object Play extends Action("Play")
object Fold extends Action("Fold")
object NoAction extends Action("")
object Action {
    def get(text: String): Action = {
        Seq(Play, Fold).find(_.name == text).get
    }
}

class PlayerTurnState(
    val turnIndex: Int,
    val user: User,
    var card: Option[Card],
    @volatile var action: Option[Action]) {

    implicit val suitEncoder: Encoder[Suit] = deriveEncoder
    implicit val cardEncoder: Encoder[Card] = deriveEncoder
    implicit val actionEncoder: Encoder[Action] = deriveEncoder
    implicit val playerTurnStateEncoder: Encoder[PlayerTurnState] = 
        Encoder.forProduct5("login", "turnIndex", "card", "action", "tokens")(p =>
            (p.user.login, p.turnIndex, p.card.getOrElse(BlankCard), 
             p.action.getOrElse(NoAction), p.user.tokens.get()))

    def this(turnIndex: Int, user: User) = {
        this(turnIndex = turnIndex,
             user = user,
             card = None,
             action = None)
    }

    def toJson: String = {
        this.asJson.toString
    }    
}

class Turn(
    val index: Int,
    val playersStates: Seq[PlayerTurnState])

sealed case class GameType(val name: String)
object SingleCardGame extends GameType("single-card-game")
object DoubleCardGame extends GameType("double-card-game")
object GameType {
    def get(text: String): GameType = {
        Seq(SingleCardGame, DoubleCardGame).find(_.name == text).get
    }
}

class Game(val users: Seq[User],
           val gameType: GameType,
           val id: String = java.util.UUID.randomUUID.toString(),
           private val turnes: ArrayBuffer[Turn] = new ArrayBuffer[Turn]) {
    // Init game state
    {
        val turnIndex = 0
        val firstTurn: Turn = new Turn(
            index = turnIndex,
            playersStates = users.map(p => new PlayerTurnState(turnIndex, p)))
        turnes += firstTurn
        dealCards(firstTurn)
    }

    def calculation(turn: Turn): Unit = {
        val playersPlay: Seq[PlayerTurnState] = turn.playersStates.filter(_.action.get == Play).toSeq
        val playersFold: Seq[PlayerTurnState] = turn.playersStates.filter(_.action.get == Fold).toSeq

        if (playersPlay.isEmpty) {
            users.foreach(_.tokens.addAndGet(-1))
        } else if (playersPlay.length == 1) {
            playersPlay.head.user.tokens.addAndGet(+3)
            playersFold.foreach(_.user.tokens.addAndGet(-3))
        } else {
            val maxRank: Int = playersPlay.map(_.card.get.rank).max            
            if (playersPlay.exists(_.card.get.rank != maxRank) && playersFold.isEmpty) {
                playersPlay.foreach(p => 
                    if (p.card.get.rank == maxRank) {
                        p.user.tokens.addAndGet(+10)
                    } else {
                        p.user.tokens.addAndGet(-10)
                    })
                playersFold.foreach(_.user.tokens.addAndGet(-3))
            }
        }
    }

    def dealCards(turn: Turn): Unit = {
        val currentDeck: mutable.Set[Card] = new mutable.HashSet() ++ Cards.cardDeck
        val random = new Random
        turn.playersStates.foreach(p => {
            val newCard: Card = currentDeck.toSeq(random.nextInt(currentDeck.size))
            currentDeck -= newCard
            p.card = Some(newCard)
        })
    }    

    def getState(user: User): Option[PlayerTurnState] = {
        val turn: Turn = turnes.last
        turn.playersStates.find(_.user.login == user.login)
    }

    def userTakesAction(
            user: User, 
            turnIndex: Int, 
            action: Action): Unit = {
        val thisTurn: Turn = turnes.last
        if (thisTurn.index == turnIndex) {
            thisTurn.playersStates
                .find(_.user.login == user.login)
                .map(_.action = Some(action))
        }
        if (!thisTurn.playersStates.exists(p => p.action.isEmpty)) {
            val turnIndex = turnes.length
            val newTurn = new Turn(
                index = turnIndex,
                playersStates = thisTurn.playersStates.map(p => 
                    new PlayerTurnState(turnIndex, p.user)).toSeq)
            calculation(thisTurn)            
            dealCards(newTurn)
            turnes += newTurn
        }        
    }
}