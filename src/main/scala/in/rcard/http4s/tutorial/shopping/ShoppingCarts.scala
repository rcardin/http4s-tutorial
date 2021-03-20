package in.rcard.http4s.tutorial.shopping

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

trait ShoppingCarts[F[_]] {
  def create(id: String): F[Unit]
  def findBy(id: String): F[Option[ShoppingCarts.ShoppingCart]]
}

object ShoppingCarts {

//  @newtype class ProductId(id: String)
//  @newtype class ProductDescription(description: String)
  final case class Product(id: String, description: String)
  object Product {
    implicit val prDecoder: Decoder[Product] = deriveDecoder[Product]
    implicit def prEntityDecoder[F[_]: Sync]: EntityDecoder[F, Product] = jsonOf
    implicit val prEncoder: Encoder[Product] = deriveEncoder[Product]
    implicit def prEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Product] = jsonEncoderOf
  }

//  @newtype case class ShoppingCartId(id: String)

  final case class ShoppingCart(id: String, products: List[Product])
  object ShoppingCart {
    implicit val scDecoder: Decoder[ShoppingCart] = deriveDecoder[ShoppingCart]
    implicit def scEntityDecoder[F[_]: Sync]: EntityDecoder[F, ShoppingCart] = jsonOf
    implicit val scEncoder: Encoder[ShoppingCart] = deriveEncoder[ShoppingCart]
    implicit def scEntityEncoder[F[_]: Applicative]: EntityEncoder[F, ShoppingCart] = jsonEncoderOf
  }

  def impl[F[_]]: ShoppingCarts[F] = new ShoppingCarts[F] {
    override def create(id: String): F[Unit] = ???

    override def findBy(id: String): F[Option[ShoppingCart]] = ???
  }
}
