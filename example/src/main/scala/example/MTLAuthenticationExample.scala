package example

import cats.data.EitherT
import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats._
import cats.implicits._
import cats.mtl._
import cats.mtl.implicits._
import example.AuthenticationError.{WrongPassword, WrongUserName}

object MTLAuthenticationExample extends IOApp {

  // We start by splitting the authentication logic into composable pieces
  // Each piece uses exactly the type classes it needs, no more no less

  def findUserByName[F[_]](name: String)(implicit
      ME: MonadError[F, Throwable]
  ): F[User] =
    ME.raiseError(new RuntimeException("The database cannot be reached"))

  def checkPassword[F[_]](user: User, password: String)(implicit
      FR: FunctorRaise[F, AuthenticationError],
      M: Monad[F]
  ): F[Unit] = if (password == "1234") M.unit else FR.raise(WrongPassword)

  def checkSubscription[F[_]](user: User): F[Unit] = ???

  def checkUserStatus[F[_]](user: User): F[Unit] = ???

  // Then we compose our pieces into a more complex authentication method

  def authenticate[F[_]](userName: String, password: String)(implicit
      ME: MonadError[F, Throwable],
      functorRaise: FunctorRaise[F, AuthenticationError]
  ): F[User] = {

    for {
      user <- findUserByName[F](userName)
      _ <- checkPassword[F](user, password)
      _ <- checkSubscription[F](user)
      _ <- checkUserStatus[F](user)
    } yield user
  }

  // If we want to, we can wrap our error handling logic around the authentication method.
  // Let's pretend this is an HTTP server for a moment
  final case class HttpResponse(code: Int, body: String)

  def authenticateAndServeResponse[F[_]](implicit
      ME: MonadError[F, Throwable],
      AE: ApplicativeHandle[F, AuthenticationError],
      // `Sync` is the type class that describes the ability to suspend side effects
      // `IO` provides a concrete instance of Sync
      Sync: Sync[F]
  ): F[HttpResponse] =
    authenticate[F]("john.doe", "123456")
      // If the authentication succeeds, we return the "200 OK" status
      .map(user => HttpResponse(code = 200, body = user.toString))
      // Here we can handle business errors if we want to. "handleWith" expects us to produce an [[HttpResponse]],
      // just like the [[authenticate]] method.
      .handleWith[AuthenticationError]({
        case e @ WrongUserName =>
          Sync.delay { /* Do stuff, like logging the error ... */ } as HttpResponse(403, "Wrong username!")
        case e @ WrongPassword =>
          Sync.delay { /* Do stuff, like logging the error ... */ } as HttpResponse(403, "Wrong password!")
        case e: AuthenticationError =>
          Sync.delay(println(s"Another domain error was caught ! ($e)")) as HttpResponse(403, e.toString)
      })
      // Here we can handle technical failures. "recoverWith" expects us to produce an [[HttpResponse]]
      // Since this is a technical, server-side failure, we're going to send a "500 Internal Server Error" status
      // We could also implement a retry logic here
      .recoverWith({
        case e: Throwable =>
          Sync.delay(println("Something went terribly wrong!")) as HttpResponse(
            500,
            "Something went wrong on our side, please retry later"
          )
      })

  // Interpreting the program, using EitherT as our concrete instance for "F"
  override def run(args: List[String]): IO[ExitCode] = {
    type F[A] = EitherT[IO, AuthenticationError, A]
    authenticate[F]("john.doe", "123456").value
      .flatMap({
        case Left(err)     => IO(println(err)).as(ExitCode.Error)
        case Right(result) => IO(println(result)).as(ExitCode.Success)
      })
  }
}
