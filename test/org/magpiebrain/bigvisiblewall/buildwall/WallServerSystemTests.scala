/*
 * Copyright 2009 Sam Newman
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package org.magpiebrain.bigvisiblewall.buildwall


import common.CommonsWebClient
import fixture.FakeHttpServer
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler
import java.net.URLEncoder
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.By
import org.specs.runner.JUnit
import org.specs.Specification
import server.WallServer

/**
 * High-level system tests which fire up a fake source of CCTray
 * data and use WebDriver to confirm markup
 */
class WallServerSystemTests extends Specification with JUnit {

  "Build Wall" should {
     val driver = new HtmlUnitDriver {
        //This seems to be needed to support the refresh header on the page
        //Using this seems to cause the tests to pass but not exit on Intellij, but everything
        //seems happy on the command line
        getWebClient.setRefreshHandler(new ThreadedRefreshHandler)
     }

     val portForCCTraySource = 6789
     val cctraySource = new FakeHttpServer(portForCCTraySource)
     val buildWallServer = new WallServer(new CommonsWebClient, null, 12345, null)

     doFirst {
      cctraySource.start
       buildWallServer.start
     }

    "Display build status" in {
      cctraySource.serveHtmlForUrl("/",
        <Projects>
          <Project name="My Super Project"
            activity="Sleeping" lastBuildStatus="Success" lastBuildLabel="3"
            lastBuildTime="2009-07-27T15:03:33" webUrl="http://mybuildmachine/project" />
        </Projects>
      )
      val cctrayUrl = URLEncoder.encode("http://localhost:6789/", "UTF-8");
      driver.navigate().to("http://localhost:12345/?source=" + cctrayUrl)

      
      val build = driver.findElements(By.className("build")).get(0)
      build.findElement(By.className("project")).getText must equalTo("My Super Project")
    }

    "Display status of more than one cctray feed" in {
      cctraySource.serveHtmlForUrl("/a",
        <Projects>
          <Project name="My Super Project"
            activity="Sleeping" lastBuildStatus="Success" lastBuildLabel="3"
            lastBuildTime="2009-07-27T15:03:33" webUrl="http://mybuildmachine/project" />
        </Projects>
      )

      cctraySource.serveHtmlForUrl("/b",
        <Projects>
          <Project name="My Other Project"
            activity="Sleeping" lastBuildStatus="Success" lastBuildLabel="3"
            lastBuildTime="2009-07-27T15:03:33" webUrl="http://mybuildmachine/project" />
        </Projects>
      )

      val firstUrl = URLEncoder.encode("http://localhost:6789/a", "UTF-8")
      val secondUrl = URLEncoder.encode("http://localhost:6789/b", "UTF-8")

      driver.navigate().to("http://localhost:12345/?source=" + firstUrl + "&source=" + secondUrl)

      val builds = driver.findElements(By.className("project"))
      builds.get(0).getText must equalTo("My Other Project")
      builds.get(1).getText must equalTo("My Super Project")
    }
    
    "Display status of a normal and a cruise cctray feed" in {
      cctraySource.serveHtmlForUrl("/normal",
        <Projects>
          <Project name="My Super Project"
            activity="Sleeping" lastBuildStatus="Success" lastBuildLabel="3"
            lastBuildTime="2009-07-27T15:03:33" webUrl="http://mybuildmachine/project" />
        </Projects>
      )

      cctraySource.serveHtmlForUrl("/cruise",
        <Projects>
          <Project name="My Other Project :: Stage 1"
            activity="Sleeping" lastBuildStatus="Success" lastBuildLabel="3"
            lastBuildTime="2009-07-27T15:03:33" webUrl="http://mybuildmachine/project" />
          <Project name="My Other Project :: Stage 1 :: Step 1"
            activity="Sleeping" lastBuildStatus="Success" lastBuildLabel="3"
            lastBuildTime="2009-07-27T15:03:33" webUrl="http://mybuildmachine/project" />
        </Projects>
      )

      val firstUrl = URLEncoder.encode("http://localhost:6789/normal", "UTF-8")
      val secondUrl = URLEncoder.encode("http://localhost:6789/cruise", "UTF-8")

      driver.navigate().to("http://localhost:12345/?source=" + firstUrl + "&cruiseSource=" + secondUrl)

      val builds = driver.findElements(By.className("project"))
      builds.get(0).getText must equalTo("My Other Project")
      builds.get(1).getText must equalTo("My Other Project :: Stage 1")
      builds.get(2).getText must equalTo("My Other Project :: Stage 1 :: Step 1")
      builds.get(3).getText must equalTo("My Super Project")
    }

    doLast {
      cctraySource.stop
      buildWallServer.stop
    }

  }

}