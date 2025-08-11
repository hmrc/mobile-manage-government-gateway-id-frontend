import sbt._

object AppDependencies {

  private val playBootstrapVersion = "10.1.0"
  private val playFrontendVersion  = "12.8.0"
  private val scalaMockVersion     = "7.4.0"

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
