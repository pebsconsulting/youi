package org.hyperscala.web

import org.hyperscala.html._
import org.hyperscala.module.ModularPage
import org.powerscala.concurrent.Temporal
import org.powerscala.concurrent.Time._
import org.powerscala.hierarchy.ParentLike
import com.outr.net.http.HttpHandler
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import org.hyperscala.{Markup, Unique}
import org.powerscala.MapStorage
import java.io.OutputStream
import org.powerscala.hierarchy.event.StandardHierarchyEventProcessor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Webpage extends HttpHandler with HTMLPage with ModularPage with Temporal with ParentLike[tag.HTML] {
  Website().requestContext("webpage") = this

  private val _rendered = new AtomicBoolean(false)
  def rendered = _rendered.get()

  val pageId = Unique()
  val store = new MapStorage[Any, Any]

  val pageLoadingEvent = new StandardHierarchyEventProcessor[Webpage]("pageLoading")
  val pageLoadedEvent = new StandardHierarchyEventProcessor[Webpage]("pageLoaded")

  val doctype = "<!DOCTYPE html>\r\n"
  private lazy val basicHTML = new tag.HTML

  def html = basicHTML

  def head = html.head
  def body = html.body

  protected lazy val hierarchicalChildren = List(html)

  def onReceive(request: HttpRequest, response: HttpResponse) = {
    val status = HttpResponseStatus.OK
    val content = new HTMLStreamer(html) {
      override def stream(output: OutputStream) = {
        pageLoading()
        super.stream(output)
        pageLoaded()
      }
    }
    val headers = response.headers.CacheControl()
    response.copy(content = content, status = status, headers = headers)
    // TODO: error handling
  }

  /**
   * The amount of time in seconds this webpage will continue to be cached in memory without any communication.
   *
   * Defaults to 2 minutes.
   */
  def timeout = 2.minutes

  /**
   * Called before the page is (re)loaded.
   */
  def pageLoading() = {
    pageLoadingEvent.fire(this)
  }

  /**
   * Called after the page is (re)loaded.
   */
  def pageLoaded() = {
    html.byTag[HTMLTag].foreach(Markup.rendered)
    _rendered.set(true)
    pageLoadedEvent.fire(this)
  }

  def dispose() = {
    Website().pages.remove(pageId)
  }
}

object Webpage {
  def apply() = Website().requestContext[Webpage]("webpage")
}