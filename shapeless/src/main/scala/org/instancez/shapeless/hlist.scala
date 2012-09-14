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

import scalaz._, syntax.monoid._
import shapeless._

trait HListInstances {
  implicit object hnilMonoid extends Monoid[HNil] {
    val zero = HNil
    def append(a: HNil, b: => HNil) = HNil
  }

  implicit def hlistMonoid[H, T <: HList](implicit
    mh: Monoid[H],
    mt: Monoid[T]
  ): Monoid[H :: T] = new Monoid[H :: T] {
    val zero = mh.zero :: mt.zero
    def append(a: H :: T, b: => H :: T) =
      (a.head |+| b.head) :: (a.tail |+| b.tail)
  }
}

