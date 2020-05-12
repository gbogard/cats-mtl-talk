package example

import cats.data.EitherT
import cats.effect.IO
import cats._
import cats.implicits._
import cats.mtl._
import cats.mtl.implicits._
import example.AuthenticationError.WrongPassword

object MTLAuthenticationExample {

  def findUserByName[F[_]](name: String)(
    implicit ME: MonadError[F, Throwable]
  ): F[User] = ME.raiseError(new RuntimeException("The database cannot be reached"))

  def checkPassword[F[_]](user: User, password: String)(
    implicit FR: FunctorRaise[F, AuthenticationError],
    M: Monad[F]
  ): F[Unit] = if (password == "1234") M.unit else FR.raise(WrongPassword)

  def checkSubscription[F[_]](user: User): F[Unit] = ???

  def checkUserStatus[F[_]](user: User): F[Unit] = ???

  def authenticate[F[_]](userName: String, password: String)(
    implicit ME: MonadError[F, Throwable],
    functorRaise: FunctorRaise[F, AuthenticationError]
  ): F[User] = {

    for {
      user <- findUserByName[F](userName)
      _ <- checkPassword[F](user, password)
      _ <- checkSubscription[F](user)
      _ <- checkUserStatus[F](user)
    } yield user
  }

  // Interpreting the program
  object Main extends App {
    type F[A] = EitherT[IO, AuthenticationError, A]
    authenticate[F]("john.doe", "123456")
  }

}
