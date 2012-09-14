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
    import shapeless.Iso
    import scalaz._, std.list._, std.option._, std.string._, syntax.monoid._

    case class Foo(a: Option[String], b: List[Int])

    implicit val fooIso = Iso.hlist(Foo.apply _, Foo.unapply _)

And now we have a monoid instance for `Foo`:

    scala> Foo(Some("bar"), 1 :: Nil) |+| Foo(Some("baz"), 2 :: 3 :: Nil)
    res0: Foo = Foo(Some(barbaz),List(1, 2, 3))

