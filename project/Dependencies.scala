import sbt._

object Dependencies {
  case object dev {
    case object zio {
      val zio =
        "dev.zio" %% "zio" % "1.0.3"
    }
  }

  case object io {
    case object `7mind` {
      case object izumi {
        val `fundamentals-bio` =
          "io.7mind.izumi" %% "fundamentals-bio" % "0.10.19"
      }
    }
  }

  case object com {
    case object github {
      case object alexarchambault {
        val `scalacheck-shapeless_1.14` =
          "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.5"
      }
    }

    case object olegpy {
      val `better-monadic-for` =
        "com.olegpy" %% "better-monadic-for" % "0.3.1"
    }
  }

  case object org {
    case object augustjune {
      val `context-applied` =
        "org.augustjune" %% "context-applied" % "0.1.4"
    }

    case object scalacheck {
      val scalacheck =
        "org.scalacheck" %% "scalacheck" % "1.15.1"
    }

    case object scalatest {
      val scalatest =
        "org.scalatest" %% "scalatest" % "3.2.3"
    }

    case object scalatestplus {
      val `scalacheck-1-14` =
        "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0"
    }

    case object typelevel {
      val `cats-core` =
        "org.typelevel" %% "cats-core" % "2.3.0"

      val `cats-effect` =
        "org.typelevel" %% "cats-effect" % "2.2.0"

      val `cats-mtl-core` =
        "org.typelevel" %% "cats-mtl-core" % "0.7.1"

      val `discipline-scalatest` =
        "org.typelevel" %% "discipline-scalatest" % "2.1.0"

      val `kind-projector` =
        "org.typelevel" %% "kind-projector" % "0.11.1" cross CrossVersion.full
    }
  }
}
