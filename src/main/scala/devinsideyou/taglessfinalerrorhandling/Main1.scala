package devinsideyou
package taglessfinalerrorhandling

import cats._
import cats.syntax.all._

object Main1 extends App {
  println("─" * 100)

  type TechnicalError = Throwable

  sealed abstract class BusinessError extends Product with Serializable
  object BusinessError {
    case object Error1 extends BusinessError
    final case class Error2(msg: String) extends BusinessError
  }

  trait Console[F[_]] {
    def good(in: Any): F[Unit]
    def bad(in: Any): F[Unit]
  }

  object Console {
    def dsl[F[_]: effect.Sync]: Console[F] =
      new Console[F] {
        import scala.Console._

        override def good(in: Any): F[Unit] =
          F.delay(println(GREEN + in + RESET))

        override def bad(in: Any): F[Unit] =
          F.delay(err.println(RED + in + RESET))
      }
  }

  trait ErrorProducer[F[_]] {
    def goodTechnical: F[Int]
    def badTechnical: F[Int]
    def goodBusiness: F[Either[BusinessError, Int]]
    def badBusiness: F[Either[BusinessError, Int]]
  }

  object ErrorProducer {
    def dsl[F[_]: ApplicativeError[*[_], TechnicalError]]: ErrorProducer[F] =
      new ErrorProducer[F] {
        override def goodTechnical: F[Int] =
          F.pure(1337)

        override def badTechnical: F[Int] =
          F.raiseError(new RuntimeException("db is down"))

        override def goodBusiness: F[Either[BusinessError, Int]] =
          F.pure(Right(1338))

        override def badBusiness: F[Either[BusinessError, Int]] =
          F.pure(Left(BusinessError.Error2("some business error")))
      }
  }

  trait ErrorHandler[F[_]] {
    def goodTechnicalProgram: F[Unit]
    def badTechnicalProgram: F[Unit]
    def goodBusinessProgram: F[Unit]
    def badBusinessProgram: F[Unit]
  }

  object ErrorHandler {
    def dsl[F[_]: MonadError[*[_], TechnicalError]](
        dsl: ErrorProducer[F],
        console: Console[F]
      ): ErrorHandler[F] =
      new ErrorHandler[F] {
        override def goodTechnicalProgram: F[Unit] =
          dsl
            .goodTechnical
            .flatMap(console.good)

        override def badTechnicalProgram: F[Unit] =
          dsl
            .badTechnical
            .flatMap(console.good)
            .handleErrorWith(console.bad)

        override def goodBusinessProgram: F[Unit] =
          dsl
            .goodBusiness
            .flatMap(_.fold(console.bad, console.good))

        override def badBusinessProgram: F[Unit] =
          dsl
            .badBusiness
            .flatMap(_.fold(console.bad, console.good))
      }
  }

  val handler: ErrorHandler[effect.IO] =
    ErrorHandler.dsl(ErrorProducer.dsl, Console.dsl)

  handler
    .goodTechnicalProgram
    .unsafeRunSync()

  handler
    .badTechnicalProgram
    .unsafeRunSync()

  handler
    .goodBusinessProgram
    .unsafeRunSync()

  handler
    .badBusinessProgram
    .unsafeRunSync()

  println("─" * 100)
}
