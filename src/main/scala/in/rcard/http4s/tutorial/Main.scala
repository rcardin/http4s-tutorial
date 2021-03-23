package in.rcard.http4s.tutorial

import cats.effect.{ExitCode, IO, IOApp}
import in.rcard.http4s.tutorial.shopping.ShoppingCartsServer

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    ShoppingCartsServer.stream[IO].compile.drain.as(ExitCode.Success)
}
