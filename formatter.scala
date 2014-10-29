package com.todesking.dox

class Markdown(
  val optimalWidth:Int = 80
) {
  def render(markups:Seq[Markup]):String =
    markups.map(render(_)).mkString("")

  def render(markup:Markup):String = {
    markup match {
      case Markup.Text(content) =>
        content
      case Markup.Paragraph(children) =>
        paragraph(children.map(render(_)).mkString(""))
      case Markup.Dl(items) =>
        definitionList(items.map {item => (render(item.dt), render(item.dd))})
      case Markup.Code(content) =>
        code(content)
      case Markup.CodeInline(content) =>
        inlineCode(content)
      case Markup.LinkInternal(title, id) =>
        link(title, id.fullName)
      case Markup.LinkExternal(title, url) =>
        link(title, url)
      case Markup.Bold(contents) =>
        bold(contents.map(render(_)).mkString(""))
      case Markup.Italic(contents) =>
        italic(contents.map(render(_)).mkString(""))
      case Markup.UnorderedList(items) =>
        unorderedList(items.map{li => render(li.contents)})
    }
  }

  def code(str:String):String = s"""
    |```scala
    |${str}
    |```
    |
    |""".stripMargin

  def inlineCode(str:String):String = s"`${cleanup(str)}`"

  def h(level:Int)(str:String):String = ("#" * level) + " " + str + "\n\n"

  def h2bar(str:String):String =
    s" ${str}\n${"-" * (str.length + 2)}\n\n"

  def paragraph(str:String):String =
    cleanup(str) + "\n\n"

  def link(title:String, url:String):String =
    s"[${title}](${url})"

  // Seq[(dtContent, ddContent)]
  def definitionList(items:Seq[(String ,String)]):String =
    (items.map {case (dt, dd) =>
      "* " + cleanup(dt) + "\n" + "    * " + cleanup(dd) + "\n"
    }).mkString("") + "\n"

  def unorderedList(items:Seq[String]):String =
    items.map {item =>
      s"* ${item}\n"
    }.mkString

  def bold(content:String) =
    s" *${content}* "

  def italic(content:String) =
    s" _${content}_ "

  private def cleanup(str:String):String =
    """^\n+|\n+$""".r.replaceAllIn(str, "")
}

class MarkdownFormatter {
  def format(item:Item, repo:Repository):String = {
    val sb = new scala.collection.mutable.StringBuilder
    val renderer = new Markdown(80)

    sb.append(renderer.h(1)(item.id.fullName))
    sb.append(renderer.code(item.signature))
    sb.append(renderer.render(item.comment))

    repo.childrenOf(item).sortBy(_.id.fullName).foreach {child =>
      sb.append('\n')
      sb.append(renderer.h2bar(renderer.inlineCode(child.signature)))
      sb.append(renderer.render(child.comment))

      child match {
        case c:ViaImplicitMethod =>
          sb.append(s" (added by implicit convertion: ${c.originalId})\n")
        case c:ViaInheritMethod =>
          sb.append(s"    (defined at ${c.originalId})\n")
        case _ =>
      }
      sb.append("\n")
    }

    sb.toString
  }
}
