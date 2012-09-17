/*
 * #%L
 * Scalaz Instances for the Scala standard library
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
package org.instancez.std

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.util.parsing.combinator._
import scalaz._, Scalaz._

trait StatefulParsers[S] { this: Parsers =>
  type StatefulParser[A] = StateT[Parser, S, A]
  protected def pm: Monad[Parser]

  def lift[A](p: Parser[A]) = StateT.StateMonadTrans[S].liftM(p)(pm)

  def rep1[A](p: => StatefulParser[A]): StatefulParser[List[A]] = rep1(p, p)

  def rep1[A](first: => StatefulParser[A], p0: => StatefulParser[A]):
    StatefulParser[List[A]] = StateT { s =>
    lazy val p = p0
    val elems = new ListBuffer[A]

    def continue(in: Input, t: S): ParseResult[(S, List[A])] = {
      val p0 = p
      @tailrec def applyp(in0: Input, u: S): ParseResult[(S, List[A])] =
        p0.run(u)(in0) match {
          case Success((v, x), rest) => elems += x; applyp(rest, v)
          case _ => Success((u, elems.toList), in0)
        }

      applyp(in, t)
    }

    new Parser[(S, List[A])] {
      def apply(in: Input): ParseResult[(S, List[A])] =
        first.run(s)(in) match {
          case Success((u, x), rest) => elems += x; continue(rest, u)
          case ns: NoSuccess => ns
        }
    }
  }

  class SPW[A](p: StatefulParser[A]) {
    def ~> [B](q0: => StatefulParser[B]): StatefulParser[B] = {
      lazy val q = q0
      StateT { s =>
        new Parser[(S, B)] {
          def apply(in: Input) = p.run(s)(in).flatMapWithNext {
            case (t, _) => q.run(t)
          }
        }
      }
    }

    def <~ [B](q0: => StatefulParser[B]): StatefulParser[A] = {
      lazy val q = q0
      StateT { s =>
        new Parser[(S, A)] {
          def apply(in: Input): ParseResult[(S, A)] = p.run(s)(in) match {
            case Success((t, r), rest) =>
              q.run(t)(rest).map { case (t, _) => (t, r) }
            case e => e
          }
        }
      }
    }
  }

  implicit def toSPW[A](p: StatefulParser[A]) = new SPW(p)
}

