/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.indicators.http

import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import uk.gov.hmrc.indicators.DefaultPatienceConfig


class HttpClientSpec extends WireMockSpec with ScalaFutures with Matchers with DefaultPatienceConfig {

  case class Response(success: Boolean)

  object Response {
    implicit val formats = Json.format[Response]
  }

  def httpClient = new HttpClient

  "HttpClientSpec.get" should {

    "report success with string body" in {

      givenRequestExpects(
        method = GET,
        url = s"$endpointMockUrl/resource/1",
        willRespondWith = (200, Some( """{"success" : true}"""))
      )

      httpClient.get[Response](s"$endpointMockUrl/resource/1").futureValue should be(Response(success = true))

    }

    "report exception if correct http status is not returned" in {

      givenRequestExpects(
        method = GET,
        url = s"$endpointMockUrl/resource/1",
        willRespondWith = (500, None)
      )

      a[RuntimeException] should be thrownBy httpClient.get[Response](s"$endpointMockUrl/resource/1").futureValue
    }


  }
}
