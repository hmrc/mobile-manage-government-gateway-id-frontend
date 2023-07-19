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

package controllers

import akka.stream.Materializer
import akka.stream.testkit.NoMaterializer
import base.SpecBase
import controllers.action.{AuthorisedWithProfileLink, RequestWithProfileLink}
import play.api.http.Status
import play.api.mvc.{Action, AnyContent, BodyParser, PlayBodyParsers, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class IndexControllerSpec extends SpecBase {

  "Sign-in" - {

    "redirect to GG sign in page" in {

      val controller = new IndexController(stubMessagesControllerComponents(),
                                           new AlwaysAuthorisedWithProfileLink(""),
                                           "appName",
                                           "gateway_url",
                                           "callback_url")
      val request = FakeRequest(GET, routes.IndexController.signIn.url)
      val result: Future[Result] = controller.signIn.apply(request)
      status(result) mustEqual Status.SEE_OTHER
      await(result.map(_.header.headers.get("Location"))).get mustEqual "gateway_url?continue_url=callback_url&origin=appName"

    }
  }

  "Manage-id" - {

    "redirect to user's profile link" in {

      val controller = new IndexController(stubMessagesControllerComponents(),
        new AlwaysAuthorisedWithProfileLink("www.myprofile.gov/user"),
        "appName",
        "gateway_url",
        "callback_url")
      val request = FakeRequest(GET, routes.IndexController.profile.url)
      val result: Future[Result] = controller.profile.apply(request)
      status(result) mustEqual Status.SEE_OTHER
      await(result.map(_.header.headers.get("Location"))).get mustEqual "www.myprofile.gov/user"

    }
  }
}

class AlwaysAuthorisedWithProfileLink(link: String) extends AuthorisedWithProfileLink {

  override protected def refine[A](request: Request[A]): Future[Either[Result, RequestWithProfileLink[A]]] =
    Future successful Right(new RequestWithProfileLink(request, Some(link)))

  implicit val materializer: Materializer = NoMaterializer

  override def parser:                     BodyParser[AnyContent] = PlayBodyParsers().anyContent
  override protected def executionContext: ExecutionContext       = scala.concurrent.ExecutionContext.Implicits.global
}
