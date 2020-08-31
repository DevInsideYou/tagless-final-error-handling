package devinsideyou
package taglessfinalerrorhandling

import izumi.functional.bio._

object Main4 extends App {
  println("─" * 100)

  type TechnicalError = Throwable

  sealed abstract class BusinessError extends Product with Serializable
  object BusinessError {
    case object Error1 extends BusinessError
    final case class Error2(msg: String) extends BusinessError
  }

  trait Console[F[+_, +_], E] {
    def good(in: Any): F[E, Unit]
    def bad(in: Any): F[E, Unit]
  }

  object Console {
    def dsl[F[+_, +_]: BIO]: Console[F, Nothing] =
      new Console[F, Nothing] {
        import scala.Console._

        override def good(in: Any): F[Nothing, Unit] =
          F.sync(println(GREEN + in + RESET))

        override def bad(in: Any): F[Nothing, Unit] =
          F.sync(err.println(RED + in + RESET))
      }
  }

  trait ErrorProducer[F[+_, +_], E] {
    def goodTechnical: F[E, Int]
    def badTechnical: F[E, Int]
    def goodBusiness: F[E, Int]
    def badBusiness: F[E, Int]
  }

  object ErrorProducer {
    def dsl[F[+_, +_]: BIOError]: ErrorProducer[F, Either[TechnicalError, BusinessError]] =
      new ErrorProducer[F, Either[TechnicalError, BusinessError]] {
        override def goodTechnical: F[Nothing, Int] =
          F.pure(1337)

        override def badTechnical: F[Left[TechnicalError, BusinessError], Int] =
          F.fail(Left(new RuntimeException("db is down")))

        override def goodBusiness: F[Nothing, Int] =
          F.pure(1338)

        override def badBusiness: F[Right[TechnicalError, BusinessError], Int] =
          F.fail(Right(BusinessError.Error2("some business error")))
      }
  }

  trait ErrorHandler[F[+_, +_], E] {
    def goodTechnicalProgram: F[E, Unit]
    def badTechnicalProgram: F[E, Unit]
    def goodBusinessProgram: F[E, Unit]
    def badBusinessProgram: F[E, Unit]
  }

  object ErrorHandler {
    def dsl[F[+_, +_]: BIOMonadError](
        dsl: ErrorProducer[F, Either[TechnicalError, BusinessError]],
        console: Console[F, Nothing]
      ): ErrorHandler[F, Nothing] =
      new ErrorHandler[F, Nothing] {
        override def goodTechnicalProgram: F[Nothing, Unit] =
          dsl
            .goodTechnical
            .flatMap(console.good)
            .catchAll(_.fold(console.bad, console.bad))

        override def badTechnicalProgram: F[Nothing, Unit] =
          dsl
            .badTechnical
            .flatMap(console.good)
            .catchAll(_.fold(console.bad, console.bad))

        override def goodBusinessProgram: F[Nothing, Unit] =
          dsl
            .goodBusiness
            .flatMap(console.good)
            .catchAll(_.fold(console.bad, console.bad))

        override def badBusinessProgram: F[Nothing, Unit] =
          dsl
            .badBusiness
            .flatMap(console.good)
            .catchAll(_.fold(console.bad, console.bad))
      }
  }

  val handler: ErrorHandler[zio.IO, Nothing] =
    ErrorHandler.dsl(ErrorProducer.dsl, Console.dsl)

  import zio.Runtime.{ default => runtime }

  runtime.unsafeRun {
    handler.goodTechnicalProgram
  }

  runtime.unsafeRun {
    handler.badTechnicalProgram
  }

  runtime.unsafeRun {
    handler.goodBusinessProgram
  }

  runtime.unsafeRun {
    handler.badBusinessProgram
  }

  println("─" * 100)
}
