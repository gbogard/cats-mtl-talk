package example

import java.util.Date

sealed trait AuthenticationError

object AuthenticationError {
  case object WrongUserName extends AuthenticationError
  case object WrongPassword extends AuthenticationError
  final case class ExpiredSubscription(expirationDate: Date) extends AuthenticationError
  case object BannedUser extends AuthenticationError
}
