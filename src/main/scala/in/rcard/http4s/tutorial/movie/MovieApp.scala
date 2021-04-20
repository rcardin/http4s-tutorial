package in.rcard.http4s.tutorial.movie

import cats.effect.{IO, Sync}
import org.http4s.{EntityDecoder, HttpApp, HttpRoutes, QueryParamDecoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.{OptionalQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.circe.jsonOf
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT

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
    List("Henry Cavill", "Gal Godot", "Ezra Miller", "Ben Affleck", "Ray Fisher", "Jason Momoa"),
    "Zack Snyder"
  )

  def movieRoutes[F[_]: Sync]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "movies" :? DirectorQueryParamMatcher(director) +& YearQueryParamMatcher(year) =>
        println(director)
        println(year)
        Ok(List(snjl).asJson)
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
    import cats.syntax.flatMap._
    import cats.syntax.functor._
    HttpRoutes.of[F] {
      case GET -> Root / "directors" / DirectorVar(director) =>
        println(director)
        val okRes = Ok(Director("Zack", "Snyder").asJson)
        okRes
      case req @ POST -> Root / "directors" =>
        for {
          _ <- req.as[Director]
          res <- Ok()
        } yield res
    }
  }

  def allRoutes[F[_]: Sync]: HttpRoutes[F] = {
    import cats.syntax.semigroupk._
    movieRoutes <+> directorRoutes
  }

  def allRoutesComplete[F[_]: Sync]: HttpApp[F] = {
    allRoutes.orNotFound
  }

  def main(args: Array[String]): Unit = {
    val dsl = new Http4sDsl[IO]{}
    import dsl._
    val okRes = Ok(Director("Zack", "Snyder").asJson)
    println(okRes)
  }
}
