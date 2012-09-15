Instancez
=========

This library is a collection of Scalaz 7 type class instances for various
classes from other Scala and Java libraries.

It doesn't aim for comprehensiveness, and is likely to remain a grab bag of
things that the author (Travis Brown) finds useful.

The following are a few of the libraries that will be represented initially:

 * [Shapeless](https://github.com/milessabin/shapeless)
 * [The Lift web framework](http://liftweb.net/)
 * [Dispatch 0.9](http://dispatch.databinder.net/Dispatch.html)

See [this Stack Overflow question](http://stackoverflow.com/q/12426269/334519)
by the author for more background.

Shapeless
---------

The `org.instancez.shapeless` package contains the following instances:

 * `Monoid` for `HList`s (when they're made up of monoids)
 * `Monoid` for things that are isomorphic to `HList` (most relevantly case classes)

### Shapeless usage examples ###

First for imports and set up:

    import org.instancez.shapeless._
    import scalaz._, std.list._, std.option._, std.string._, syntax.monoid._

    case class Foo(name: Option[String], things: List[Int])

    implicit val fooIso = shapeless.Iso.hlist(Foo.apply _, Foo.unapply _)

And now we have a monoid instance for `Foo`:

    scala> val a = Foo(Some("bar"), 1 :: Nil)
    a: Foo = Foo(Some(bar),List(1))

    scala> val b = Foo(Some("baz"), 2 :: 3 :: Nil)
    b: Foo = Foo(Some(baz),List(2, 3))

    scala> a |+| b
    res0: Foo = Foo(Some(barbaz),List(1, 2, 3))

We can of course change the behavior of the monoid instance for the case class
by changing the monoid instances for the types of its members. For example, if
we want to use the monoid as a kind of update operation for the `name` member,
we can put the "last" monoid instance for `Option` into scope.

    implicit def lastOptionMonoid[A] = new Monoid[Option[A]] {
      def zero = None
      def append(x: Option[A], y: => Option[A]) = y orElse x
    }

Now we can "replace" the `name` value in a `Foo` by adding another `Foo` with
a `Some` for its name (while a `None` leaves it unchanged):

    scala> a |+| Foo(Some("baz"), Nil)
    res1: Foo = Foo(Some(baz),List(1))

    scala> a |+| Foo(None, Nil)
    res2: Foo = Foo(Some(bar),List(1))

It would arguably be better just to make `name` a `LastOption` in this case,
though.

Lift
----

The `org.instancez.lift` package contains the following instances:

 * `Monad` for `Box`

`Box` is essentially half-way between an `Option` and an `Either`, and a lot
of the machinery that Scalaz provides for these types is useful for `Box` as
well.

The package also includes `full` and `empty` on the model of Scalaz's `some`
and `none`.

### Lift usage examples ###

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

Or, more concisely with `full`:

    scala> List(full(1), full(2), full(3)).sequence
    res1: net.liftweb.common.Box[List[Int]] = Full(List(1, 2, 3))

This won't compile if we use `Full`, since the inferred type of the list
would be `List[Full[Int]]`, and we of course don't have a monad for `Full`.

Dispatch
--------

The `org.instancez.dispatch` package contains the following instances:

 * `Monad` for `Promise`

The monad instance isn't necessarily terribly useful on its own, but it
allows us for example to use Scalaz's `EitherT` monad transformer to work with
things of type `Promise[Either[Throwable, A]]` (a common idiom) monadically.

### Dispatch usage examples ###

Suppose we've got a `Promise` representing something like some JSON containing
an OAuth token:

    import dispatch._

    val tokenJson: Promise[String] = Promise("""
      { "access_token": "61a6efeaf1423aefefb3696dfaf684eb" }
    """)

This is a simplified example—in real life this would of course come from the
Internet.

Now we want to parse this and grab the token itself. I'm using the standard
library JSON parser here only for the sake of having a more convenient complete
working example—I know it's hideous. All that really matters here is the type
of `token`.

    import org.instancez.dispatch._
    import scala.util.parsing.json.JSON.parseFull
    import scalaz._

    type EitherPromise[A] = EitherT[Promise, Throwable, A]

    val tokenP: EitherPromise[String] = EitherT.fromEither(tokenJson.map(
      parseFull(_).flatMap(
        _.asInstanceOf[Map[String, Any]].get("access_token")
      ).asInstanceOf[Option[String]].toRight(
        new RuntimeException("Invalid JSON response.")
      )
    ))

Now we want to use this token to construct a new request and end up with
another promise of the same type:

    val resultP: EitherPromise[String] = tokenP.flatMap { token =>
      val stuff = url("https://example.com/api/stuff/") <:< Map(
        "Authorization" -> ("Bearer " + token)
      )

      EitherT.fromEither(Http(stuff OK as.String).either)
    }

We could go on and parse or otherwise process the response inside the
`EitherPromise` monad, but for this example I'll stop here.

    resultP.run().toEither match {
      case Right(content)        => println("Success! " + content)
      case Left(StatusCode(404)) => println("Not found!")
      case Left(e)               => println("Something went wrong! " + e)
    }

