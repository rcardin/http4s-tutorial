package in.rcard.http4s.tutorial.shopping

import cats.Applicative
import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

trait ShoppingCarts[F[_]] {
  def create(id: ShoppingCarts.ShoppingCartId): F[Unit]
  def findBy(id: ShoppingCarts.ShoppingCartId): F[ShoppingCarts.ShoppingCart]
}

object ShoppingCarts {

  final case class ProductId(id: String) extends AnyVal
  final case class Product(id: ProductId, description: String)
  final case class ShoppingCartId(id: String) extends AnyVal

  final case class ShoppingCart(id: ShoppingCartId, products: List[Product])
  object ShoppingCart {
    implicit val jokeDecoder: Decoder[ShoppingCart] = deriveDecoder[ShoppingCart]
    implicit def jokeEntityDecoder[F[_]: Sync]: EntityDecoder[F, ShoppingCart] = jsonOf
    implicit val jokeEncoder: Encoder[ShoppingCart] = deriveEncoder[ShoppingCart]
    implicit def jokeEntityEncoder[F[_]: Applicative]: EntityEncoder[F, ShoppingCart] = jsonEncoderOf
  }

  def impl[F[_]]: ShoppingCarts[F] = new ShoppingCarts[F] {
    override def create(id: ShoppingCartId): F[Unit] = ???

    override def findBy(id: ShoppingCartId): F[ShoppingCart] = ???
  }
}
