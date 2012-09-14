Instancez
=========

This library is a collection of Scalaz 7 type class instances for various
classes from other Scala and Java libraries.

It doesn't aim for comprehensiveness, and is likely to remain a grab bag of
things that the author (Travis Brown) finds useful.

The following are a few of the libraries that may be represented initially:

 * [Dispatch 0.9](http://dispatch.databinder.net/Dispatch.html)
 * [Shapeless](https://github.com/milessabin/shapeless)
 * [Lift](http://liftweb.net/)

See [this Stack Overflow question](http://stackoverflow.com/q/12426269/334519)
by the author for more background.

Shapeless usage examples
------------------------

First for imports and set up:

    import org.instancez.shapeless._
    import scalaz._, std.list._, std.option._, std.string._, syntax.monoid._

    case class Foo(a: Option[String], b: List[Int])

    implicit val fooIso = shapeless.Iso.hlist(Foo.apply _, Foo.unapply _)

And now we have a monoid instance for `Foo`:

    scala> Foo(Some("bar"), 1 :: Nil) |+| Foo(Some("baz"), 2 :: 3 :: Nil)
    res0: Foo = Foo(Some(barbaz),List(1, 2, 3))

Lift usage examples
-------------------

Imports again:

    import net.liftweb.common.{ Box, Full }
    import org.instancez.lift._
    import scalaz._, std.list._, syntax.traverse._

And now we've got a monad for `Box`, which means we also have an applicative
functor for `Box`, which means we can sequence a list of boxes:

    scala> val boxen: List[Box[Int]] = Full(1) :: Full(2) :: Full(3) :: Nil
    boxen: List[net.liftweb.common.Box[Int]] = List(Full(1), Full(2), Full(3))

    scala> boxen.sequence
    res0: net.liftweb.common.Box[List[Int]] = Full(List(1, 2, 3))

