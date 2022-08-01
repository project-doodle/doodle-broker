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

import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingRouteSetupCodec.routeId;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingRouteSetupCodec.serviceName;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingRouteSetupCodec.tags;

import io.netty.buffer.ByteBuf;
import java.util.StringJoiner;

public final class RSocketRoutingRouteSetup extends RSocketRoutingFrame {

  private final RSocketRoutingRouteId routeId;
  private final String serviceName;
  private final RSocketRoutingTags tags;

  public static RSocketRoutingRouteSetupBuilder from(
      RSocketRoutingRouteId routeId, String serviceName) {
    return new RSocketRoutingRouteSetupBuilder(routeId, serviceName);
  }

  public static RSocketRoutingRouteSetup from(ByteBuf byteBuf) {
    return from(routeId(byteBuf), serviceName(byteBuf)).with(tags(byteBuf)).build();
  }

  RSocketRoutingRouteSetup(
      RSocketRoutingRouteId routeId, String serviceName, RSocketRoutingTags tags) {
    super(RSocketRoutingFrameType.ROUTE_SETUP, 0);
    this.routeId = routeId;
    this.serviceName = serviceName;
    this.tags = tags;
  }

  public RSocketRoutingRouteId getRouteId() {
    return routeId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public RSocketRoutingTags getTags() {
    return tags;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RSocketRoutingRouteSetup.class.getSimpleName() + "[", "]")
        .add("routeId=" + routeId)
        .add("tags=" + tags)
        .toString();
  }
}
