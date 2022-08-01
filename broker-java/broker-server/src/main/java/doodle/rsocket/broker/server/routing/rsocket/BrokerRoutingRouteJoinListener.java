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
package doodle.rsocket.broker.server.routing.rsocket;

import static doodle.rsocket.broker.server.BrokerServerConstants.REQUEST_CLUSTER_ROUTE_JOIN;

import doodle.rsocket.broker.core.routing.RSocketRoutingRouteJoin;
import doodle.rsocket.broker.server.config.BrokerServerProperties;
import doodle.rsocket.broker.server.routing.rsocket.BrokerRoutingConnections.BrokerInfoEntry;
import java.io.Closeable;
import java.io.IOException;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class BrokerRoutingRouteJoinListener implements Closeable {
  private final Logger logger = LoggerFactory.getLogger(BrokerRoutingRouteJoinListener.class);

  private final Disposable disposable;

  public BrokerRoutingRouteJoinListener(
      BrokerServerProperties properties,
      BrokerRoutingRSocketTable routingTable,
      BrokerRoutingClusterConnections clusterConnections) {

    Predicate<RSocketRoutingRouteJoin> SAME_BROKER_ID =
        routeJoin -> properties.getBrokerId().equals(routeJoin.getBrokerId());

    disposable =
        routingTable
            .joinEvents(SAME_BROKER_ID)
            .flatMap(
                routeJoin ->
                    Flux.fromIterable(clusterConnections.entries())
                        .flatMap(entry -> sendRouteJoin(entry, routeJoin)))
            .log()
            .subscribe(this::log);
  }

  private void log(RSocketRoutingRouteJoin routeJoin) {
    System.out.println(routeJoin);
  }

  private Mono<RSocketRoutingRouteJoin> sendRouteJoin(
      BrokerInfoEntry<RSocketRequester> entry, RSocketRoutingRouteJoin routeJoin) {
    logger.info("Sending RouteJoin {} to {}", routeJoin, entry.getBrokerInfo());
    RSocketRequester requester = entry.getValue();
    return requester
        .route(REQUEST_CLUSTER_ROUTE_JOIN)
        .data(routeJoin)
        .retrieveMono(RSocketRoutingRouteJoin.class);
  }

  @Override
  public void close() throws IOException {
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }
}
