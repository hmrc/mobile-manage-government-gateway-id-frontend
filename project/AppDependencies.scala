import sbt._

object AppDependencies {

  private val playBootstrapVersion = "9.5.0"
  private val playFrontendVersion  = "11.5.0"
  private val pegdownVersion       = "1.6.0"
  private val scalaMockVersion     = "5.2.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playFrontendVersion,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % playBootstrapVersion
  )

  val test = Seq(
    "uk.gov.hmrc"   %% "bootstrap-test-play-30" % playBootstrapVersion,
    "org.pegdown"   % "pegdown"                 % pegdownVersion,
    "org.scalamock" %% "scalamock"              % scalaMockVersion
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
