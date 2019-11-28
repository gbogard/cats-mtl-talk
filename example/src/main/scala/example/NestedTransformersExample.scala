package example

import cats.data.{EitherT, ReaderT, StateT}
import cats.effect.IO

object NestedTransformersExample {


  // Retrieves document from a super secure data store
  def getDocument: IO[SecretDocument] = ???

  type Count = Int
  val readSecretDocument: User => EitherT[IO, String, SecretDocument] = {
    val state: StateT[ReaderT[IO, User, *], Count, Either[String, SecretDocument]] =
      StateT[ReaderT[IO, User, *], Int, Either[String, SecretDocument]](currentAttemptsCount =>
        ReaderT[IO, User, (Count, Either[String, SecretDocument])](user =>
          if (currentAttemptsCount >= 3) IO.pure((currentAttemptsCount, Left("Max attemps exceeded")))
          else if (user.isAdmin) getDocument.map(doc => (currentAttemptsCount, Right(doc)))
          else IO.pure((currentAttemptsCount + 1, Left("Access denied")))
        )
      )

    state.run(0).map(_._2).mapF(EitherT(_)).run
  }


}
