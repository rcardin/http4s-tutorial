package in.rcard.http4s.tutorial.movie

import cats.effect.Sync
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, QueryParamDecoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.{OptionalQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.circe.jsonOf

import java.time.Year
import scala.util.Try

object MovieApp {

  object DirectorQueryParamMatcher extends QueryParamDecoderMatcher[String]("director")

  implicit val yearQueryParamDecoder: QueryParamDecoder[Year] =
    QueryParamDecoder[Int].map(Year.of)

  object YearQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Year]("year")

  case class Movie(title: String, year: Int, actors: List[String], director: String)

  val snjl: Movie = Movie(
    "Zack Snyder's Justice League",
    2021,
    List(
      "Henry Cavill",
      "Gal Godot",
      "Ezra Miller",
      "Ben Affleck",
      "Ray Fisher",
      "Jason Momoa"
    ),
    "Zack Snyder"
  )

  def movieRoutes[F[_]: Sync]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "movies" :? DirectorQueryParamMatcher(director) +& YearQueryParamMatcher(year) =>
        Ok(snjl.asJson)
      case GET -> Root / "movies" / UUIDVar(movieId) / "actors" =>
        println(movieId)
        Ok()
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

  def directorRoutes[F[_]: Sync]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    implicit val directorDecoder: EntityDecoder[F, Director] = jsonOf[F, Director]
    HttpRoutes.of[F] {
      case GET -> Root / "directors" / DirectorVar(director) =>
        println(director)
        Ok()
      case req @ POST -> Root / "directors" =>
        for {
          director <- req.as[Director]
        }
    }
  }

  def allRoutes[F[_]: Sync]: HttpRoutes[F] = {
    import cats.syntax.semigroupk._
    movieRoutes <+> directorRoutes
  }
}
