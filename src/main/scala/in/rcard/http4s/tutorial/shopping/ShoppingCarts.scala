package in.rcard.http4s.tutorial.shopping

trait ShoppingCarts[F[_]] {
  def create(id: ShoppingCarts.ShoppingCartId): F[Unit]
  def find(id: ShoppingCarts.ShoppingCartId): F[Option[ShoppingCarts.ShoppingCart]]
}

object ShoppingCarts {

  final case class ProductId(id: String) extends AnyVal
  final case class Product(id: ProductId, description: String)
  final case class ShoppingCartId(id: String) extends AnyVal
  final case class ShoppingCart(id: ShoppingCartId, products: List[Product])

  def impl[F[_]]: ShoppingCarts[F] = new ShoppingCarts[F] {
    override def create(id: ShoppingCartId): F[Unit] = ???

    override def find(id: ShoppingCartId): F[Option[ShoppingCart]] = ???
  }
}
