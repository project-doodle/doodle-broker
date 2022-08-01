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

import static doodle.rsocket.broker.core.routing.RSocketRoutingFrameType.ROUTE_REMOVE;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingRouteRemoveCodec.brokerId;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingRouteRemoveCodec.routeId;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingRouteRemoveCodec.timestamp;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.StringJoiner;

public class RSocketRoutingRouteRemove extends RSocketRoutingFrame {
  private final RSocketRoutingRouteId brokerId;
  private final RSocketRoutingRouteId routeId;
  private long timestamp;

  public static RSocketRoutingRouteRemoveBuilder builder() {
    return new RSocketRoutingRouteRemoveBuilder();
  }

  public static RSocketRoutingRouteRemove from(ByteBuf byteBuf) {
    return builder()
        .brokerId(brokerId(byteBuf))
        .routeId(routeId(byteBuf))
        .timestamp(timestamp(byteBuf))
        .build();
  }

  public RSocketRoutingRouteRemove(
      RSocketRoutingRouteId brokerId, RSocketRoutingRouteId routeId, long timestamp) {
    super(ROUTE_REMOVE, 0);
    this.brokerId = brokerId;
    this.routeId = routeId;
    this.timestamp = timestamp;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RSocketRoutingRouteRemove that = (RSocketRoutingRouteRemove) o;
    return timestamp == that.timestamp
        && Objects.equals(brokerId, that.brokerId)
        && Objects.equals(routeId, that.routeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(brokerId, routeId, timestamp);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RSocketRoutingRouteRemove.class.getSimpleName() + "[", "]")
        .add("brokerId=" + brokerId)
        .add("routeId=" + routeId)
        .add("timestamp=" + timestamp)
        .toString();
  }
}
