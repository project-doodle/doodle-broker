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
package doodle.rsocket.broker.client.config;

import static doodle.rsocket.broker.client.BrokerClientConstants.PREFIX;
import static doodle.rsocket.broker.core.routing.RSocketRoutingMimeTypes.ROUTING_FRAME_MIME_TYPE;

import doodle.rsocket.broker.client.rsocket.BrokerRSocketRequester;
import doodle.rsocket.broker.client.rsocket.BrokerRSocketRequesterBuilder;
import doodle.rsocket.broker.core.routing.RSocketRoutingRouteSetup;
import doodle.rsocket.broker.core.routing.RSocketRoutingRouteSetupBuilder;
import doodle.rsocket.broker.core.routing.RSocketRoutingSetupBuilderCustomizer;
import doodle.rsocket.broker.core.routing.config.BrokerRSocketStrategiesAutoConfiguration;
import doodle.rsocket.broker.core.transport.BrokerClientRSocketTransportFactory;
import doodle.rsocket.broker.core.transport.netty.NettyBrokerClientRSocketTransportFactory;
import io.rsocket.RSocket;
import io.rsocket.transport.ClientTransport;
import java.time.Duration;
import java.util.Objects;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.rsocket.RSocketConnectorConfigurer;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.MimeTypeUtils;
import reactor.util.retry.Retry;

@SpringBootConfiguration(proxyBeanMethods = false)
@ConditionalOnBean(BrokerClientMarkerConfiguration.Marker.class)
@ConditionalOnClass({RSocketRequester.class, RSocket.class, RSocketStrategies.class})
@EnableConfigurationProperties(BrokerClientProperties.class)
@AutoConfigureAfter({
  RSocketRequesterAutoConfiguration.class,
  BrokerRSocketStrategiesAutoConfiguration.class
})
public class BrokerClientAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public NettyBrokerClientRSocketTransportFactory nettyBrokerClientRSocketTransportFactory() {
    return new NettyBrokerClientRSocketTransportFactory();
  }

  @Bean
  @ConditionalOnMissingBean
  public RSocketConnectorConfigurer rSocketConnectorConfigurer(
      RSocketMessageHandler messageHandler) { // spring messaging bridge
    return (connector) ->
        connector
            .acceptor(messageHandler.responder())
            .reconnect(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)));
  }

  @Bean
  @Scope("prototype")
  @ConditionalOnMissingBean
  public BrokerRSocketRequesterBuilder brokerRSocketRequesterBuilder(
      BrokerClientProperties properties,
      RSocketConnectorConfigurer configurer,
      RSocketStrategies strategies,
      ObjectProvider<RSocketRoutingSetupBuilderCustomizer> customizers) {

    RSocketRoutingRouteSetupBuilder routeSetup =
        RSocketRoutingRouteSetup.from(properties.getRouteId(), properties.getServiceName());

    properties
        .getTags()
        .forEach(
            (key, value) -> {
              if (Objects.nonNull(key.getWellKnownKey())) {
                routeSetup.with(key.getWellKnownKey(), value);
              } else if (Objects.nonNull(key.getKey())) {
                routeSetup.with(key.getKey(), value);
              }
            });

    customizers.orderedStream().forEach((customizer) -> customizer.customize(routeSetup));

    RSocketRequester.Builder builder =
        RSocketRequester.builder()
            .setupMetadata(routeSetup.build(), ROUTING_FRAME_MIME_TYPE)
            .dataMimeType(
                Objects.nonNull(properties.getDataMimeType())
                    ? properties.getDataMimeType()
                    : MimeTypeUtils.APPLICATION_JSON)
            .rsocketStrategies(strategies)
            .rsocketConnector(configurer);

    return new BrokerRSocketRequesterBuilder(builder, properties);
  }

  @Bean
  @ConditionalOnProperty(prefix = PREFIX, name = "auto-connect", matchIfMissing = true)
  public BrokerRSocketRequester brokerRSocketRequester(
      BrokerRSocketRequesterBuilder builder,
      BrokerClientProperties properties,
      ObjectProvider<BrokerClientRSocketTransportFactory> transportFactories) {

    BrokerProperties broker = properties.getBroker();

    if (Objects.isNull(broker.getUri())) {
      throw new IllegalStateException(PREFIX + ".broker uri can not be null!");
    }

    if (Objects.isNull(broker.getBrokerId())) {
      throw new IllegalStateException(PREFIX + ".broker id can not be null!");
    }

    ClientTransport clientTransport =
        transportFactories
            .orderedStream()
            .filter(transportFactory -> transportFactory.supports(broker.getUri()))
            .findFirst()
            .map(transportFactory -> transportFactory.create(broker.getUri()))
            .orElseThrow(() -> new IllegalStateException("Unknown transport: " + broker));

    BrokerRSocketRequester requester = builder.transport(clientTransport);

    requester.rsocketClient().source().subscribe(); // connect

    return requester;
  }
}
