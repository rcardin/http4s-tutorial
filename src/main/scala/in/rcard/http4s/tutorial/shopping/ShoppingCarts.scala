package in.rcard.http4s.tutorial.shopping

trait ShoppingCarts[F[_]] {
  def findById()
}

object ShoppingCarts {

  final case class ProductId(id: String) extends AnyVal
  final case class Product(id: ProductId, description: String)
  final case class ShoppingCartId(id: String) extends AnyVal
  final case class ShoppingCart(id: ShoppingCartId, products: List[Product])

  // TODO
}
