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

Dispatch usage example
----------------------

Suppose we've got a `Promise` representing something like some JSON containing
an OAuth token:

    import dispatch._

    val tokenJson: Promise[String] = Promise("""
      { "access_token": "61a6efeaf1423aefefb3696dfaf684eb" }
    """)

This is a simplified example—in real life this would of course come from the
Internet.

Now I want to parse this and grab the token itself. I'm using the standard
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

