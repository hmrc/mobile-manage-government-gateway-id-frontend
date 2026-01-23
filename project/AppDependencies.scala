import sbt._

object AppDependencies {

  private val playBootstrapVersion = "10.5.0"
  private val playFrontendVersion  = "12.28.0"
  private val scalaMockVersion     = "7.5.3"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playFrontendVersion,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % playBootstrapVersion
  )

  val test = Seq(
    "uk.gov.hmrc"   %% "bootstrap-test-play-30" % playBootstrapVersion,
    "org.scalamock" %% "scalamock"              % scalaMockVersion
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
