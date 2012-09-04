import sbt._
import Keys._

object Build extends Build {
  import Dependencies._

  // configure prompt to show current project
  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  lazy val root =
    Project("twirl", file("."))
      .settings(general: _*)
      .settings(noPublishing: _*)
      .aggregate(twirlApi, twirlCompiler, sbtTwirl)

  lazy val twirlApi =
    Project("twirl-api", file("twirl-api"))
      .settings(general: _*)
      .settings(publishing: _*)
      .settings(
        libraryDependencies += commonsLang
      )

  lazy val twirlCompiler =
    Project("twirl-compiler", file("twirl-compiler"))
      .settings(general: _*)
      .settings(publishing: _*)
      .settings(
        libraryDependencies ++= Seq(
          scalaIO,
          Test.specs
        ),
        libraryDependencies <+= scalaVersion(scalaCompiler)
      )
      .dependsOn(twirlApi % "test")

  lazy val sbtTwirl =
    Project("sbt-twirl", file("sbt-twirl"))
      .settings(general: _*)
      .settings(publishing: _*)
      .settings(
        Keys.sbtPlugin := true,
        CrossBuilding.crossSbtVersions := Seq("0.11.3", "0.12")
      )
      .dependsOn(twirlCompiler)


  lazy val general = seq(
    version               := IO.read(file("sbt-twirl/src/main/resources/twirl-version")),
    homepage              := Some(new URL("https://github.com/spray/sbt-twirl")),
    organization          := "cc.spray",
    organizationHomepage  := Some(new URL("http://spray.cc")),
    startYear             := Some(2012),
    licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalaVersion          := "2.9.1",
    scalacOptions         := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    description           := "The Play framework Scala template engine, standalone and packaged as an SBT plugin",
    resolvers             += "typesafe repo"   at "http://repo.typesafe.com/typesafe/releases/"
  )

  lazy val publishing = seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := false,
    publishTo <<= (version) { version: String =>
      val scalasbt = "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-"
      val suffix = if (version.contains("-SNAPSHOT")) "snapshots" else "releases"

      val name = "plugin-" + suffix
      val url  = scalasbt      + suffix

      Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
    }
  )

  lazy val noPublishing = seq(
    publish := (),
    publishLocal := ()
  )
}


