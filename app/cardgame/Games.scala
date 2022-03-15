package cardgame

import java.util.UUID
import scala.collection._
import scala.collection.mutable.Queue
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import io.circe.syntax._
import io.circe.generic.semiauto._
import io.circe.parser.decode
import io.circe.Encoder
import play.api.Logger

// https://github.com/evolution-gaming/recruitment/blob/master/backend/GameServer.md

object GameDAO {
    val USERS_PER_GAME = 2
    val logger: Logger = Logger(this.getClass())

    private val singleCardQueue: Queue[User] = new Queue[User]()
    private val games: ArrayBuffer[Game] = new ArrayBuffer[Game]()

    def askForGame(user: User): Option[String] = {
        val game: Option[Game] = games
            .find(g => g.users.exists(_.login == user.login))
        game match {
            case Some(g) => Some(g.id)
            case None => addSingleCardGameUser(user).map(_.id)
        }
    }

    def find(gameId: String): Option[Game] = {
        games.find(_.id == gameId)
    }

    def addSingleCardGameUser(user: User): Option[Game] = {
        if (!singleCardQueue.exists(u => u.login == user.login)) {
            singleCardQueue.enqueue(user)
        }
        logger.info("singleCardQueue: " + singleCardQueue)
        if (singleCardQueue.length >= USERS_PER_GAME) {
            val game = new Game((1 to USERS_PER_GAME).map(_ =>
                singleCardQueue.dequeue()))
            games += game
            logger.info("singleCardQueue: " + singleCardQueue)
            Some(game)
        } else {
            None
        }
    }
}

sealed case class Action(name: String)
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
    var action: Option[Action]) {

    implicit val suitEncoder: Encoder[Suit] = deriveEncoder
    implicit val cardEncoder: Encoder[Card] = deriveEncoder
    implicit val actionEncoder: Encoder[Action] = deriveEncoder
    implicit val playerTurnStateEncoder: Encoder[PlayerTurnState] = 
        Encoder.forProduct5("login", "turnIndex", "card", "action", "tokens")(p =>
            (p.user.login, p.turnIndex, p.card.getOrElse(BlankCard), 
             p.action.getOrElse(NoAction), p.user.tokens))

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

class Game(val users: Seq[User], 
           val id: String = java.util.UUID.randomUUID.toString(),
           private val turnes: ArrayBuffer[Turn] = new ArrayBuffer[Turn]) {
    // Init game state
    {
        val turnIndex = 0
        val firstTurn: Turn = new Turn(
            index = turnIndex,
            playersStates = users.map(p => 
                new PlayerTurnState(turnIndex, p)).toSeq)
        turnes += firstTurn
        dealCards(firstTurn)
    }

    def calculation(turn: Turn): Unit = {
        val playersPlay: Seq[PlayerTurnState] = turn.playersStates.filter(_.action.get == Play).toSeq
        val playersFold: Seq[PlayerTurnState] = turn.playersStates.filter(_.action.get == Fold).toSeq

        if (playersPlay.isEmpty) {
            users.foreach(_.tokens += -1)
        } else if (playersPlay.length == 1) {
            playersPlay.head.user.tokens += 3
            playersFold.foreach(_.user.tokens += -3)
        } else {
            val maxRank: Int = playersPlay.map(_.card.get.rank).max            
            if (playersPlay.exists(_.card.get.rank != maxRank) && playersFold.isEmpty) {
                playersPlay.foreach(p => 
                    if (p.card.get.rank == maxRank) {
                        p.user.tokens += 10
                    } else {
                        p.user.tokens += -10
                    })
                playersFold.foreach(_.user.tokens += -3)
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