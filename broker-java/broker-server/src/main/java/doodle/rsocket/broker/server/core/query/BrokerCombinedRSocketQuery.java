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
package doodle.rsocket.broker.server.core.query;

import static doodle.rsocket.broker.core.routing.RSocketRoutingBrokerInfo.from;

import doodle.rsocket.broker.core.routing.*;
import doodle.rsocket.broker.server.routing.rsocket.BrokerRoutingRSocketIndex;
import doodle.rsocket.broker.server.routing.rsocket.BrokerRoutingRSocketTable;
import io.netty.util.concurrent.FastThreadLocal;
import io.rsocket.RSocket;
import java.util.*;
import java.util.function.Function;

public class BrokerCombinedRSocketQuery implements BrokerRSocketQuery {

  private static final FastThreadLocal<List<RSocket>> RSOCKET_STORE;
  private static final FastThreadLocal<Set<RSocketRoutingRouteId>> FOUND_STORE;

  static {
    RSOCKET_STORE =
        new FastThreadLocal<List<RSocket>>() {
          @Override
          protected List<RSocket> initialValue() {
            return new ArrayList<>();
          }
        };

    FOUND_STORE =
        new FastThreadLocal<Set<RSocketRoutingRouteId>>() {
          @Override
          protected Set<RSocketRoutingRouteId> initialValue() {
            return new HashSet<>();
          }
        };
  }

  private final RSocketRoutingRouteId brokerId;
  private final BrokerRoutingRSocketIndex routingIndex;
  private final BrokerRoutingRSocketTable routingTable;
  private final Function<RSocketRoutingBrokerInfo, RSocket> brokerInfoRSocketMapper;

  public BrokerCombinedRSocketQuery(
      RSocketRoutingRouteId brokerId,
      BrokerRoutingRSocketIndex routingIndex,
      BrokerRoutingRSocketTable routingTable,
      Function<RSocketRoutingBrokerInfo, RSocket> brokerInfoRSocketMapper) {
    this.brokerId = brokerId;
    this.routingIndex = routingIndex;
    this.routingTable = routingTable;
    this.brokerInfoRSocketMapper = brokerInfoRSocketMapper;
  }

  @Override
  public List<RSocket> query(RSocketRoutingAddress address) {
    RSocketRoutingTags tags = address.getTags();
    if (Objects.isNull(tags) || tags.isEmpty()) {
      throw new IllegalArgumentException("tags can not be empty!");
    }

    List<RSocket> rSocketStored = RSOCKET_STORE.get();
    rSocketStored.clear();

    List<RSocket> rSockets = routingIndex.query(tags);
    if (Objects.nonNull(rSockets) && !rSockets.isEmpty()) {
      rSocketStored.addAll(rSockets);
    }

    if (!address.getBrokerId().equals(brokerId)) {
      return rSocketStored;
    }

    Set<RSocketRoutingRouteId> foundStore = FOUND_STORE.get();
    foundStore.clear();

    for (RSocketRoutingRouteJoin routeJoin : routingTable.find(tags)) {
      RSocketRoutingRouteId joinedBrokerId = routeJoin.getBrokerId();
      if (!Objects.equals(this.brokerId, joinedBrokerId) && !foundStore.contains(joinedBrokerId)) {
        foundStore.add(joinedBrokerId);

        RSocketRoutingBrokerInfo brokerInfo = from(joinedBrokerId).build();
        rSocketStored.add(brokerInfoRSocketMapper.apply(brokerInfo));
      }
    }

    return rSocketStored;
  }
}
