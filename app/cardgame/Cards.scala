package cardgame

sealed abstract case class Card(val rank: Int) extends Ordered[Card] {
    def compare(that: Card): Int = this.rank.compare(that.rank)
}
object Ace extends Card(14)
object King extends Card(13)
object Queen extends Card(12)
object Jack extends Card(11)
object Ten extends Card(10)
object Nine extends Card(9)
object Eight extends Card(8)
object Seven extends Card(7)
object Six extends Card(6)
object Five extends Card(5)
object Four extends Card(4)
object Three extends Card(3)
object Two extends Card(2)

object Cards {
    val cardDeck: Set[Card] = Set(
        Ace, King, Queen, Jack, Ten, Nine, Eight, 
        Seven, Six, Five, Four, Three, Two)
}