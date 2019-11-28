package example

import java.util.Date

import cats.effect.IO
import cats.implicits._

object ExceptionsExample {

  case object WrongUserName extends RuntimeException("No user with that name")

  case object WrongPassword extends RuntimeException("Wrong password")

  case class ExpiredSubscription(expirationDate: Date) extends
    RuntimeException("Expired subscription")

  case object BannedUser extends RuntimeException("User is banned")

  def findUserByName(username: String): IO[User] = ???

  def checkPassword(user: User, password: String): IO[Unit] = ???

  def checkSubscription(user: User): IO[Unit] = ???

  def checkUserStatus(user: User): IO[Unit] = ???

  def authenticate(userName: String, password: String): IO[User] =
    for {
      user <- findUserByName(userName)
      _ <- checkPassword(user, password)
      _ <- checkSubscription(user)
      _ <- checkUserStatus(user)
    } yield user

  val user = authenticate("john.doe", "foo.bar")
    .flatMap(user => IO {
      println(s"Success! $user")
    })
    .recoverWith({
      case WrongUserName => IO {
        /* Do stuff ... */
      }
      case WrongPassword => IO {
        /* Do stuff ... */
      }
      case ExpiredSubscription(date) => IO {
        /* Do stuff ... */
      }
      case BannedUser => IO {
        /* Do stuff ... */
      }
      case _ => IO {
        println("Another exception was caught !")
      }
    })
}
