import sbt._

object AppDependencies {

  private val play28Bootstrap     = "8.1.0"
  private val playFrontendVersion = "7.14.0-play-28"
  private val pegdownVersion      = "1.6.0"
  private val scalaMockVersion    = "5.1.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % playFrontendVersion,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % play28Bootstrap
  )

  val test = Seq(
    "uk.gov.hmrc"   %% "bootstrap-test-play-28" % play28Bootstrap,
    "org.pegdown"   % "pegdown"                 % pegdownVersion,
    "org.scalamock" %% "scalamock"              % scalaMockVersion
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
