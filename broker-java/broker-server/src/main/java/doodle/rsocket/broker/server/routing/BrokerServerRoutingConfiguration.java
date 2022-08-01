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
package doodle.rsocket.broker.server.routing;

import static doodle.rsocket.broker.core.routing.RSocketRoutingMimeTypes.ROUTING_FRAME_METADATA_KEY;
import static doodle.rsocket.broker.core.routing.RSocketRoutingMimeTypes.ROUTING_FRAME_MIME_TYPE;
import static doodle.rsocket.broker.server.BrokerServerConstants.REQUEST_CLUSTER_REMOTE_BROKER_INFO;
import static doodle.rsocket.broker.server.BrokerServerConstants.RSOCKET_SERVER_ROUTING_ROUND_ROBIN_LB_STRATEGY;

import doodle.rsocket.broker.core.routing.RSocketRoutingBrokerInfo;
import doodle.rsocket.broker.core.routing.RSocketRoutingFrame;
import doodle.rsocket.broker.core.routing.config.BrokerRSocketStrategiesAutoConfiguration;
import doodle.rsocket.broker.server.config.BrokerServerProperties;
import doodle.rsocket.broker.server.core.query.BrokerCombinedRSocketQuery;
import doodle.rsocket.broker.server.core.query.BrokerRSocketQuery;
import doodle.rsocket.broker.server.core.rsocket.BrokerCompositeRSocketLocator;
import doodle.rsocket.broker.server.core.rsocket.BrokerMulticastRSocketLocator;
import doodle.rsocket.broker.server.core.rsocket.BrokerRSocketLocator;
import doodle.rsocket.broker.server.core.rsocket.BrokerUnicastRSocketLocator;
import doodle.rsocket.broker.server.routing.rsocket.*;
import io.rsocket.loadbalance.LoadbalanceStrategy;
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.rsocket.DefaultMetadataExtractor;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Flux;

@SpringBootConfiguration(proxyBeanMethods = false)
@AutoConfigureAfter({
  RSocketStrategiesAutoConfiguration.class,
  BrokerRSocketStrategiesAutoConfiguration.class
})
public class BrokerServerRoutingConfiguration {

  @Autowired
  public BrokerServerRoutingConfiguration(RSocketStrategies rSocketStrategies) {
    Objects.requireNonNull(rSocketStrategies);
    MetadataExtractor metadataExtractor = rSocketStrategies.metadataExtractor();
    if (metadataExtractor instanceof DefaultMetadataExtractor) { // register extractor
      DefaultMetadataExtractor defaultMetadataExtractor =
          (DefaultMetadataExtractor) metadataExtractor;
      defaultMetadataExtractor.metadataToExtract(
          ROUTING_FRAME_MIME_TYPE, RSocketRoutingFrame.class, ROUTING_FRAME_METADATA_KEY);
    }
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerServerRoutingProperties brokerServerRoutingProperties(
      BrokerServerProperties properties) {
    return properties.getRouting();
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerRoutingRSocketIndex brokerRSocketRoutingIndex() {
    return new BrokerRoutingRSocketIndex();
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerRoutingRSocketTable brokerRoutingRSocketTable() {
    return new BrokerRoutingRSocketTable();
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerRoutingProxyConnections brokerRoutingProxyConnections() {
    return new BrokerRoutingProxyConnections();
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerRoutingClusterConnections brokerRoutingClusterConnections(
      BrokerServerProperties properties) {
    BrokerRoutingClusterConnections clusterConnections = new BrokerRoutingClusterConnections();
    clusterConnections
        .joinEvents()
        .flatMap(
            newEntry ->
                Flux.fromIterable(clusterConnections.entries())
                    .filter(
                        existingEntry ->
                            !existingEntry
                                    .getBrokerInfo()
                                    .getBrokerId()
                                    .equals(newEntry.getBrokerInfo().getBrokerId())
                                && !newEntry
                                    .getBrokerInfo()
                                    .getBrokerId()
                                    .equals(properties.getBrokerId()))
                    .flatMap(
                        entry ->
                            entry
                                .getValue()
                                .route(REQUEST_CLUSTER_REMOTE_BROKER_INFO)
                                .data(newEntry.getBrokerInfo())
                                .retrieveMono(RSocketRoutingBrokerInfo.class)))
        .subscribe();
    return clusterConnections;
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerRoutingRouteJoinListener brokerRoutingRouteJoinListener(
      BrokerServerProperties properties,
      BrokerRoutingRSocketTable routingTable,
      BrokerRoutingClusterConnections clusterConnections) {
    return new BrokerRoutingRouteJoinListener(properties, routingTable, clusterConnections);
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerCombinedRSocketQuery brokerCombinedRSocketQuery(
      BrokerServerProperties properties,
      BrokerRoutingRSocketIndex routingIndex,
      BrokerRoutingRSocketTable routingTable,
      BrokerRoutingProxyConnections proxyConnections) {
    return new BrokerCombinedRSocketQuery(
        properties.getBrokerId(), routingIndex, routingTable, proxyConnections::get);
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerMulticastRSocketLocator brokerMulticastRSocketLocator(
      BrokerRSocketQuery rSocketQuery) {
    return new BrokerMulticastRSocketLocator(rSocketQuery);
  }

  @Bean(name = RSOCKET_SERVER_ROUTING_ROUND_ROBIN_LB_STRATEGY)
  @ConditionalOnMissingBean
  public RoundRobinLoadbalanceStrategy roundRobinLoadbalanceStrategy() {
    return new RoundRobinLoadbalanceStrategy();
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerUnicastRSocketLocator brokerUnicastRSocketLocator(
      BrokerServerRoutingProperties properties,
      BrokerRSocketQuery rSocketQuery,
      Map<String, LoadbalanceStrategy> lbStrategies) {
    return new BrokerUnicastRSocketLocator(
        rSocketQuery, lbStrategies, properties.getDefaultLBStrategy());
  }

  @Bean
  @Primary
  public BrokerCompositeRSocketLocator brokerCompositeRSocketLocator(
      ObjectProvider<BrokerRSocketLocator> locators) {
    return new BrokerCompositeRSocketLocator(locators.orderedStream().collect(Collectors.toList()));
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerRoutingAddressExtractor brokerRoutingAddressExtractor(
      RSocketStrategies rSocketStrategies) {
    return new BrokerRoutingAddressExtractor(rSocketStrategies.metadataExtractor());
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerRoutingRSocketFactory brokerRoutingRSocketFactory(
      BrokerRSocketLocator rSocketLocator, BrokerRoutingAddressExtractor addressExtractor) {
    return new BrokerRoutingRSocketFactory(rSocketLocator, addressExtractor);
  }

  @Bean
  @ConditionalOnMissingBean
  public BrokerServerRoutingAcceptor brokerServerRoutingAcceptor(
      BrokerServerProperties properties,
      BrokerRoutingRSocketIndex routingIndex,
      BrokerRoutingRSocketTable routingTable,
      BrokerRoutingProxyConnections proxyConnections,
      BrokerRoutingRSocketFactory routingRSocketFactory,
      RSocketStrategies rSocketStrategies) {
    return new RSocketBrokerServerRoutingAcceptor(
        properties.getBrokerId(),
        routingIndex,
        routingTable,
        proxyConnections::put,
        proxyConnections::get,
        routingRSocketFactory,
        rSocketStrategies.metadataExtractor());
  }
}
