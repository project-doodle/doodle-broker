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

import static doodle.rsocket.broker.core.routing.RSocketRoutingFrameType.ROUTE_JOIN;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingRouteJoinCodec.*;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.StringJoiner;

public class RSocketRoutingRouteJoin extends RSocketRoutingFrame {
  private final RSocketRoutingRouteId brokerId;
  private final RSocketRoutingRouteId routeId;
  private final long timestamp;
  private final String serviceName;
  private final RSocketRoutingTags tags;

  public RSocketRoutingRouteJoin(
      RSocketRoutingRouteId brokerId,
      RSocketRoutingRouteId routeId,
      long timestamp,
      String serviceName,
      RSocketRoutingTags tags) {
    super(ROUTE_JOIN, 0);
    this.brokerId = brokerId;
    this.routeId = routeId;
    this.timestamp = timestamp;
    this.serviceName = serviceName;
    this.tags = tags;
  }

  public static RSocketRoutingRouteJoinBuilder builder() {
    return new RSocketRoutingRouteJoinBuilder();
  }

  public static RSocketRoutingRouteJoin from(ByteBuf byteBuf) {
    return builder()
        .brokerId(brokerId(byteBuf))
        .routeId(routeId(byteBuf))
        .timestamp(timestamp(byteBuf))
        .serviceName(serviceName(byteBuf))
        .with(tags(byteBuf))
        .build();
  }

  public RSocketRoutingRouteId getBrokerId() {
    return brokerId;
  }

  public RSocketRoutingRouteId getRouteId() {
    return routeId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getServiceName() {
    return serviceName;
  }

  public RSocketRoutingTags getTags() {
    return tags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RSocketRoutingRouteJoin that = (RSocketRoutingRouteJoin) o;
    return timestamp == that.timestamp
        && Objects.equals(brokerId, that.brokerId)
        && Objects.equals(routeId, that.routeId)
        && Objects.equals(serviceName, that.serviceName)
        && Objects.equals(tags, that.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(brokerId, routeId, timestamp, serviceName, tags);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RSocketRoutingRouteJoin.class.getSimpleName() + "[", "]")
        .add("brokerId=" + brokerId)
        .add("routeId=" + routeId)
        .add("timestamp=" + timestamp)
        .add("serviceName='" + serviceName + "'")
        .add("tags=" + tags)
        .toString();
  }
}
