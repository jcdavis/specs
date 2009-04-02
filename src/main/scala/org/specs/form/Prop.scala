package org.specs.form

import scala.xml.NodeSeq
import org.specs.specification._
import org.specs.matcher._
import util.Property

/**
 * The Prop class is a named property which holds:<ul>
 * <li/>an expected value
 * <li/>an actual value
 * <li/>a constraint
 * </ul>
 *
 * This property can then be executed, its results can be fetched and displayed as Html
 * It has also the possibility of being inserted in the Layout of a Form.
 *
 * A Prop property is meant to be declared as "bound" to an actual value:
 *
 *   val customerName = Prop("Customer name", person.name)
 *
 * Then it can be associated an expected value with the apply method (usually in a Form declaration):
 *
 *   customerName("Bill")
 *
 * Different constraints can be set on a Prop, by using the companion object factory methods:
 *
 * // Constructs a Prop with an AnyConstraint. The block will be executed if the property is executed
 * val propWithABlock = Prop("label", actualValue, thisVariableInTheScope must_== thatVariableInTheScope)
 *
 * // Constructs a Prop with a FunctionConstraint. The function will be executed with the actual and expected values if the property is executed
 * val propWithAFunction = Prop("label", actualValue, (actual, expected) => actual must_== expected)
 *
 * // Constructs a Prop with a MatcherConstraint. The function will be executed with the default matcher (BeEqualTo)
 * //  if the property is executed
 * val propWithAMatcherExecutor = Prop("label", actualValue, m => actual must m)
 *
 * // Note that in that case the matcher set on the constraint can be changed with
 * propWithAMatcherExecutor.matchesWith(beGreaterThan(_))
 *
 * Props can be temporarily commented out with the comment() method and thus won't be
 * executed:
 *   name("Eric").comment() // won't check the expected value
 *
 * After execution, Props can be queried for their results using the HasResults trait.
 * Then they can be outputed in Html
 *
 */
class Prop[T](val label: String,
              var expected: Option[T],
              actual: =>Option[T], constraint: Option[Constraint[T]])
        extends Property(expected) with DefaultExecutable with Linkable[Prop[T]] with ToHtml with HasLabel {

  /**
   * The apply method sets the expected value and returns the Prop
   */
  def apply(expected: T): Prop[T] = {
    super.apply(Some(expected))
    this.expected = Some(expected)
    this
  }

  /** shortcut method for this().get returning the contained expected value. */
  def get: T = this().get

  /** this function will be executed if the property is executed via its execute method. */
  def executeThis = constraint.map { c => 
    c.execute(expected)
  }

  /**
   * Display the property and possibly its followers as a Linkable object:
   * val firstName = Prop("First Name", person.firstName)
   * val lastName = Prop("Last Name", person.lastName)
   * (firstName("Eric") --> lastName("Torreborre")).toString
   * // First Name: Eric, Last Name: Torreborre
   */
  override def toString = {
    label + ": " + this().getOrElse("_") +
            (if (next.isEmpty) "" else ", ") +
            next.toList.mkString(", ")
  }
  /** @return the status of the execution or value if the cell hasn't been executed. */
  protected def statusClass = if (executed) status else "value"

  /**
   * Display the Property expected value with a different class attribute
   * depending if it hasn't been executed (white), has failed (yellow),
   * isOk (green, yes!)
   */
  override def toXhtml = {
    if (label.isEmpty) 
      valueCell
    else
      <td>{label}</td> ++ valueCell
  }
  def valueCell = {
    if (executed) {
      if (isOk)
        <td class={statusClass}>{getValue}</td>
      else {
        <td class={statusClass} valign="top"><b>{getValue}</b>
        {issueMessages}
        </td>
      } 
     }
     else
       <td class="value">{this().getOrElse(actual.getOrElse(""))}</td>
  }
  def getValue = format(this().getOrElse(actual.getOrElse("")))
  def format(s: Any) = s match {
    case d: Double => new java.text.DecimalFormat("#.##############").format(d)
    case _: Any => s.toString 
  }
  /**
   * When embedded in an Html table, a Prop doesn't need a new <td/> cell.
   */
  override def toEmbeddedXhtml = toXhtml
  
}
/**
 * Companion object containing default factory methods
 */
case object Prop {
  def apply[T](label: String, value: =>T, toCheck: => Any): Prop[T] = new Prop(label, None, Some(value), Some(AnyConstraint(() => toCheck)))
  def apply[T](label: String, value: =>T, f: (T, T) => Any): Prop[T] = new Prop(label, None, Some(value), Some(FunctionConstraint(value, f)))
  def apply[T](label: String, value: =>T, c: Constraint[T]): Prop[T] = new Prop(label, None, Some(value), Some(c))
  def apply[T](label: String, value: =>T, c: MatcherConstraint[T]): MatcherProp[T] = new MatcherProp(label, None, Some(value), Some(c))
  def apply[T](label: String, value: =>T): MatcherProp[T] = new MatcherProp(label, None, Some(value), None)
  def apply[T](label: String): MatcherProp[T] = new MatcherProp(label, None, None, None)
}

trait HasLabel {
  val label: String
}