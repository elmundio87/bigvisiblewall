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


import specs.runner.JUnit
import specs.Specification

/**
 * @author Sam Newman (sam.newman@gmail.com)
 */

class ByNameSortingTests extends Specification with JUnit {

  "By Name Sorting for Buld Source" should {

    "Handle an empty list" in {
      val source = new StubBuildSource(List()) with ByNameSorting
      source.get must_== List()
    }

    "Sort builds by name" in {
      val builds = List(
        new Build("B", UNKNOWN, None, ""),
        new Build("A", UNKNOWN, None, ""),
        new Build("C", UNKNOWN, None, "")
        )

      val source = new StubBuildSource(builds) with ByNameSorting

      source.get must containInOrder(List(
        new Build("A", UNKNOWN, None, ""),
        new Build("B", UNKNOWN, None, ""),
        new Build("C", UNKNOWN, None, "")
      ))
    }
    
  }

  class StubBuildSource(val builds: List[Build]) extends BuildSource {
    def get : List[Build] = builds
  }

}