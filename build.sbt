name := "Redirect"

organization := "com.example"

version := "0.0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.typelevel"           %% "cats-core"       % "1.0.0-RC1",
  "ch.qos.logback"          %  "logback-classic" % "1.2.3",
  "net.databinder.dispatch" %% "dispatch-core"   % "0.13.2",
  "org.scalatest"           %% "scalatest"       % "3.0.1"    % "test",
  "org.scalacheck"          %% "scalacheck"      % "1.13.5"   % "test"
)

scalacOptions ++= Seq(
                      "-unchecked",
                      "-deprecation",
                      "-feature",
                      "-Xfatal-warnings",
                      "-Xlint:_",
                      "-Ywarn-dead-code",
                      "-Ywarn-inaccessible",
                      "-Ywarn-unused-import",
                      "-Ywarn-infer-any",
                      "-Ywarn-nullary-override",
                      "-Ywarn-nullary-unit",
                      "-Ypartial-unification"
                     )

scalacOptions in (Compile, console) ~= (_.filterNot(Seq("-Xfatal-warnings", "-Ywarn-unused-import") contains _))

scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value

initialCommands := "import com.example.redirect._"

