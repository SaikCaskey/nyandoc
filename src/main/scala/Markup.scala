package com.todesking.dox

sealed abstract class Markup
object Markup {
  case class Text(content:String) extends Markup
  case class Paragraph(children:Seq[Markup]) extends Markup
  case class Dl(items:Seq[DlItem]) extends Markup
  case class DlItem(dt:Seq[Markup], dd:Seq[Markup])
  case class UnorderedList(items:Seq[ListItem]) extends Markup
  case class ListItem(contents:Seq[Markup])
  case class LinkInternal(title:String, id:Id) extends Markup
  case class LinkExternal(title:String, url:String) extends Markup
  case class Code(content:String) extends Markup
  case class CodeInline(content:String) extends Markup
  case class Bold(contents:Seq[Markup]) extends Markup
  case class Italic(contents:Seq[Markup]) extends Markup

  def normalize(markups:Seq[Markup]):Seq[Markup] =
    if(markups.size < 2) markups
    else {
      val tail = normalize(markups.tail)
      (markups.head, tail.head) match {
        case (Markup.Text(c1), Markup.Text(c2)) =>
          Markup.Text(c1 + " " + c2) +: tail.tail
        case _ => markups.head +: tail
      }
    }

}
