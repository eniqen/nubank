import sbt._

object Dependencies {
  type GroupId = String
  type Version = String

  trait Module {
    def groupId: GroupId

    def live: List[ModuleID]

    def withVersion(version: Version): String => ModuleID = groupId %% _ % version

    def withVersionSimple(version: Version): String => ModuleID = groupId % _ % version

    def withTest(module: ModuleID): ModuleID = module % Test
  }

  object Versions {
    val betterMonadicForPlugin = "0.3.1"
    val catsEffect             = "3.1.0"
    val enumeratum             = "1.7.0"
    val circe                  = "0.13.0"
    val fs2                    = "3.0.2"
    val newtype                = "0.4.4"
    val kindProjector          = "0.11.3"

    val scalatest              = "3.1.0"
    val scalamock              = "5.1.0"
  }

  object typelevel extends Module {
    def groupId: GroupId = "org.typelevel"

    val live = List(
      withVersion(Versions.catsEffect)("cats-effect")
    )
  }

  object fs2 extends Module {
    def groupId: GroupId = "co.fs2"

    def live: List[sbt.ModuleID] = List(
      "fs2-core",
      "fs2-io"
    ).map(withVersion(Versions.fs2))
  }

  object circe extends Module {
    def groupId: GroupId = "io.circe"

    def live: List[sbt.ModuleID] = List(
      "circe-generic",
      "circe-core",
      "circe-parser",
      "circe-generic-extras"
    ).map(withVersion(Versions.circe))
  }

  object estatico extends Module {
    def groupId: GroupId = "io.estatico"

    def live: List[sbt.ModuleID] = List("newtype").map(withVersion(Versions.newtype))
  }

  object enumeratum extends Module {
    override def groupId: GroupId = "com.beachape"

    override def live: List[sbt.ModuleID] = List("enumeratum", "enumeratum-circe").map(withVersion(Versions.enumeratum))
  }

  object scalatest extends Module {
    override def groupId: GroupId = "org.scalatest"

    override def live: List[sbt.ModuleID] = List("scalatest").map(withVersion(Versions.scalatest)).map(withTest)
  }

  object scalamock extends Module {
    override def groupId: GroupId = "org.scalamock"

    override def live: List[sbt.ModuleID] = List("scalamock").map(withVersion(Versions.scalamock)).map(withTest)
  }

  val live: List[sbt.ModuleID] =
    List(
      typelevel,
      enumeratum,
      fs2,
      circe,
      estatico,
      scalatest,
      scalamock
    )
      .flatMap(_.live)

  object CompilerPlugins {

    object olegpy extends Module {
      def groupId: GroupId = "com.olegpy"

      def live: List[sbt.ModuleID] = List("better-monadic-for").map(withVersion(Versions.betterMonadicForPlugin))
    }

    object typelevel extends Module {
      def groupId: GroupId = "org.typelevel"

      def live: List[sbt.ModuleID] = List(groupId % "kind-projector" % Versions.kindProjector cross CrossVersion.full)
    }

    val live: List[sbt.ModuleID] = List(olegpy, typelevel).flatMap(_.live)
  }
}
