package example

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import cats.mtl.implicits._
import cats.mtl.{ApplicativeHandle, FunctorRaise}
import cats.{Applicative, ApplicativeError, MonadError}
import example.AuthenticationError.WrongPassword

object MTLExample {

  def readSecretDocument[F[_] : Applicative](user: User)
    (implicit F: FunctorRaise[F, String]): F[SecretDocument] =
    if (user.isAdmin) SecretDocument().pure[F]
    else F.raise("Access Denied!")


  def getDocumentContent[F[_]: Applicative](user: User)
    (implicit A: ApplicativeHandle[F, String]): F[String] =
    readSecretDocument[F](user)
      .map(_.content)
      .handle[String](_ => "Default content")

  def findUserByName[F[_]](name: String)
    (implicit AE: ApplicativeError[F, Throwable]): F[User] = {
    AE.raiseError(new RuntimeException("Database not reachable!"))
  }

  def checkPassword[F[_]](user: User, password: String)
    (implicit F: FunctorRaise[F, AuthenticationError]): F[Unit] =
    F.raise(WrongPassword)

  def checkSubscription[F[_]](user: User): F[User] = ???

  def checkUserStatus[F[_]](user: User): F[User] = ???


  def authenticate[F[_]: Applicative](userName: String, password: String)
    (implicit F: FunctorRaise[F, AuthenticationError], AE: MonadError[F, Throwable]): F[User] =
    for {
      user <- findUserByName[F](userName)
      _ <- checkPassword[F](user, password)
      _ <- checkSubscription[F](user)
      _ <- checkUserStatus[F](user)
    } yield user

  // Interpreting the program
  object Main extends App {
    type F[A] = EitherT[IO, AuthenticationError, A]
    authenticate[F]("john.doe", "123456")
  }

}
