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
package doodle.rsocket.broker.server.core.rsocket;

import doodle.rsocket.broker.core.routing.RSocketRoutingAddress;
import doodle.rsocket.broker.core.routing.RSocketRoutingTags;
import doodle.rsocket.broker.core.routing.RSocketRoutingType;
import doodle.rsocket.broker.core.routing.RSocketRoutingWellKnownKey;
import doodle.rsocket.broker.server.core.query.BrokerRSocketQuery;
import io.rsocket.RSocket;
import io.rsocket.loadbalance.LoadbalanceStrategy;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BrokerUnicastRSocketLocator implements BrokerRSocketLocator {

  private final BrokerRSocketQuery rSocketQuery;
  private final Map<String, LoadbalanceStrategy> lbStrategies;
  private final String defaultLBStrategy;

  // FIXME: 2021/8/8 RSocketAdapter api not implementation exception
  private static final RSocket NoAvailableRSocket = new RSocket() {};

  public BrokerUnicastRSocketLocator(
      BrokerRSocketQuery rSocketQuery,
      Map<String, LoadbalanceStrategy> lbStrategies,
      String defaultLBStrategy) {
    this.rSocketQuery = Objects.requireNonNull(rSocketQuery);
    this.lbStrategies = Objects.requireNonNull(lbStrategies);
    this.defaultLBStrategy = defaultLBStrategy;
  }

  @Override
  public boolean supports(RSocketRoutingType routingType) {
    return routingType == RSocketRoutingType.UNICAST;
  }

  @Override
  public RSocket locate(RSocketRoutingAddress address) {
    List<RSocket> rSockets = rSocketQuery.query(address);
    final int size = rSockets.size();
    switch (size) {
      case 0:
        return NoAvailableRSocket;
      case 1:
        return rSockets.get(0);
      default:
        return loadbalance(rSockets, address.getTags());
    }
  }

  private RSocket loadbalance(List<RSocket> rSockets, RSocketRoutingTags tags) {
    LoadbalanceStrategy lbStrategy = null;
    if (tags.containsKey(RSocketRoutingWellKnownKey.LB_METHOD)) { // check LB_METHOD
      String lbMethod = tags.get(RSocketRoutingWellKnownKey.LB_METHOD);
      if (this.lbStrategies.containsKey(lbMethod)) {
        lbStrategy = this.lbStrategies.get(lbMethod);
      }
    }
    if (Objects.isNull(lbStrategy)) {
      lbStrategy = this.lbStrategies.get(this.defaultLBStrategy);
    }
    return lbStrategy.select(rSockets);
  }
}
