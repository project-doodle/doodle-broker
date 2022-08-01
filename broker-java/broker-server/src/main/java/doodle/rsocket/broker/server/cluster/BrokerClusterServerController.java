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
package doodle.rsocket.broker.server.cluster;

import static doodle.rsocket.broker.core.routing.RSocketRoutingWellKnownKey.BROKER_CLUSTER_URI;
import static doodle.rsocket.broker.core.routing.RSocketRoutingWellKnownKey.BROKER_PROXY_URI;
import static doodle.rsocket.broker.server.BrokerServerConstants.REQUEST_CLUSTER_BROKER_INFO;
import static doodle.rsocket.broker.server.BrokerServerConstants.REQUEST_CLUSTER_REMOTE_BROKER_INFO;
import static doodle.rsocket.broker.server.BrokerServerConstants.REQUEST_CLUSTER_ROUTE_JOIN;

import doodle.rsocket.broker.core.routing.RSocketRoutingBrokerInfo;
import doodle.rsocket.broker.core.routing.RSocketRoutingFrame;
import doodle.rsocket.broker.core.routing.RSocketRoutingRouteJoin;
import doodle.rsocket.broker.server.config.BrokerServerProperties;
import doodle.rsocket.broker.server.routing.rsocket.BrokerRoutingClusterConnections;
import doodle.rsocket.broker.server.routing.rsocket.BrokerRoutingRSocketTable;
import io.rsocket.exceptions.ApplicationErrorException;
import io.rsocket.exceptions.RejectedSetupException;
import java.net.URI;
import java.time.Duration;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Controller
public class BrokerClusterServerController {
  private static final Logger logger = LoggerFactory.getLogger(BrokerClusterServerController.class);
  private final BrokerServerProperties properties;

  private final Consumer<BrokerClusterNodeProperties>
      connectionEventPublisher; // for updating broker info

  private final BrokerRoutingClusterConnections clusterConnections;
  private final BrokerRoutingRSocketTable routingTable;

  private final Sinks.Many<RSocketRoutingBrokerInfo> connectEvents =
      Sinks.many().multicast().directBestEffort(); // emits element with best effort

  public BrokerClusterServerController(
      BrokerServerProperties properties,
      Consumer<BrokerClusterNodeProperties> connectionEventPublisher,
      BrokerRoutingClusterConnections clusterConnections,
      BrokerRoutingRSocketTable routingTable) {
    this.properties = properties;
    this.connectionEventPublisher = connectionEventPublisher;
    this.clusterConnections = clusterConnections;
    this.routingTable = routingTable;

    connectEvents
        .asFlux()
        .delayElements(Duration.ofSeconds(1))
        .flatMap(this::onConnectEvent)
        .subscribe();
  }

  @ConnectMapping
  public Mono<Void> onConnect(RSocketRoutingFrame routingFrame, RSocketRequester rSocketRequester) {
    if (!(routingFrame instanceof RSocketRoutingBrokerInfo)) {
      return Mono.empty();
    }
    RSocketRoutingBrokerInfo brokerInfo = (RSocketRoutingBrokerInfo) routingFrame;
    if (brokerInfo.getBrokerId().equals(properties.getBrokerId())) {
      return Mono.empty();
    }
    logger.info("Received connection from {}", brokerInfo);
    if (clusterConnections.contains(brokerInfo)) {
      return Mono.error(new RejectedSetupException("Duplicate connection from " + brokerInfo));
    }
    clusterConnections.put(brokerInfo, rSocketRequester);
    connectEvents.tryEmitNext(brokerInfo); // emits event when connection established
    return Mono.empty();
  }

  @MessageMapping(REQUEST_CLUSTER_REMOTE_BROKER_INFO)
  public void handleBrokerInfoUpdate(RSocketRoutingBrokerInfo brokerInfo) {
    logger.info("Received remote BrokerInfo {}", brokerInfo);
    BrokerClusterNodeProperties clusterNode = new BrokerClusterNodeProperties();
    clusterNode.setProxy(URI.create(brokerInfo.getTags().get(BROKER_PROXY_URI)));
    clusterNode.setCluster(URI.create(brokerInfo.getTags().get(BROKER_CLUSTER_URI)));
    connectionEventPublisher.accept(clusterNode);
  }

  @MessageMapping(REQUEST_CLUSTER_BROKER_INFO)
  public Mono<RSocketRoutingBrokerInfo> handleBrokerInfo(
      RSocketRoutingBrokerInfo brokerInfo, RSocketRequester rSocketRequester) {
    logger.info("Received BrokerInfo request from: {}", brokerInfo);
    if (clusterConnections.contains(brokerInfo)) {
      logger.debug("Connection for broker already exists {}", brokerInfo);
      return Mono.just(getLocalBrokerInfo());
    }
    clusterConnections.put(brokerInfo, rSocketRequester);
    return sendBrokerInfo(rSocketRequester, brokerInfo);
  }

  @MessageMapping(REQUEST_CLUSTER_ROUTE_JOIN)
  public Mono<RSocketRoutingRouteJoin> handleRouteJoin(RSocketRoutingRouteJoin routeJoin) {
    logger.info("Received RouteJoin {}", routeJoin);
    RSocketRoutingBrokerInfo brokerInfo =
        RSocketRoutingBrokerInfo.from(routeJoin.getBrokerId()).build();
    if (!clusterConnections.contains(brokerInfo)) {
      return Mono.error(new ApplicationErrorException("No connection for broker " + brokerInfo));
    }
    routingTable.add(routeJoin);

    return Mono.just(routeJoin);
  }

  private Mono<RSocketRoutingBrokerInfo> onConnectEvent(RSocketRoutingBrokerInfo brokerInfo) {
    RSocketRequester requester = clusterConnections.get(brokerInfo);
    return sendBrokerInfo(requester, brokerInfo);
  }

  private Mono<RSocketRoutingBrokerInfo> sendBrokerInfo(
      RSocketRequester requester, RSocketRoutingBrokerInfo brokerInfo) {
    RSocketRoutingBrokerInfo localBrokerInfo = getLocalBrokerInfo();
    return requester
        .route(REQUEST_CLUSTER_BROKER_INFO)
        .data(localBrokerInfo)
        .retrieveMono(RSocketRoutingBrokerInfo.class)
        .map(bi -> localBrokerInfo);
  }

  private RSocketRoutingBrokerInfo getLocalBrokerInfo() {
    return RSocketRoutingBrokerInfo.from(properties.getBrokerId())
        .with(BROKER_PROXY_URI, properties.getProxy().getUri().toString())
        .with(BROKER_CLUSTER_URI, properties.getCluster().getUri().toString())
        .build();
  }
}
