package org.hyperscala.site

import org.hyperscala.html._
import org.hyperscala.examples.Example
import org.powerscala.reflect.CaseValue

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
class HyperscalaExample(example: Example) extends HyperscalaPage {
  main.contents += new tag.Div {
    contents += new tag.H2(content = CaseValue.generateLabel(example.exampleName))
  }
  main.contents += example

  override def sourceURL = example.sourceURL
}
