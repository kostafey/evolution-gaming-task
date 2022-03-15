package cardgame

sealed case class Suit(name: String)
object Spades extends Suit("Spades")
object Clubs extends Suit("Clubs")
object Diamonds extends Suit("Diamonds")
object Hearts extends Suit("Hearts")

sealed case class Card(
    val rank: Int,
    val suit: Suit,
    val imagePath: String) extends Ordered[Card] {
    def compare(that: Card): Int = this.rank.compare(that.rank)
}
object BlankCard extends Card(0, null, "../images/blank.png")

object AceOfSpades extends Card(14, Spades, "../images/ace-of-spades.png")
object KingOfSpades extends Card(13, Spades, "../images/king-of-spades.png")
object QueenOfSpades extends Card(12, Spades, "../images/queen-of-spades.png")
object JackOfSpades extends Card(11, Spades, "../images/jack-of-spades.png")
object TenOfSpades extends Card(10, Spades, "../images/ten-of-spades.png")
object NineOfSpades extends Card(9, Spades, "../images/nine-of-spades.png")
object EightOfSpades extends Card(8, Spades, "../images/eight-of-spades.png")
object SevenOfSpades extends Card(7, Spades, "../images/seven-of-spades.png")
object SixOfSpades extends Card(6, Spades, "../images/six-of-spades.png")
object FiveOfSpades extends Card(5, Spades, "../images/five-of-spades.png")
object FourOfSpades extends Card(4, Spades, "../images/four-of-spades.png")
object ThreeOfSpades extends Card(3, Spades, "../images/three-of-spades.png")
object TwoOfSpades extends Card(2, Spades, "../images/two-of-spades.png")
object AceOfClubs extends Card(14, Clubs, "../images/ace-of-clubs.png")
object KingOfClubs extends Card(13, Clubs, "../images/king-of-clubs.png")
object QueenOfClubs extends Card(12, Clubs, "../images/queen-of-clubs.png")
object JackOfClubs extends Card(11, Clubs, "../images/jack-of-clubs.png")
object TenOfClubs extends Card(10, Clubs, "../images/ten-of-clubs.png")
object NineOfClubs extends Card(9, Clubs, "../images/nine-of-clubs.png")
object EightOfClubs extends Card(8, Clubs, "../images/eight-of-clubs.png")
object SevenOfClubs extends Card(7, Clubs, "../images/seven-of-clubs.png")
object SixOfClubs extends Card(6, Clubs, "../images/six-of-clubs.png")
object FiveOfClubs extends Card(5, Clubs, "../images/five-of-clubs.png")
object FourOfClubs extends Card(4, Clubs, "../images/four-of-clubs.png")
object ThreeOfClubs extends Card(3, Clubs, "../images/three-of-clubs.png")
object TwoOfClubs extends Card(2, Clubs, "../images/two-of-clubs.png")
object AceOfDiamonds extends Card(14, Diamonds, "../images/ace-of-diamonds.png")
object KingOfDiamonds extends Card(13, Diamonds, "../images/king-of-diamonds.png")
object QueenOfDiamonds extends Card(12, Diamonds, "../images/queen-of-diamonds.png")
object JackOfDiamonds extends Card(11, Diamonds, "../images/jack-of-diamonds.png")
object TenOfDiamonds extends Card(10, Diamonds, "../images/ten-of-diamonds.png")
object NineOfDiamonds extends Card(9, Diamonds, "../images/nine-of-diamonds.png")
object EightOfDiamonds extends Card(8, Diamonds, "../images/eight-of-diamonds.png")
object SevenOfDiamonds extends Card(7, Diamonds, "../images/seven-of-diamonds.png")
object SixOfDiamonds extends Card(6, Diamonds, "../images/six-of-diamonds.png")
object FiveOfDiamonds extends Card(5, Diamonds, "../images/five-of-diamonds.png")
object FourOfDiamonds extends Card(4, Diamonds, "../images/four-of-diamonds.png")
object ThreeOfDiamonds extends Card(3, Diamonds, "../images/three-of-diamonds.png")
object TwoOfDiamonds extends Card(2, Diamonds, "../images/two-of-diamonds.png")
object AceOfHearts extends Card(14, Hearts, "../images/ace-of-hearts.png")
object KingOfHearts extends Card(13, Hearts, "../images/king-of-hearts.png")
object QueenOfHearts extends Card(12, Hearts, "../images/queen-of-hearts.png")
object JackOfHearts extends Card(11, Hearts, "../images/jack-of-hearts.png")
object TenOfHearts extends Card(10, Hearts, "../images/ten-of-hearts.png")
object NineOfHearts extends Card(9, Hearts, "../images/nine-of-hearts.png")
object EightOfHearts extends Card(8, Hearts, "../images/eight-of-hearts.png")
object SevenOfHearts extends Card(7, Hearts, "../images/seven-of-hearts.png")
object SixOfHearts extends Card(6, Hearts, "../images/six-of-hearts.png")
object FiveOfHearts extends Card(5, Hearts, "../images/five-of-hearts.png")
object FourOfHearts extends Card(4, Hearts, "../images/four-of-hearts.png")
object ThreeOfHearts extends Card(3, Hearts, "../images/three-of-hearts.png")
object TwoOfHearts extends Card(2, Hearts, "../images/two-of-hearts.png")

object Cards {
    val cardDeck: Set[Card] = Set(
        AceOfSpades, KingOfSpades, QueenOfSpades, JackOfSpades, TenOfSpades,
        NineOfSpades, EightOfSpades, SevenOfSpades, SixOfSpades,
        FiveOfSpades, FourOfSpades, ThreeOfSpades, TwoOfSpades,
        AceOfClubs, KingOfClubs, QueenOfClubs, JackOfClubs, TenOfClubs,
        NineOfClubs, EightOfClubs, SevenOfClubs, SixOfClubs,
        FiveOfClubs, FourOfClubs, ThreeOfClubs, TwoOfClubs,
        AceOfDiamonds, KingOfDiamonds, QueenOfDiamonds, JackOfDiamonds,
        TenOfDiamonds, NineOfDiamonds, EightOfDiamonds, SevenOfDiamonds,
        SixOfDiamonds, FiveOfDiamonds, FourOfDiamonds, ThreeOfDiamonds,
        TwoOfDiamonds,
        AceOfHearts, KingOfHearts, QueenOfHearts, JackOfHearts,
        TenOfHearts, NineOfHearts, EightOfHearts, SevenOfHearts,
        SixOfHearts, FiveOfHearts, FourOfHearts, ThreeOfHearts,
        TwoOfHearts)
}