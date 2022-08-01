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
package doodle.rsocket.broker.server.cluster.rsocket;

import static doodle.rsocket.broker.core.routing.RSocketRoutingMimeTypes.ROUTING_FRAME_MIME_TYPE;
import static doodle.rsocket.broker.core.routing.RSocketRoutingWellKnownKey.BROKER_CLUSTER_URI;
import static doodle.rsocket.broker.core.routing.RSocketRoutingWellKnownKey.BROKER_PROXY_URI;
import static doodle.rsocket.broker.server.BrokerServerConstants.REQUEST_CLUSTER_BROKER_INFO;
import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA;

import doodle.rsocket.broker.core.routing.RSocketRoutingBrokerInfo;
import doodle.rsocket.broker.core.routing.codec.RSocketRoutingBrokerInfoCodec;
import doodle.rsocket.broker.core.transport.BrokerClientRSocketTransportFactory;
import doodle.rsocket.broker.server.cluster.BrokerClusterNodeProperties;
import doodle.rsocket.broker.server.config.BrokerServerProperties;
import doodle.rsocket.broker.server.routing.rsocket.BrokerRoutingClusterConnections;
import doodle.rsocket.broker.server.routing.rsocket.BrokerRoutingProxyConnections;
import doodle.rsocket.broker.server.routing.rsocket.BrokerRoutingRSocketFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.DefaultConnectionSetupPayload;
import io.rsocket.frame.SetupFrameCodec;
import io.rsocket.loadbalance.WeightedStatsRequestInterceptor;
import io.rsocket.transport.ClientTransport;
import io.rsocket.util.DefaultPayload;
import java.net.URI;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class RSocketBrokerClusterManager {
  private static final Logger logger = LoggerFactory.getLogger(RSocketBrokerClusterManager.class);

  private final BrokerServerProperties properties;
  private final BrokerRoutingProxyConnections proxyConnections;
  private final BrokerRoutingClusterConnections clusterConnections;
  private final RSocketMessageHandler messageHandler;
  private final RSocketStrategies strategies;
  private final BrokerRoutingRSocketFactory routingRSocketFactory;
  private final ObjectProvider<BrokerClientRSocketTransportFactory> transportFactories;
  private final RSocketRoutingBrokerInfo localBrokerInfo;

  private RSocket rSocket;

  private final Sinks.Many<BrokerClusterNodeProperties> connectionEvents =
      Sinks.many().multicast().directBestEffort(); // emits element with best effort

  public RSocketBrokerClusterManager(
      BrokerServerProperties properties,
      BrokerRoutingProxyConnections proxyConnections,
      BrokerRoutingClusterConnections clusterConnections,
      RSocketMessageHandler messageHandler,
      RSocketStrategies strategies,
      BrokerRoutingRSocketFactory routingRSocketFactory,
      ObjectProvider<BrokerClientRSocketTransportFactory> transportFactories) {
    this.properties = Objects.requireNonNull(properties);
    this.proxyConnections = Objects.requireNonNull(proxyConnections);
    this.clusterConnections = Objects.requireNonNull(clusterConnections);
    this.messageHandler = Objects.requireNonNull(messageHandler);
    this.strategies = Objects.requireNonNull(strategies);
    this.routingRSocketFactory = Objects.requireNonNull(routingRSocketFactory);
    this.transportFactories = Objects.requireNonNull(transportFactories);

    setupRSocket();

    this.localBrokerInfo = getLocalBrokerInfo();

    connectionEvents.asFlux().map(this::onConnect).subscribe();
  }

  private Disposable onConnect(BrokerClusterNodeProperties clusterNode) {
    logger.info("Starting connect to broker cluster node: {}", clusterNode);
    RSocketRequester requester = connect(clusterNode.getCluster(), localBrokerInfo, null, rSocket);
    return requester
        .route(REQUEST_CLUSTER_BROKER_INFO)
        .data(localBrokerInfo)
        .retrieveMono(RSocketRoutingBrokerInfo.class)
        .map(
            rbi -> { // remote broker info
              clusterConnections.put(rbi, requester);
              return rbi;
            })
        .flatMap(
            rbi -> {
              RSocketRequester requester1 =
                  connect(
                      clusterNode.getProxy(),
                      null,
                      localBrokerInfo,
                      routingRSocketFactory.create());
              return requester1
                  .rsocketClient()
                  .source()
                  .map(
                      rSocket1 -> {
                        proxyConnections.put(rbi, rSocket1);
                        return rSocket1;
                      });
            })
        .subscribe();
  }

  private RSocketRequester connect(
      URI clusterUri, Object setupData, Object setupMetaData, RSocket rSocket) {
    RSocketRequester.Builder builder =
        RSocketRequester.builder()
            .rsocketStrategies(strategies)
            .dataMimeType(ROUTING_FRAME_MIME_TYPE);
    if (Objects.nonNull(setupData)) {
      builder.setupData(setupData);
    }

    if (Objects.nonNull(setupMetaData)) {
      builder.setupMetadata(setupMetaData, ROUTING_FRAME_MIME_TYPE);
    }

    builder.rsocketConnector(
        rSocketConnector ->
            rSocketConnector
                .interceptors(
                    ir ->
                        ir.forRequestsInResponder(
                            requesterRSocket -> {
                              final WeightedStatsRequestInterceptor
                                  weightedStatsRequestInterceptor =
                                      new WeightedStatsRequestInterceptor();
                              return weightedStatsRequestInterceptor;
                            }))
                .acceptor((setup, sendingSocket) -> Mono.just(rSocket)));

    ClientTransport clientTransport =
        transportFactories
            .orderedStream()
            .filter(transportFactory -> transportFactory.supports(clusterUri))
            .findFirst()
            .map(transportFactory -> transportFactory.create(clusterUri))
            .orElseThrow(() -> new IllegalArgumentException("Unknown transport " + properties));

    return builder.transport(clientTransport);
  }

  public Sinks.Many<BrokerClusterNodeProperties> getConnectionEventPublisher() {
    return this.connectionEvents;
  }

  private void setupRSocket() {
    ConnectionSetupPayload setupPayload = getConnectionSetupPayload();
    SocketAcceptor responder = this.messageHandler.responder();
    responder.accept(setupPayload, new RSocket() {}).subscribe(rSocket -> this.rSocket = rSocket);
  }

  private ConnectionSetupPayload getConnectionSetupPayload() {
    DataBufferFactory dataBufferFactory = messageHandler.getRSocketStrategies().dataBufferFactory();
    ByteBufAllocator allocator = ((NettyDataBufferFactory) dataBufferFactory).getByteBufAllocator();
    return getConnectionSetupPayload(allocator, properties);
  }

  private DefaultConnectionSetupPayload getConnectionSetupPayload(
      ByteBufAllocator allocator, BrokerServerProperties properties) {
    RSocketRoutingBrokerInfo brokerInfo = getLocalBrokerInfo();
    ByteBuf encoded =
        RSocketRoutingBrokerInfoCodec.encode(
            allocator,
            brokerInfo.getBrokerId(),
            brokerInfo.getTimestamp(),
            brokerInfo.getTags(),
            0);
    Payload setupPayload = DefaultPayload.create(encoded.retain(), EMPTY_BUFFER);
    ByteBuf setupBuf =
        SetupFrameCodec.encode(
            allocator,
            false,
            1,
            1,
            MESSAGE_RSOCKET_COMPOSITE_METADATA.toString(),
            ROUTING_FRAME_MIME_TYPE.toString(),
            setupPayload);
    return new DefaultConnectionSetupPayload(setupBuf);
  }

  private RSocketRoutingBrokerInfo getLocalBrokerInfo() {
    return RSocketRoutingBrokerInfo.from(properties.getBrokerId())
        .with(BROKER_PROXY_URI, properties.getProxy().getUri().toString())
        .with(BROKER_CLUSTER_URI, properties.getCluster().getUri().toString())
        .build();
  }
}
