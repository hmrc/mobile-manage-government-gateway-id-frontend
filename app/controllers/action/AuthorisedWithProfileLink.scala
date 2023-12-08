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

package controllers.action

import play.api.Logging
import play.api.mvc.Results.{BadRequest, Forbidden}
import play.api.mvc.{ActionBuilder, ActionRefiner, AnyContent, BodyParser, MessagesControllerComponents, Request, Result, WrappedRequest}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.profile
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, ConfidenceLevel, InsufficientConfidenceLevel}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestWithProfileLink[+A](
  request:         Request[A],
  val profileLink: Option[String])
    extends WrappedRequest[A](request)

trait AuthorisedWithProfileLink
    extends ActionBuilder[RequestWithProfileLink, AnyContent]
    with ActionRefiner[Request, RequestWithProfileLink]

class AuthorisedWithProfileLinkImpl @Inject() (
  af:                            AuthConnector,
  mcc:                           MessagesControllerComponents
)(implicit val executionContext: ExecutionContext)
    extends AuthorisedWithProfileLink
    with Logging {

  val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override protected def refine[A](request: Request[A]): Future[Either[Result, RequestWithProfileLink[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    for {
      auth: Either[Result, Option[String]] <- authenticate()

      response = auth match {
        case Right(Some(profileLink)) => Right(new RequestWithProfileLink(request, Some(profileLink)))
        case Left(result)             => Left(result)
        case _                        => Left(BadRequest("Unexpected response from authentication"))
      }
    } yield response
  }

  def authenticate()(implicit hc: HeaderCarrier): Future[Either[Result, Option[String]]] =
    af.authorise(ConfidenceLevel.L50, profile)
      .map {
        case Some(profile) => {
          if (profile.trim.isEmpty) {
            logger.warn("Profile link not found")
            Left(Forbidden("Profile link not found"))
          } else {
            Right(Some(profile))
          }
        }
        case None =>
          logger.warn("Profile link not found")
          Left(Forbidden("Profile link not found"))
      }
      .recover {
        case e: InsufficientConfidenceLevel =>
          logger.warn(
            "Forbidding access due to insufficient confidence level"
          )
          Left(Forbidden(s"Authorisation failure [${e.reason}]"))
        case e: AuthorisationException => {
          Left(Forbidden(s"Authorisation failure [${e.reason}]"))
        }

      }

}
