/*
 * Copyright (c) 2022-present Doodle. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package doodle.rsocket.broker.core.routing;

public class RSocketRoutingRouteJoinBuilder
    extends RSocketRoutingTagsBuilder<RSocketRoutingRouteJoinBuilder> {

  private RSocketRoutingRouteId brokerId;
  private RSocketRoutingRouteId routeId;
  private long timestamp = System.currentTimeMillis();
  private String serviceName;

  public RSocketRoutingRouteJoinBuilder brokerId(RSocketRoutingRouteId brokerId) {
    this.brokerId = brokerId;
    return this;
  }

  public RSocketRoutingRouteJoinBuilder routeId(RSocketRoutingRouteId routeId) {
    this.routeId = routeId;
    return this;
  }

  public RSocketRoutingRouteJoinBuilder timestamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public RSocketRoutingRouteJoinBuilder serviceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public RSocketRoutingRouteJoin build() {
    return new RSocketRoutingRouteJoin(brokerId, routeId, timestamp, serviceName, buildTags());
  }
}
