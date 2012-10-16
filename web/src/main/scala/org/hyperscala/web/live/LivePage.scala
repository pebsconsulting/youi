package org.hyperscala.web.live

import org.hyperscala._
import io.{StringBuilderHTMLWriter, HTMLWriter}
import org.hyperscala.html._
import org.hyperscala.css.StyleSheet
import web.{HeadScript, HeadStyle, Website, HTMLPage}
import actors.threadpool.AtomicInteger
import scala.io.Source
import org.hyperscala.javascript.JavaScriptContent
import annotation.tailrec
import org.hyperscala.html.attributes.Method
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import util.parsing.json.JSON
import org.powerscala.hierarchy.Child

import org.powerscala.concurrent.Time._
import org.powerscala.concurrent.Time
import org.powerscala.property.event.PropertyChangeEvent
import org.powerscala.hierarchy.event.ChildRemovedEvent
import org.powerscala.hierarchy.event.ChildAddedEvent

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class LivePage extends HTMLPage with IdentifyTags with HeadStyle with HeadScript {
  import LivePage.escape

  /**
   * Maximum number of times the client / browser will retry a poll before giving up
   */
  def maximumClientRetries = 10

  /**
   * How frequently the client will poll the server when idle.
   *
   * Defaults to 15 seconds
   */
  def pollInterval: Double = 15.seconds

  /**
   * The amount of time a connection should live without any communication.
   *
   * Defaults to 2 minutes.
   */
  def connectionTimeout: Double = 2.minutes

  /**
   * If true the session will be disconnected after all connections are removed.
   *
   * Defaults to true.
   */
  def killSessionOnDisconnect = true

  def debugMode = false

  implicit val livePage = this

  private var connections = List.empty[LiveConnection]
  protected[live] val increment = new AtomicInteger(0)
  protected def nextId = increment.addAndGet(1)

  private val applying = new ThreadLocal[(PropertyAttribute[_], Any)]

  head.id := "liveHead"
  head.contents(0).id := "liveTitle"
  ensureScript("/js/jquery-1.7.2.js", ".*/jquery.*")
  head.contents += new tag.Script {
    contents += new JavaScriptContent {
      def content = synchronized {   // Called per render!!!
        val connection = new LiveConnection()
        connections = connection :: connections
        LivePage.Template.format(connection.id, increment.get(), maximumClientRetries, Time.millis(pollInterval), debugMode)
      }

      protected def content_=(content: String) {}
    }
  }

  body.id := "liveBody"

  def tagsByStyleSheet(ss: StyleSheet, tags: Iterable[HTMLTag] = view)(f: HTMLTag => Any): Unit = {
    if (tags.nonEmpty) {
      val t = tags.head
      if (t.style.loaded && t.style() == ss) {
        f(t)
      }
      tagsByStyleSheet(ss, tags.tail)(f)
    }
  }

  listeners.synchronous.filter.descendant() {
    case evt: ChildAddedEvent => {
      val parent = evt.parent.asInstanceOf[HTMLTag with Container[HTMLTag]]
      evt.child match {
        case child: HTMLTag => {
          if (parent.id() == null) {
            parent.id := Unique()
          }
          if (child.id() == null) {
            child.id := Unique()
          }
          val index = parent.contents.indexOf(child)
          val instruction = if (index == parent.contents.length - 1) {    // Append to the end
            "$('#%s').append(content);".format(parent.id())
          } else if (index == 0) {                                   // Append before
          val after = parent.contents(1)
            "$('#%s').before(content);".format(after.id())
          } else {
            val before = parent.contents(index - 1)
            "$('#%s').after(content);".format(before.id())
          }
          val writer = HTMLWriter().asInstanceOf[StringBuilderHTMLWriter]
          child.write(writer)
          val content = writer.writer.toString()
          enqueue(LiveChange(nextId, null, instruction, content))
        }
        case js: JavaScriptContent => sendJavaScript(js.content)
      }
    }
    case evt: ChildRemovedEvent => evt.child match {
      case text: tag.Text => {
        val parent = evt.parent.asInstanceOf[HTMLTag with Container[HTMLTag]]
        val index = parent.contents.indexOf(text)
        enqueue(LiveChange(nextId, null, "liveRemoveByIndex('%s', %s);".format(parent.id(), index), null))
      }
      case tag: HTMLTag => enqueue(LiveChange(nextId, null, "liveRemove('%s');".format(tag.id()), null))
      case js: JavaScriptContent => // Nothing necessary
    }
  }

  listeners.synchronous.filter(evt => true) {
    case evt: PropertyChangeEvent if (applying.get() != null && applying.get()._1 == evt.property && applying.get()._2 == evt.newValue) => {
      // Ignoring changes that are pushed from client
//      println("Ignoring change resulting from client: %s".format(evt))
      applying.set(null)  // Ignore only once
    }
    case evt: PropertyChangeEvent => evt.property match {
      case property: PropertyAttribute[_] => property.parent match {
        case t: HTMLTag => if (hasRoot(t) && property != t.style && !t.isInstanceOf[tag.Text]) {
          if (property == t.id && evt.oldValue == null) {
            // Ignore
          } else {
            if (property.shouldRender) {
              Page().intercept.renderAttribute.fire(property) match {
                case Some(pa) => {
                  val key = "%s.%s".format(t.id(), property.name())
                  var content: String = null
                  val script = if (t.isInstanceOf[tag.Title] && property.name() == "content") {
                    "document.title = '%s';".format(property.attributeValue)
                  } else if (t.isInstanceOf[Textual] && property.name() == "content") {
                    content = property.attributeValue
                    "$('#%s').val(content);".format(t.id())
                  } else if (property() == false) {   // Remove attribute
                    "$('#%s').removeAttr('%s');".format(t.id(), property.name())
                  } else {    // TODO: ignore values when HeadScript is in use! - add intercept logic
                    "$('#%s').attr('%s', %s);".format(t.id(), property.name(), scriptifyValue(property))
                  }
                  enqueue(LiveChange(nextId, key, script, null))
                }
                case None => // Shouldn't render
              }
            }
          }
        }
        case ss: StyleSheet => tagsByStyleSheet(ss) {
          case tag => {
            val key = "%s.style.%s".format(tag.id(), property.name())
            val script = "$('#%s').css('%s', %s);".format(tag.id(), property.name(), scriptifyValue(property))
            enqueue(LiveChange(nextId, key, script, null))
          }
        }
        case _ => // Ignore others
      }
      case _ => // Ignore
    }
  }

  private def scriptifyValue(property: PropertyAttribute[_]) = property.manifest.erasure.getClass.getSimpleName match {
    case _ if (property() == null) => "''"
    case _ => "'%s'".format(escape(property.attributeValue))
  }

  private def hasRoot(parent: Any): Boolean = parent match {
    case _ if (parent == this) => true
    case c: Child => hasRoot(c.parent)
    case _ => false
  }

  override def sendRedirect(url: String) = sendJavaScript("window.location.href = '%s';".format(url))

  def reload() = sendJavaScript("window.location.reload(true);")

  def sendJavaScript(js: String, content: String = null) = enqueue(LiveChange(nextId, null, js, content))

  @tailrec
  final def enqueue(change: LiveChange, connections: List[LiveConnection] = this.connections): Unit = {
    if (connections.nonEmpty) {
      if (debugMode) {
        println(change)
      }
      val c = connections.head
      c += change
      enqueue(change, connections.tail)
    }
  }

  override def processRequest(method: Method, request: HttpServletRequest, response: HttpServletResponse) = {
    if (method == Method.Post) {
      Page.instance.set(this)
      try {
        // TODO: extract live events from LivePage
        val postData = Source.fromInputStream(request.getInputStream).mkString
        JSON.parseFull(postData) match {
          case Some(parsed) => {
            val json = parsed.asInstanceOf[Map[String, Any]]
            val connectionId = json("liveId").asInstanceOf[String]
            val messageId = json("liveMessageId").asInstanceOf[Double].toInt
            val messages = json("messages").asInstanceOf[List[Any]]

            messages.foreach {
              case m: Map[_, _] => {
                val map = m.asInstanceOf[Map[String, Any]]
                val id = map("id").asInstanceOf[String]
//                val t = view.find(t => t.id() == id).getOrElse(throw new RuntimeException("Unable to find %s".format(id)))
                view.find(t => t.id() == id) match {
                  case Some(t) => {
                    val messageType = map("type").asInstanceOf[String]
                    messageType match {
                      case "event" => {
                        t.fire(LiveEvent.create(t, map))
                      }
                      case "change" => {
                        val v = map("value").asInstanceOf[String]
    //                    applying.set(true)    // Make sure we don't send extraneous information back
    //                    try {
                          t match {
                            case input: tag.Input => applyChange(input.value, v)
                            case select: tag.Select => {
                              var toSelect: tag.Option = null
                              select.contents.foreach {
                                case option: tag.Option => if (option.value() == v) {
                                  toSelect = option
                                } else {
                                  applyChange(option.selected, false)
                                }
                              }
                              if (toSelect != null) {
                                applyChange(toSelect.selected, true)
                              }
                            }
                            case textArea: tag.TextArea => applyChange(textArea.content, v)
                            case _ => throw new RuntimeException("Change not supported on %s with id %s".format(t.getClass.getName, id))
                          }
    //                    } finally {
    //                      applying.set(false)
    //                    }
                      }
                    }
                  }
                  case None => {    // Unable to find the element by id (could have been removed since last update)
                    println("Unable to find %s by id".format(id))
                  }
                }
              }
              case m => throw new RuntimeException("Unhandled Message Type: %s".format(m))
            }
            pageUpdate()
            val changes = connections.find(c => c.id == connectionId) match {
              case Some(connection) => {
                connection(messageId)
              }
              case None => {
//                System.err.println("Connection dead (%s)!".format(connectionId))
                List(LiveChange(0, null, "window.location.href = window.location.href;", null))
              }
            }
            val formatted = changes.map(change => change.output).mkString("\n")
//            val changesJSON = changes.map(c => new JSONObject(Map("id" -> c.id, "script" -> convertScript(c.script)))).toList
//            val responseArray = JSONArray(changesJSON)
//            val formatted = JSONFormat.defaultFormatter(responseArray)
            response.setContentType("text/plain")
//            println("SENDING[%s]".format(formatted))
            val output = response.getOutputStream
            try {
              output.write(formatted.getBytes)
            } finally {
              output.flush()
              output.close()
            }
          }
          case None => println("Unable to parse as JSON: [%s]".format(postData))
        }
      } finally {
        Page.instance.set(null)
      }
      false   // Never send a response page during POST
    } else {
      true
    }
  }

  private def applyChange[T](property: PropertyAttribute[T], value: T) = {
    applying.set(property -> value)
    try {
      property := value
    } finally {
      applying.set(null)
    }
  }

  /**
   * Called when the client checks in to the server.
   */
  def pageUpdate() = {}

  override def update(delta: Double) {
    super.update(delta)

    // Check connections
    checkConnections()
  }

  @tailrec
  private def checkConnections(connections: List[LiveConnection] = connections): Unit = {
    if (connections.nonEmpty) {
      val connection = connections.head
      if (connection.elapsed > connectionTimeout) {   // Connection has timed out
        this.connections = this.connections.filterNot(c => c == connection)
        if (killSessionOnDisconnect &&  this.connections.isEmpty) {   // Kill the page session
          dispose()
          try {
            Website().session.map.foreach {
              case (key, value) => if (value == LivePage.this) {
                Website().session.update(key, null)   // Remove from the session
              }
            }
            Website().application.map.foreach {
              case (key, value) => if (value == LivePage.this) {
                Website().application.update(key, null)   // Remove from the application
              }
            }
          } catch {
            case t: Throwable => t.printStackTrace()
          }
        }
      }
      checkConnections(connections.tail)
    }
  }
}

object LivePage {
  def apply() = HTMLPage().asInstanceOf[LivePage]

  val Template = Source.fromURL(getClass.getClassLoader.getResource("livepage.js")).mkString

  def escape(s: String) = s.replaceAll("'", "\\\\'")
}