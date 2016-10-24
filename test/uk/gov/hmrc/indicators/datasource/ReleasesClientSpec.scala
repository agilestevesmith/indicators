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

package uk.gov.hmrc.indicators.datasource

import java.time.{LocalDateTime, ZoneOffset}

import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.{OneAppPerSuite, OneAppPerTest, OneServerPerSuite}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.indicators.{DefaultPatienceConfig, WireMockSpec}

class ReleasesClientSpec extends WordSpec with Matchers with WireMockSpec with ScalaFutures with DefaultPatienceConfig with OneAppPerSuite {

  val releasesClient = new ReleasesClient(endpointMockUrl)

//  implicit override lazy val app = new GuiceApplicationBuilder().build()

  "ReleasesClient.getForService" should {

    "get all releases from the releases api and return ones for the given serviceName" in {
      running(app) {
        val serviceName = "test-service"

        val `release 8.3.0 production date` = LocalDateTime.now().minusMonths(2).toEpochSecond(ZoneOffset.UTC)
        val `release 11.0.0 creation date` = LocalDateTime.now().minusDays(5).toEpochSecond(ZoneOffset.UTC)
        val `release 11.0.0 production date` = LocalDateTime.now().minusDays(4).toEpochSecond(ZoneOffset.UTC)

        givenRequestExpects(
          method = GET,
          url = s"$endpointMockUrl/$serviceName",
          willRespondWith = (200,
            Some(
              s"""
              |[
              |    {
              |        "name": "$serviceName",
              |        "version": "11.0.0",
              |        "creationDate": ${`release 11.0.0 creation date`},
              |        "productionDate": ${`release 11.0.0 production date`},
              |        "leadTime": 5
              |    },
              |    {
              |        "name": "$serviceName",
              |        "version": "8.3.0",
              |        "productionDate": ${`release 8.3.0 production date`},
              |        "leadTime": 20,
              |        "interval": 10
              |    }
              |]
            """.stripMargin
          )))

        val results = releasesClient.getForService(serviceName).futureValue
        results.size shouldBe 2

        results.head shouldBe Release(name = serviceName, version="11.0.0", productionDate = LocalDateTime.ofEpochSecond(`release 11.0.0 production date`, 0, ZoneOffset.UTC), leadTime = Some(5))

        results.last shouldBe Release(name = serviceName, version="8.3.0", productionDate = LocalDateTime.ofEpochSecond(`release 8.3.0 production date`, 0, ZoneOffset.UTC), leadTime = Some(20), interval = Some(10))
      }
    }
  }
}
