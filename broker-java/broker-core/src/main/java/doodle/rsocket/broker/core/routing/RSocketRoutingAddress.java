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

import doodle.rsocket.broker.core.routing.codec.RSocketRoutingAddressCodec;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.StringJoiner;

public class RSocketRoutingAddress extends RSocketRoutingFrame {

  private final RSocketRoutingRouteId originRouteId;
  private final RSocketRoutingRouteId brokerId;
  private final RSocketRoutingTags tags;

  public static RSocketRoutingAddressBuilder from(
      RSocketRoutingRouteId originRouteId, RSocketRoutingRouteId brokerId) {
    return new RSocketRoutingAddressBuilder(originRouteId, brokerId);
  }

  public static RSocketRoutingAddress from(ByteBuf byteBuf, int flags) {
    return from(
            RSocketRoutingAddressCodec.originRouteId(byteBuf),
            RSocketRoutingAddressCodec.brokerId(byteBuf))
        .with(RSocketRoutingAddressCodec.tags(byteBuf))
        .flags(flags)
        .build();
  }

  RSocketRoutingAddress(
      RSocketRoutingRouteId originRouteId,
      RSocketRoutingRouteId brokerId,
      RSocketRoutingTags tags,
      int flags) {
    super(RSocketRoutingFrameType.ADDRESS, flags);
    this.originRouteId = originRouteId;
    this.brokerId = brokerId;
    this.tags = tags;
  }

  public RSocketRoutingType getRoutingType() {
    int routingType = getFlags() & (~RSocketRoutingAddressCodec.ROUTING_TYPE_MASK);
    return RSocketRoutingType.from(routingType);
  }

  public RSocketRoutingRouteId getOriginRouteId() {
    return originRouteId;
  }

  public RSocketRoutingRouteId getBrokerId() {
    return brokerId;
  }

  public RSocketRoutingTags getTags() {
    return tags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RSocketRoutingAddress that = (RSocketRoutingAddress) o;
    return Objects.equals(originRouteId, that.originRouteId)
        && Objects.equals(brokerId, that.brokerId)
        && Objects.equals(tags, that.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(originRouteId, brokerId, tags);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RSocketRoutingAddress.class.getSimpleName() + "[", "]")
        .add("originRouteId=" + originRouteId)
        .add("brokerId=" + brokerId)
        .add("tags=" + tags)
        .toString();
  }
}
