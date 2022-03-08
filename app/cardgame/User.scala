package cardgame

case class User(
    val login: String,
    var tokens: Int = 100
)