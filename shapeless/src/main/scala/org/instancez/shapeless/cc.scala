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

import scalaz.Monoid
import scalaz.syntax.monoid._
import shapeless.{ HList, Iso }

trait CaseClassInstances {
  implicit def caseClassMonoid[C, L <: HList](implicit
    iso: Iso[C, L],
    ml: Monoid[L]
  ) = new Monoid[C] {
    val zero = iso from ml.zero
    def append(a: C, b: => C) = iso from (iso.to(a) |+| iso.to(b))
  }
}

