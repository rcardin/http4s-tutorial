package in.rcard.http4s.tutorial

import cats.effect._
import in.rcard.http4s.tutorial.movie.MovieApp
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze._

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {

    val apis = Router(
      "/api/v1" -> MovieApp.movieRoutes[IO],
      "/api/v2" -> MovieApp.directorRoutes[IO]
    ).orNotFound

    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(apis)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
