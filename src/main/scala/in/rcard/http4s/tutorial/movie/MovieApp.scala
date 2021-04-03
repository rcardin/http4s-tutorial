package in.rcard.http4s.tutorial.movie

import cats.effect.Sync
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object MovieApp {

  object DirectoryQueryParamMatcher extends QueryParamDecoderMatcher[String]("director")

  def movieRoutes[F[_]: Sync]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "movies" :? DirectoryQueryParamMatcher(director) => ???
    }
  }
}
