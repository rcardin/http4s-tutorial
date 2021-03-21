package in.rcard.http4s.tutorial.shopping

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object ShoppingCartRoutes {

  def shoppingCartRoutes[F[_]: Sync](sc: ShoppingCarts[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "carts" / id =>
        for {
          maybeCart <- sc.findBy(ShoppingCarts.ShoppingCartId(id))
          resp <- maybeCart match {
            case Some(cart) => Ok(cart)
            case _ => NotFound()
          }
        } yield resp
    }
  }
}
