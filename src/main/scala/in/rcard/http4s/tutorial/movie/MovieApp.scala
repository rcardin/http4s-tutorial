package in.rcard.http4s.tutorial.movie

import cats.effect.kernel.Concurrent
import cats.implicits.toBifunctorOps
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.{OptionalValidatingQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import org.http4s.headers.`Content-Encoding`
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT

import java.time.Year
import scala.util.Try

object MovieApp {

  object DirectorQueryParamMatcher extends QueryParamDecoderMatcher[String]("director")

  implicit val yearQueryParamDecoder: QueryParamDecoder[Year] =
    QueryParamDecoder[Int].emap { y =>
      Try(Year.of(y))
        .toEither
        .leftMap { tr =>
          ParseFailure(tr.getMessage, tr.getMessage)
        }
    }

  object YearQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Year]("year")

  case class Movie(id: String, title: String, year: Int, actors: List[String], director: String)

  val snjl: Movie = Movie(
    "6bcbca1e-efd3-411d-9f7c-14b872444fce",
    "Zack Snyder's Justice League",
    2021,
    List("Henry Cavill", "Gal Godot", "Ezra Miller", "Ben Affleck", "Ray Fisher", "Jason Momoa"),
    "Zack Snyder"
  )

  def movieRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "movies" :? DirectorQueryParamMatcher(director) +& YearQueryParamMatcher(maybeYear) =>
        maybeYear match {
          case Some(y) =>
            y.fold(
              _ => BadRequest("The given year is not valid"),
              year =>
                if ("Zack Snyder" == director && year == Year.of(2021))
                  Ok(List(snjl).asJson)
                else
                  NotFound(s"There are no movies for director $director")
            )
          case None => NotFound(s"There are no movies for director $director")
        }
      case GET -> Root / "movies" / UUIDVar(movieId) / "actors" =>
        if ("6bcbca1e-efd3-411d-9f7c-14b872444fce" == movieId.toString)
          Ok(
            List(
              "Henry Cavill",
              "Gal Godot",
              "Ezra Miller",
              "Ben Affleck",
              "Ray Fisher",
              "Jason Momoa"
            ).asJson
          )
        else
          NotFound(s"No movie with id $movieId found")
    }
  }

  case class Director(firstName: String, lastName: String)

  object DirectorVar {
    def unapply(str: String): Option[Director] = {
      if (str.nonEmpty && str.matches(".* .*")) {
        Try {
          val splitStr = str.split(' ')
          Director(splitStr(0), splitStr(1))
        }.toOption
      } else None
    }
  }

  def directorRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    implicit val directorDecoder: EntityDecoder[F, Director] = jsonOf[F, Director]
    import cats.syntax.flatMap._
    import cats.syntax.functor._
    HttpRoutes.of[F] {
      case GET -> Root / "directors" / DirectorVar(director) =>
        if (director == Director("Zack", "Snyder"))
          Ok(Director("Zack", "Snyder").asJson, Header("My-Custom-Header", "value"))
        else
          NotFound(s"No director called $director found")
      case req@POST -> Root / "directors" =>
        for {
          _ <- req.as[Director]
          res <- Ok(`Content-Encoding`(ContentCoding.gzip))
            .map(_.addCookie(ResponseCookie("My-Cookie", "value")))
        } yield res
    }
  }

  def allRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
    import cats.syntax.semigroupk._
    movieRoutes <+> directorRoutes
  }

  def allRoutesComplete[F[_] : Concurrent]: HttpApp[F] = {
    allRoutes.orNotFound
  }
}
