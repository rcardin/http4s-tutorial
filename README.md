<a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="40px" align="right" alt="Cats friendly" /></a>
<br/>

Unleashing the Power of HTTP APIs: The Http4s Library 
================================================

Once we learnt how to define Monoids, Semigroups, Applicative, Monads, and so on, it's time to 
understand how to use them to build a production-ready application. Nowadays, Many applications 
exposes APIs over an HTTP channel. So, it's worth to spend some time to study libraries implementing
such use case.

If we learnt the basics of functional programming using the Cats ecosystem, it's straightforward to
choose the `http4s` library to implement HTTP endpoints. Let's see how to implement some aspects of
HTTP APIs using it.

## Introduction

First thing first, we need a good example to work with. In this case, we need a domain model easy 
enough to allow us focusing on the exposition of simple APIs. 

So, imagine we've just finished watching the Snyder Cut of the Justice League movie, and we are very 
excited about the film. We really want to tell the world how much we enjoyed the movie, and then we 
decide to build our personal "Rotten Tomatoes" application (ugh...).

As we are very experienced developers, we chose to start coding from the backend. Hence, we began in
defining the resources we need in terms of domain.

For sure, we need a `Movie`, and a movie has a `Director`, many `Actors`, a `Synopsis`, and finally 
a `Review`. Without dwelling on the details, we can identify the following APIs among the others:

 * Getting all movies of a director (i.e., Zack Snyder), made during a given year
 * Getting the list of actors of a movie
 * Adding a new director to the application

As we just finished the amazing course on [Cats on Rock The JVM](https://rockthejvm.com/p/cats), we
want to use a library built on the Cats ecosystem to expose the above APIs. Fortunately, the 
[http4s](https://http4s.org/) library is what we are looking for. So, let's get a deep breath, and
start diving into the world of functional programming applied to HTTP APIs. 

## Library Setup

We are going to use the version 0.21.20 of the http4s library. Even if Scala 3 is almost upon us, 
this version of http4s uses Scala 2.13 as target language.

The dependency we must add in the `build.sbt` file are a lot:

```scala
val Http4sVersion = "0.21.23"
val LogbackVersion = "1.2.3"
val MunitVersion = "0.7.20",
val MunitCatsEffectVersion = "0.13.0"
libraryDependencies ++= Seq(
  "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "org.scalameta"   %% "munit"               % MunitVersion           % Test,
  "org.typelevel"   %% "munit-cats-effect-2" % MunitCatsEffectVersion % Test,
  "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
  "org.scalameta"   %% "svm-subs"            % "20.2.0"
)
```

The best way to understand each dependency is to explain it along the way. So, let's start the 
journey along the `http4s` library.

## Http4s Basics

The `http4s` library grounds its function on the concepts of `Request` and `Response`. Indeed, using
the library, we respond to a `Request` using a function of type `Request => Response`. We call this
function a route. In fact, a server is nothing more than a set of routes.

Very often, producing  a `Response` from a `Request` means interacting with databases, external 
services, and so on, which may produce some side effect. However, as diligent functional 
developers, we aim to maintain the referential transparency of our functions. Hence, the library
surrounds the `Response` type into an effect `F` (more to come...), changing the route definition 
in `Request => F[Response]`.

Nevertheless, not all the `Request`s will find a route to a `Response`. So, we need to take into 
consideration this fact, defining a route as a function of type `Request => F[Option[Response]]`.
Using a monad transformer, we can simplify the route type in `Request => OptionT[F, Response]`.

Finally, using the types Cats provides us, we can rewrite the type `Request => OptionT[F, Response]`
using the Kleisli monad transformer. Remembering that the type `Kleisli[F[_], A, B]` is just a 
wrapper around the function `A => F[B]`, our route definition becomes 
`Kleisli[OptionT[F, *], Request, Response]`. Easy, isn't it?

Fortunately, the `http4s` library defines a type alias for the Kleisli monad transformer that is 
easier to understand for human beings: `HttpRoutes[F]`.

Hence, to define the routes that we need for our new shining website it's sufficient to instantiate 
some routes using the above type. Awesome. So, it's time to start our journey. Let's implement the
endpoint returning the list of movies associated with a particular director.

## Http4s DSL

We can image the route that returns the list of movies of a director as something similar to the
following:

```
GET /movies?director=Zack%20Snyder
```

Every route corresponds to an instance of the `HttpRoutes[F]` type. Again, the `http4s` library 
helps us in the definition of such routes, providing us with a dedicated DSL, the `http4s-dsl`.

Through the DSL, we build an `HttpRoutes[F]` using pattern matching, as a sequence of case 
statements. So, let's do it:

```scala
HttpRoutes.of[F] {
  case GET -> Root / "movies" :? DirectoryQueryParamMatcher(director) +& YearQueryParamMatcher (year) => ???
}
```

As we can see, the DSL is very straightforward. We surround the definition of all the routes in the
`HttpRoutes.of` constructor. As we said, we parametrize the routes' definition with an effect `F`, as
we probably have to retrieve information from some external resource.

Then, each `case` statement represents a specific route, and it matches a `Request` object. Hence,
the DSL provides many decostructors for a `Request` object, and everyone is associated with a proper
`unapply` method.

First, the decostructor that we can use to extract the HTTP method of a `Request`s is called `->`, 
and decomposes them as a couple containing a `Method` (i.e. `GET`,`POST`, `PUT`, and so on), and a 
`Path`: 

```scala
// From the Http4s DSL
object -> {
  def unapply[F[_]](req: Request[F]): Some[(Method, Path)] =
    Some((req.method, Path(req.pathInfo)))
}
```

The definition of the route continues extracting the rest of information of the provided URI. If the
path is absolute, we use the `Root` object, which simply consumes the leading `'/'` character at the
beginning of the path.

Each part of the remaining `Path` is read using a specific extractor. We use the `/` extractor to 
pattern match each piece of the path. In this case, we match pure a `String`, that is `movie`.  
However, we will see in a minute that is possible to pattern match also directly in a variable.

The route we are matching contains a query parameter. We can introduce the match of query parameters
using the `:?` extractor. Even though there are many ways of extracting query params, the library
sponsors the use of _matchers_. In detail, extending the `abstract class QueryParamDecoderMatcher`,
we can extract directly into a variable a given parameters:

```scala
object DirectorQueryParamMatcher extends QueryParamDecoderMatcher[String]("director")
```

As we can see, the `QueryParamDecoderMatcher` requires the name of the parameter and its type. Then,
The library  provides the value of the parameter directly in the specified variable, which in our
example is called `director`.

If we have to handle more than one query parameter, we can use the `+&` extractor, as we did in our
example. 

It's possible to manage also optional query parameters. In this case, we have to change the base 
class of our matcher, using the `OptionalQueryParamDecoderMatcher`:

```scala
object YearQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Year]("year")
```

Moreover, every mather uses an instance of the class `QueryParamDecoder[T]` to decode its query 
parameter. The DSL provides us the decoders for basic types, such as `String`, numbers, and so on.
However, we want to decode our `"year"` query parameters directly as an instance of the 
`java.time.Year` class.

TODO