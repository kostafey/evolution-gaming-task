package cardgame

import scala.collection._
import scala.collection.mutable.Queue
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import io.circe.syntax._
import io.circe.generic.semiauto._
import io.circe.parser.decode
import io.circe.Decoder
import java.util.UUID

// https://github.com/evolution-gaming/recruitment/blob/master/backend/GameServer.md

object Games {
    val USERS_PER_GAME = 2

    val singleCardQueue: Queue[User] = new Queue[User]()
    val games: ArrayBuffer[Game] = new ArrayBuffer[Game]()

    def askForGame(user: User): Option[UUID] = {
        val game: Option[Game] = games
            .find(g => g.users.exists(_.login == user.login))
        game match {
            case Some(g) => Some(g.id)
            case None => addSingleCardGameUser(user).map(_.id)
        }
    }

    def addSingleCardGameUser(user: User): Option[Game] = {
        if (!singleCardQueue.exists(u => u.login == user.login)) {
            singleCardQueue.enqueue(user)
        }
        if (singleCardQueue.length >= USERS_PER_GAME) {
            val game = new Game((1 to USERS_PER_GAME).map(_ =>
                singleCardQueue.dequeue()))
            games += game
            Some(game)
        } else {
            None
        }
    }
}

sealed abstract case class Action(name: String)
object Play extends Action("Play")
object Fold extends Action("Fold")
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

    def this(turnIndex: Int, user: User) = {
        this(turnIndex = turnIndex,
             user = user,
             card = None,
             action = None)
    }
}

class Turn(
    val index: Int,
    val playersStates: Seq[PlayerTurnState])

class Game(val users: Seq[User], 
           val id: UUID = java.util.UUID.randomUUID,
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

    def getState(user: User, turnIndex: Int): Option[PlayerTurnState] = {
        val turn: Turn = turnes.last
        if (turn.index == turnIndex) {
            turn.playersStates.find(_.user == user)
        } else {
            None
        }
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
            val maxCard: Card = playersPlay.map(_.card.get).max            
            playersPlay.foreach(p => 
                if (p.card == maxCard) {
                    p.user.tokens += 10
                } else {
                    p.user.tokens += -10
                })
            playersFold.foreach(_.user.tokens += -3)
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

    def userTakesAction(user: User, turnIndex: Int, action: Action): Boolean = {
        val thisTurn: Turn = turnes.last
        if (!thisTurn.playersStates.exists(p => p.action.isEmpty)) {
            val turnIndex = turnes.length
            val newTurn = new Turn(
                index = turnIndex,
                playersStates = thisTurn.playersStates.map(p => 
                    new PlayerTurnState(turnIndex, p.user)).toSeq)
            calculation(newTurn)            
            dealCards(newTurn)
            turnes += newTurn
            true
        } else {
            false
        }
    }
}