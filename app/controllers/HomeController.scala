package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data.Form

import cardgame.User
import cardgame.UserDAO
import cardgame.Games

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
            UserDAO.loginOrAdd(login) match {
              case Some(user) => Ok.withSession("user" -> login)
              case None => Unauthorized
            }
        } else {
            BadRequest
        }
    }

    case class GameTypeForm(gameType: String)

    val gameTypeForm = Form(mapping("gameType" -> text)
      (GameTypeForm.apply)(GameTypeForm.unapply))

    def askForGame = Action(parse.form(gameTypeForm)) { implicit request =>
        request.session.get("user")
        .map { login =>
            val user: User = UserDAO.get(login)
            Games.askForGame(user)
            .map(id => Ok(s"""{"gameId": "${id.toString()}"}""").as("application/json"))
            .getOrElse(NoContent)
        }
        .getOrElse {
            Unauthorized
        }
    }

    case class CheckStateForm(login: String, turn: Int)

    val checkStateForm = Form(mapping("login" -> text, "turn" -> number)
      (CheckStateForm.apply)(CheckStateForm.unapply))

    def getSingleCardGameState() = Action(parse.form(checkStateForm)) {
        implicit request =>
        request.session.get("user")
        .map { login =>
            val checkStateForm: CheckStateForm = request.body
            if (!checkStateForm.login.isBlank() && login == checkStateForm.login) {
                // Game.getState(checkStateForm.login, checkStateForm.turn)
                Ok("Hello " + login)
            } else {
                Unauthorized
            }
        }
        .getOrElse {
            Unauthorized
        }
    }
}
