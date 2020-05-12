package example

import cats.Applicative
import cats.mtl.{ApplicativeHandle, FunctorRaise}
import cats.implicits._
import cats.mtl.implicits._

object MTLSecretDocumentExample {

  def readSecretDocument[F[_] : Applicative](user: User)
    (implicit F: FunctorRaise[F, String]): F[SecretDocument] =
    if (user.isAdmin) SecretDocument().pure[F]
    else F.raise("Access Denied!")

  def getDocumentContent[F[_]: Applicative](user: User)
    (implicit A: ApplicativeHandle[F, String]): F[String] =
    readSecretDocument[F](user)
      .map(_.content)
      .handle[String](_ => "Default content")
}
