package in.rcard.http4s.tutorial

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    Http4stutorialServer.stream[IO].compile.drain.as(ExitCode.Success)
}
