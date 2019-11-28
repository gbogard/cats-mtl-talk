package example

import cats.effect.IO

object NestedEitherExample {
  def findUserByName(username: String): IO[Either[AuthenticationError, User]] = ???

  def checkPassword(user: User, password: String): IO[Either[AuthenticationError, Unit]] = ???

  def checkSubscription(user: User): IO[Either[AuthenticationError, Unit]] = ???

  def checkUserStatus(user: User): IO[Either[AuthenticationError, Unit]] = ???

  /* Does not compile
  def authenticate(userName: String, password: String): IO[Either[AuthenticationError, User]] =
    for {
      user <- findUserByName(userName)
      _ <- checkPassword(user, password)
      _ <- checkSubscription(user)
      _ <- checkUserStatus(user)
    } yield user
   */

  def authenticate(userName: String, password: String): IO[Either[AuthenticationError, User]] =
    findUserByName(userName).flatMap({
      case Right(user) => checkPassword(user, password).flatMap({
        case Right(_) => checkSubscription(user).flatMap({
          case Right(_) => checkUserStatus(user).map(_.map(_ => user))
          case Left(err) => IO.pure(Left(err))
        })
        case Left(err) => IO.pure(Left(err))
      })
      case Left(err) => IO.pure(Left(err))
    })
}
