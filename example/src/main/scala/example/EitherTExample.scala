package example

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._

object EitherTExample {
  def findUserByName(username: String): EitherT[IO, AuthenticationError, User] = ???

  def checkPassword(user: User, password: String): EitherT[IO, AuthenticationError, Unit] = ???

  def checkSubscription(user: User): EitherT[IO, AuthenticationError, Unit] = ???

  def checkUserStatus(user: User): EitherT[IO, AuthenticationError, Unit] = ???

  def authenticate(userName: String, password: String): EitherT[IO, AuthenticationError, User] =
    for {
      user <- findUserByName(userName)
      _ <- checkPassword(user, password)
      _ <- checkSubscription(user)
      _ <- checkUserStatus(user)
    } yield user

}
