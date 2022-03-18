package cardgame

import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import scala.collection.mutable.Queue
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import io.circe.syntax._
import io.circe.generic.semiauto._
import io.circe.parser.decode
import io.circe.Encoder
import io.circe.Json

// https://github.com/evolution-gaming/recruitment/blob/master/backend/GameServer.md

object GameDAO {
    val USERS_PER_GAME = 2
    private val lock  = new ReentrantReadWriteLock()

    private val games: ArrayBuffer[Game] = new ArrayBuffer[Game]()

    def askForGame(user: User, gameType: GameType): Option[String] = {
        val game: Option[Game] =
            try {
                lock.readLock().tryLock(10, TimeUnit.SECONDS)
                games.filter(!_.isFinished)
                     .find(g => {
                        g.gameType.name == gameType.name &&
                        g.users.exists(_.login == user.login)})
            } finally {
                lock.readLock().unlock()
            }
        game match {
            case Some(g) => Some(g.id)
            case None => addGameUser(user, gameType).map(_.id)
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

    def addGameUser(user: User, gameType: GameType): Option[Game] = {
        try {
            lock.writeLock().tryLock(10, TimeUnit.SECONDS)
            val queue = gameType.usersQueue
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
object Finish extends Action("Finish")
object NoAction extends Action("")
object Action {
    def get(text: String): Action = {
        Seq(Play, Fold, Finish).find(_.name == text).get
    }
}

class PlayerTurnState(
    val turnIndex: Int,
    val user: User,
    val cards: ArrayBuffer[Card] = new ArrayBuffer[Card](),
    @volatile var action: Option[Action],
    @volatile var isLast: Boolean = false) {    

    def highest: Card = cards.sorted.last
    def lowest: Card = cards.sorted.head

    def this(turnIndex: Int, user: User) = {
        this(turnIndex = turnIndex,
             user = user,
             action = None)
    }

    def toJson: String = {
        this.asJson.toString
    }
}
object PlayerTurnState {
    implicit val suitEncoder: Encoder[Suit] = deriveEncoder
    implicit val cardEncoder: Encoder[Card] = deriveEncoder
    implicit val actionEncoder: Encoder[Action] = deriveEncoder
    implicit val playerTurnStateEncoder: Encoder[PlayerTurnState] =
        Encoder.forProduct6("login", "turnIndex", "isLast", "cards", "action", "tokens")(p =>
            (p.user.login, p.turnIndex, p.isLast, p.cards,
             p.action.getOrElse(NoAction), p.user.tokens.get()))
    implicit val playerTurnStateSeqEncoder: Encoder[Seq[PlayerTurnState]] = 
        new Encoder[Seq[PlayerTurnState]] {
        final def apply(a: Seq[PlayerTurnState]): Json = {
            Json.arr(a.map(_.asJson):_*)
        }
    }
    implicit val playerTurnStateSeqOfSeqEncoder: Encoder[Seq[Seq[PlayerTurnState]]] = 
        new Encoder[Seq[Seq[PlayerTurnState]]] {
        final def apply(a: Seq[Seq[PlayerTurnState]]): Json = {
            Json.arr(a.map(_.asJson):_*)
        }
    }             
}

class Turn(
    val index: Int,
    val playersStates: Seq[PlayerTurnState])

sealed abstract class GameType(
    val name: String,
    val cardsAmount: Int,
    val usersQueue: Queue[User]) {

    def showdownAndResults(turn: Turn): Unit
}
object GameType {
    def get(text: String): GameType = {
        Seq(SingleCardGame, DoubleCardGame).find(_.name == text).get
    }
}
object SingleCardGame extends GameType(
    name = "single-card-game",
    cardsAmount = 1,
    usersQueue = new Queue[User]()) {

    val ALL_FOLD = 1
    val SINGLE_PLAYER_PLAY = 3
    val PLAYER_WIN_OR_LOSE = 10
    override def showdownAndResults(turn: Turn): Unit = {
        val playersPlay: Seq[PlayerTurnState] = turn.playersStates.filter(_.action.get == Play).toSeq
        val playersFold: Seq[PlayerTurnState] = turn.playersStates.filter(_.action.get == Fold).toSeq
        val allGameUsers: Seq[User] = turn.playersStates.map(_.user)

        if (playersPlay.isEmpty) {
            allGameUsers.foreach(_.tokens.addAndGet(-ALL_FOLD))
        } else if (playersPlay.length == 1) {
            playersPlay.head.user.tokens.addAndGet(+SINGLE_PLAYER_PLAY)
            playersFold.foreach(_.user.tokens.addAndGet(-SINGLE_PLAYER_PLAY))
        } else {
            val maxRank: Int = playersPlay.map(_.highest.rank).max
            if (playersPlay.exists(_.highest.rank != maxRank) && playersPlay.length > 1) {
                playersPlay.foreach(p =>
                    if (p.highest.rank == maxRank) {
                        p.user.tokens.addAndGet(+PLAYER_WIN_OR_LOSE)
                    } else {
                        p.user.tokens.addAndGet(-PLAYER_WIN_OR_LOSE)
                    })
            }
            playersFold.foreach(_.user.tokens.addAndGet(-3))
        }
    }
}
object DoubleCardGame extends GameType(
    name = "double-card-game",
    cardsAmount = 2,
    usersQueue = new Queue[User]()) {

    val ALL_FOLD = 2
    val SINGLE_PLAYER_PLAY = 5
    val PLAYER_WIN_OR_LOSE = 20
    override def showdownAndResults(turn: Turn): Unit = {
        val playersPlay: Seq[PlayerTurnState] = turn.playersStates.filter(_.action.get == Play).toSeq
        val playersFold: Seq[PlayerTurnState] = turn.playersStates.filter(_.action.get == Fold).toSeq
        val allGameUsers: Seq[User] = turn.playersStates.map(_.user)

        if (playersPlay.isEmpty) {
            allGameUsers.foreach(_.tokens.addAndGet(-ALL_FOLD))
        } else if (playersPlay.length == 1) {
            playersPlay.head.user.tokens.addAndGet(+SINGLE_PLAYER_PLAY)
            playersFold.foreach(_.user.tokens.addAndGet(-SINGLE_PLAYER_PLAY))
        } else {
            val maxRankHighest: Int = playersPlay.map(_.highest.rank).max
            if (playersPlay.count(_.highest.rank == maxRankHighest) > 1) {
                val maxRankLowest: Int = playersPlay
                    .filter(_.highest.rank == maxRankHighest)
                    .map(_.lowest.rank).max
                playersPlay.foreach(p =>
                    if (p.highest.rank == maxRankLowest) {
                        p.user.tokens.addAndGet(+PLAYER_WIN_OR_LOSE)
                    } else {
                        p.user.tokens.addAndGet(-PLAYER_WIN_OR_LOSE)
                    })
            } else {
                playersPlay.foreach(p =>
                    if (p.highest.rank == maxRankHighest) {
                        p.user.tokens.addAndGet(+PLAYER_WIN_OR_LOSE)
                    } else {
                        p.user.tokens.addAndGet(-PLAYER_WIN_OR_LOSE)
                    })
            }
            playersFold.foreach(_.user.tokens.addAndGet(-5))
        }
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

    def isFinished: Boolean = {
        turnes.exists(_.playersStates.exists(_.isLast))
    }

    def dealCards(turn: Turn): Unit = {
        val currentDeck: scala.collection.mutable.Set[Card] = 
            new scala.collection.mutable.HashSet() ++ Cards.cardDeck
        val random = new Random
        (1 to gameType.cardsAmount).foreach(i =>
            turn.playersStates.foreach(p => {
                val newCard: Card = currentDeck.toSeq(random.nextInt(currentDeck.size))
                currentDeck -= newCard
                p.cards += newCard
            })
        )
    }

    def getState(user: User): Option[PlayerTurnState] = {
        val turn: Turn = turnes.last
        turn.playersStates.find(_.user.login == user.login)
    }

    def getSummary: ArrayBuffer[Seq[PlayerTurnState]] = {
        turnes.map(_.playersStates)
    }    

    def userTakesAction(
            user: User,
            turnIndex: Int,
            action: Action): Unit = {
        val thisTurn: Turn = turnes.last
        if (action == Finish) {
            thisTurn.playersStates.map(_.isLast = true)
        }
        if (thisTurn.index == turnIndex) {
            thisTurn.playersStates
                .find(_.user.login == user.login)
                .map(_.action = Some(action))
        }
        if (!thisTurn.playersStates.exists(p => p.action.isEmpty) && !isFinished) {
            val turnIndex = turnes.length
            val newTurn = new Turn(
                index = turnIndex,
                playersStates = thisTurn.playersStates.map(p =>
                    new PlayerTurnState(turnIndex, p.user)).toSeq)
            gameType.showdownAndResults(thisTurn)
            dealCards(newTurn)
            turnes += newTurn
        }
    }
}