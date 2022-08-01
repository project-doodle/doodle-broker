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

import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingAddressCodec.FLAGS_U;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingAddressCodec.ROUTING_TYPE_MASK;

import java.util.Objects;

public class RSocketRoutingAddressBuilder
    extends RSocketRoutingTagsBuilder<RSocketRoutingAddressBuilder> {

  // TODO: 2021/7/21 add metadata tags?
  private final RSocketRoutingRouteId originRouteId;
  private final RSocketRoutingRouteId brokerId;
  private int flags = FLAGS_U; // default UNICAST

  RSocketRoutingAddressBuilder(
      RSocketRoutingRouteId originRouteId, RSocketRoutingRouteId brokerId) {
    this.originRouteId = Objects.requireNonNull(originRouteId);
    this.brokerId = Objects.requireNonNull(brokerId);
  }

  public RSocketRoutingAddressBuilder flags(int flags) {
    this.flags = flags;
    return this;
  }

  public RSocketRoutingAddressBuilder routingType(RSocketRoutingType routingType) {
    if (Objects.nonNull(routingType)) {
      flags &= ROUTING_TYPE_MASK;
      flags |= routingType.getFlag();
    }
    return this;
  }

  public RSocketRoutingAddress build() {
    RSocketRoutingTags tags = buildTags();
    if (tags.isEmpty()) {
      throw new IllegalArgumentException("RSocket routing address tags CAN NOT be null");
    }
    return new RSocketRoutingAddress(this.originRouteId, this.brokerId, tags, this.flags);
  }
}
