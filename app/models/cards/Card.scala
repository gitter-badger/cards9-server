package models.cards

import scala.util.Random
import scala.math.{ max, min }
import Card._

/**
 * Battle class of the card.
 *
 * - Physical attacks physical def stat
 * - Magical attacks magical def stat
 * - Flexible attacks lowest def stat
 * - Assault attacks the lowest stat
 *
 * Reference: [[http://finalfantasy.wikia.com/wiki/Tetra_Master_(Minigame)#Battle_class_stat Final Fantasy Wiki]]
 */
sealed trait BattleClass { def uiChar: Char }
case object Physical extends BattleClass { val uiChar: Char = 'P' }
case object Magical extends BattleClass { val uiChar: Char = 'M' }
case object Flexible extends BattleClass { val uiChar: Char = 'X' }
case object Assault extends BattleClass { val uiChar: Char = 'A' }

/**
 * Unique card instance.
 *
 * @param id unique identifier
 * @param ownerId player identifier
 * @param cardType type of card
 * @param power offensive stat
 * @param bclass battle class
 * @param pdef physical defense stat
 * @param mdef magical defense stat
 * @param arrows list of atk/def arrows
 */
case class Card(
  id: Int,
  ownerId: Int,
  cardType: CardType,
  power: Int,
  bclass: BattleClass,
  pdef: Int,
  mdef: Int,
  arrows: List[Arrow]) {

  /**
   * Challenge another card.
   *
   * @param other enemy card
   * @param side location of the enemy card
   *
   * @return a fight result
   */
  def fight(other: Card, side: Arrow): Fight = {
    // We need an arrow pointing to the other card
    require(arrows.contains(side))

    def hitPoints(stat: Int): Int = stat * MAX_LEVEL

    // Battle maths
    def statVs(atkStat: Int, defStat: Int): (Int, Int) = {
      val p1atk = hitPoints(atkStat) + Random.nextInt(MAX_LEVEL)
      val p2def = hitPoints(defStat) + Random.nextInt(MAX_LEVEL)
      (p1atk - Random.nextInt(p1atk + 1), p2def - Random.nextInt(p2def + 1))
    }

    // Fight!!
    if (other.arrows.contains(side.opposite)) {
      val (atkStat, defStat) = bclass match {
        case Physical => (power, other.pdef)
        case Magical  => (power, other.mdef)
        case Flexible => (power, min(other.pdef, other.mdef))
        case Assault => (max(max(power, pdef), mdef),
          min(min(other.power, other.pdef), other.mdef))
      }

      val (atkScore, defScore) = statVs(atkStat, defStat)

      Fight(this, other, atkScore, defScore, atkScore > defScore)
    } else {
      // Instant win 
      Fight(this, other, 0, 0, true)
    }
  }

}

object Card {
  val MAX_LEVEL = 16
}
