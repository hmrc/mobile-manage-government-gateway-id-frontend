/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import base.SpecBase
import controllers.action.AuthorisedWithProfileLinkImpl
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.FORBIDDEN
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, status, stubMessagesControllerComponents}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.profile
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, ConfidenceLevel, InsufficientConfidenceLevel}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class AuthorisedWithProfileLinkSpec extends SpecBase with MockFactory with Results {

  "AuthorisedWithIds" - {
    "include the profile link in the request" in {
      val profileLink       = "www.myprofile.gov"
      val authConnectorStub = authConnectorStubThatWillReturn(Some(profileLink))

      val authorised =
        new AuthorisedWithProfileLinkImpl(authConnectorStub, stubMessagesControllerComponents())

      var capturedLink: Option[String] = None
      val action = authorised { request =>
        capturedLink = request.profileLink
        Ok
      }

      await(action(FakeRequest())) shouldBe Ok
      capturedLink                 shouldBe Some(profileLink)
    }
  }

  "return 403 when no NINO can be retrieved" in {
    val authConnectorStub = authConnectorStubThatWillReturn(None)

    val authorised =
      new AuthorisedWithProfileLinkImpl(authConnectorStub, stubMessagesControllerComponents())

    val action = authorised { _ =>
      Ok
    }

    status(action(FakeRequest())) shouldBe FORBIDDEN
  }

  "return 403 when AuthConnector throws any other AuthorisationException" in {
    val authConnectorStub =
      authConnectorStubThatWillReturn(Future failed new AuthorisationException("not authorised") {})

    val authorised =
      new AuthorisedWithProfileLinkImpl(authConnectorStub, stubMessagesControllerComponents())

    val action = authorised { _ =>
      Ok
    }

    status(action(FakeRequest())) shouldBe FORBIDDEN
  }

  "return 403 Forbidden when AuthConnector throws InsufficientConfidenceLevel" in {
    val authConnectorStub = authConnectorStubThatWillReturn(
      Future failed new InsufficientConfidenceLevel("Insufficient ConfidenceLevel") {}
    )

    val authorised =
      new AuthorisedWithProfileLinkImpl(authConnectorStub, stubMessagesControllerComponents())

    val action = authorised { _ =>
      Ok
    }

    val resultF = action(FakeRequest())
    status(resultF)          shouldBe FORBIDDEN
    contentAsString(resultF) shouldBe "Authorisation failure [Insufficient ConfidenceLevel]"
  }

  private def authConnectorStubThatWillReturn(profileLink: Option[String]): AuthConnector =
    authConnectorStubThatWillReturn(Future successful profileLink)

  private def authConnectorStubThatWillReturn(profileLink: Future[Option[String]]): AuthConnector = {
    val authConnectorStub = stub[AuthConnector]
    (authConnectorStub
      .authorise[Option[String]](_: Predicate, _: Retrieval[Option[String]])(_: HeaderCarrier, _: ExecutionContext))
      .when(ConfidenceLevel.L50, profile, *, *)
      .returns(profileLink)
    authConnectorStub
  }
}
