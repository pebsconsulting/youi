package io.youi

import java.io.File

import io.youi.stream.{ByClass, ById, HTMLParser}
import org.scalajs.dom.Element

import scala.annotation.compileTimeOnly
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Template {
  def byId[E <: Element](path: String, id: String, appName: String): E = macro TemplateMacros.existingById[E]
  def byClass[E <: Element](path: String, className: String, appName: String): List[E] = macro TemplateMacros.existingByClass[E]
}

@compileTimeOnly("Enable Macros for expansion")
object TemplateMacros {
  def existingById[E <: Element](context: blackbox.Context)(path: context.Expr[String],
                                                            id: context.Expr[String],
                                                            appName: context.Expr[String])(implicit e: context.WeakTypeTag[E]): context.Expr[E] = {
    import context.universe._

    val pathValue = path match {
      case Expr(Literal(Constant(value: String))) => value
    }
    val idValue = id match {
      case Expr(Literal(Constant(value: String))) => value
    }
    val environmentVariable = appName match {
      case Expr(Literal(Constant(value: String))) => s"${value.toUpperCase}_TEMPLATE_PATH"
    }
    val templatePath = Option(System.getenv(environmentVariable))
    val file = templatePath match {
      case Some(basePath) => new File(basePath, pathValue)
      case None => {
        context.warning(context.enclosingPosition, s"No environment variable defined for $environmentVariable.")
        new File(pathValue)
      }
    }
    if (!file.exists()) {
      context.abort(context.enclosingPosition, s"Unable to find path for ${file.getAbsolutePath}.")
    }
    val parser = HTMLParser(file)
    val template = parser.stream(Nil, selector = Some(ById(idValue)))
    if (template.trim.isEmpty) {
      context.abort(context.enclosingPosition, s"No content found for #$idValue in ${file.getAbsolutePath}")
    }

    context.Expr[E](
      q"""
         try {
           io.youi.dom.fromString[$e]($template).head
         } catch {
           case t: Throwable => throw new RuntimeException("Error parsing HTML [" + $template + "] byId " + $idValue, t)
         }
       """)
  }

  def existingByClass[E <: Element](context: blackbox.Context)(path: context.Expr[String],
                                                               className: context.Expr[String],
                                                               appName: context.Expr[String])(implicit e: context.WeakTypeTag[E]): context.Expr[List[E]] = {
    import context.universe._

    val pathValue = path match {
      case Expr(Literal(Constant(value: String))) => value
    }
    val classValue = className match {
      case Expr(Literal(Constant(value: String))) => value
    }
    val environmentVariable = appName match {
      case Expr(Literal(Constant(value: String))) => s"${value.toUpperCase}_TEMPLATE_PATH"
    }
    val templatePath = Option(System.getenv(environmentVariable))
    val file = templatePath match {
      case Some(basePath) => new File(basePath, pathValue)
      case None => new File(pathValue)
    }
    if (!file.exists()) {
      context.abort(context.enclosingPosition, s"Unable to find path for ${file.getAbsolutePath}, $environmentVariable=$templatePath.")
    }
    val parser = HTMLParser(file)
    val template = parser.stream(Nil, selector = Some(ByClass(classValue)), includeAllMatches = true)
    if (template.trim.isEmpty) {
      context.abort(context.enclosingPosition, s"No content found for .$classValue in ${file.getAbsolutePath}")
    }

    context.Expr[List[E]](
      q"""
         try {
           io.youi.dom.fromString[$e]($template)
         } catch {
           case t: Throwable => throw new RuntimeException("Error parsing HTML [" + $template + "] byClass " + $classValue, t)
         }
       """)
  }
}