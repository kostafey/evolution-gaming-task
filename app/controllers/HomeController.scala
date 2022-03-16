package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data.Form

import cardgame.User
import cardgame.UserDAO
import cardgame.GameDAO
import cardgame.GameType
import cardgame.PlayerTurnState

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

    def index() = Action { implicit request: Request[AnyContent] =>
        Ok(views.html.index())
    }

    case class UserForm(login: String)

    val userLoginForm = Form(mapping("login" -> text)
      (UserForm.apply)(UserForm.unapply))

    def login = Action(parse.form(userLoginForm)) { implicit request =>
        val userForm: UserForm = request.body
        val login: String = userForm.login
        if (!login.isBlank()) {
            Ok(s"""{"login": "${UserDAO.loginOrAdd(login).login}"}""")
                .as("application/json")
        } else {
            BadRequest
        }
    }

    case class GameTypeForm(login: String, gameType: String)

    val gameTypeForm = Form(mapping("login" -> text, "gameType" -> text)
      (GameTypeForm.apply)(GameTypeForm.unapply))

    def askForGame = Action(parse.form(gameTypeForm)) { implicit request =>
        val gameTypeForm: GameTypeForm = request.body
        UserDAO.find(gameTypeForm.login)
        .map(user => {
            GameDAO.askForGame(user, GameType.get(gameTypeForm.gameType))
            .map(id => Ok(s"""{"gameId": "${id.toString()}"}""").as("application/json"))
            .getOrElse(NoContent)
        })
        .getOrElse {
            Unauthorized
        }
    }

    case class SubmitActionForm(login: String, gameId: String, turnIndex: Int, action: String)

    val submitActionForm = Form(mapping("login" -> text, "gameId" -> text, 
        "turnIndex" -> number, "action" -> text)
      (SubmitActionForm.apply)(SubmitActionForm.unapply))

    def submitAction() = Action(parse.form(submitActionForm)) { implicit request =>
        val submitActionForm: SubmitActionForm = request.body
        UserDAO.find(submitActionForm.login)
        .map { user =>            
            val turnIndex: Int = submitActionForm.turnIndex
            val action: cardgame.Action = cardgame.Action.get(submitActionForm.action)
            val state: Option[PlayerTurnState] = GameDAO
                .find(submitActionForm.gameId)
                .flatMap(g => {
                    g.userTakesAction(user, turnIndex, action)
                    g.getState(user)
                })
            state
                .map(s => Ok(s.toJson).as("application/json"))
                .getOrElse(Ok)
        }
        .getOrElse {
            Unauthorized
        }
    }

    case class GetStateForm(login: String, gameId: String)

    val getStateForm = Form(mapping("login" -> text, "gameId" -> text)
      (GetStateForm.apply)(GetStateForm.unapply))

    def getGameState() = Action(parse.form(getStateForm)) { implicit request =>
        val getStateForm: GetStateForm = request.body
        UserDAO.find(getStateForm.login)
        .map { user =>
            val state: Option[PlayerTurnState] = GameDAO
                .find(getStateForm.gameId)
                .flatMap(g => {
                    g.getState(user)
                })
            state
                .map(s => Ok(s.toJson).as("application/json"))
                .getOrElse(Ok)
        }
        .getOrElse {
            Unauthorized
        }
    }
}
