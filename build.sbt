// sbt-scalariform
import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._

// Dependencies
val testDependencies = Seq (
  "org.specs2"                 %% "specs2-core"                   % "3.6.4" % "test"
)

val rootDependencies = Seq(
)

val dependencies = rootDependencies ++ testDependencies


val buildSettings = Seq(
  name := "lslock",
  organization := "eu.stupidsoup",
  scalaVersion := "2.11.7",
  scalaBinaryVersion := "2.11"
)

val compileSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:_",
    "-unchecked",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )
)

val formatting =
  FormattingPreferences()
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, false)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 40)
    .setPreference(CompactControlReadability, false)
    .setPreference(CompactStringConcatenation, false)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(FormatXml, true)
    .setPreference(IndentLocalDefs, false)
    .setPreference(IndentPackageBlocks, true)
    .setPreference(IndentSpaces, 2)
    .setPreference(IndentWithTabs, false)
    .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
    .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
    .setPreference(PreserveSpaceBeforeArguments, false)
    .setPreference(PreserveDanglingCloseParenthesis, true)
    .setPreference(RewriteArrowSymbols, false)
    .setPreference(SpaceBeforeColon, false)
    .setPreference(SpaceInsideBrackets, false)
    .setPreference(SpaceInsideParentheses, false)
    .setPreference(SpacesWithinPatternBinders, true)

val mainProjectRef = LocalProject("main")

val pluginsSettings =
  buildSettings ++
  scalariformSettings

val settings = Seq(
  libraryDependencies ++= dependencies,
  mainClass in (Compile, run) := Option("eu.stupidsoup.lslock.LsLockRunner"),
  ScalariformKeys.preferences := formatting,
  run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))
)

lazy val main =
  project
    .in(file("."))
    .settings(
      pluginsSettings ++
      settings:_*
    )
    .settings(
      compile in Compile <<= (compile in Compile)
    )
