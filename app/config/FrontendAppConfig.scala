/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.RequestHeader

import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

import java.net.URLEncoder

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val host: String = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "mobile-manage-government-gateway-id-frontend"

  def feedbackUrl(implicit request: RequestHeader): String = {

    val encodedUrl = URLEncoder.encode(host + request.uri, "UTF-8")
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${RedirectUrl(encodedUrl)}"
  }

  val basGatewaySignInUrl: String = configuration.get[String]("microservice.services.auth.sign-in.url")
  val loginCallbackUrl: String = configuration.get[String]("microservice.services.auth.login-callback.url")

}
