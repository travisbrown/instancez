/*
 * #%L
 * Scalaz Instances for Shapeless
 * %%
 * Copyright (C) 2012 Instancez
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.instancez.shapeless

import org.specs2.mutable._
import shapeless._
import scalaz._, std.list._, std.option._, std.string._, syntax.monoid._

class CaseClassInstancesSpec extends Specification {
  "The sum of two instances of a case classe with monoid members" should {
    "be the case class instance containing the sums of their members" in {
      case class Foo(name: Option[String], things: List[Int])

      implicit val fooIso = shapeless.Iso.hlist(Foo.apply _, Foo.unapply _)

      val a = Foo(Some("bar"), 1 :: Nil)
      val b = Foo(Some("baz"), 2 :: 3 :: Nil)

      (a |+| b) must_== Foo(Some("barbaz"), 1 :: 2 :: 3 :: Nil)
    }
  }
}

