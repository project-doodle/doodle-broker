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
package doodle.rsocket.broker.client.rsocket;

import doodle.rsocket.broker.client.config.BrokerClientProperties;
import io.rsocket.loadbalance.LoadbalanceStrategy;
import io.rsocket.loadbalance.LoadbalanceTarget;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import java.net.URI;
import java.util.List;
import java.util.function.Consumer;
import org.reactivestreams.Publisher;
import org.springframework.messaging.rsocket.RSocketConnectorConfigurer;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;

public class BrokerRSocketRequesterBuilder implements RSocketRequester.Builder {

  private final RSocketRequester.Builder delegate;

  private final BrokerClientProperties properties;

  public BrokerRSocketRequesterBuilder(
      RSocketRequester.Builder delegate, BrokerClientProperties properties) {
    this.delegate = delegate;
    this.properties = properties;
  }

  @Override
  public RSocketRequester.Builder dataMimeType(MimeType mimeType) {
    return this.delegate.dataMimeType(mimeType);
  }

  @Override
  public RSocketRequester.Builder metadataMimeType(MimeType mimeType) {
    return this.delegate.metadataMimeType(mimeType);
  }

  @Override
  public RSocketRequester.Builder setupData(Object data) {
    return this.delegate.setupData(data);
  }

  @Override
  public RSocketRequester.Builder setupRoute(String route, Object... routeVars) {
    return this.delegate.setupRoute(route, routeVars);
  }

  @Override
  public RSocketRequester.Builder setupMetadata(Object value, MimeType mimeType) {
    return this.delegate.setupMetadata(value, mimeType);
  }

  @Override
  public RSocketRequester.Builder rsocketStrategies(RSocketStrategies strategies) {
    return this.delegate.rsocketStrategies(strategies);
  }

  @Override
  public RSocketRequester.Builder rsocketStrategies(
      Consumer<RSocketStrategies.Builder> configurer) {
    return this.delegate.rsocketStrategies(configurer);
  }

  @Override
  public RSocketRequester.Builder rsocketConnector(RSocketConnectorConfigurer configurer) {
    return this.delegate.rsocketConnector(configurer);
  }

  @Override
  public RSocketRequester.Builder apply(Consumer<RSocketRequester.Builder> configurer) {
    return this.delegate.apply(configurer);
  }

  @Override
  public BrokerRSocketRequester tcp(String host, int port) {
    return wrap(this.delegate.tcp(host, port));
  }

  @Override
  public BrokerRSocketRequester websocket(URI uri) {
    return wrap(this.delegate.websocket(uri));
  }

  @Override
  public BrokerRSocketRequester transport(ClientTransport transport) {
    return wrap(this.delegate.transport(transport));
  }

  @Override
  public BrokerRSocketRequester transports(
      Publisher<List<LoadbalanceTarget>> targetPublisher, LoadbalanceStrategy loadbalanceStrategy) {
    return wrap(this.delegate.transports(targetPublisher, loadbalanceStrategy));
  }

  @Override
  public Mono<RSocketRequester> connectTcp(String host, int port) {
    return connect(TcpClientTransport.create(host, port));
  }

  @Override
  public Mono<RSocketRequester> connectWebSocket(URI uri) {
    return connect(WebsocketClientTransport.create(uri));
  }

  @Override
  public Mono<RSocketRequester> connect(ClientTransport transport) {
    return this.delegate.connect(transport).map(this::wrap);
  }

  private BrokerRSocketRequester wrap(RSocketRequester rSocketRequester) {
    return new BrokerRSocketRequester(rSocketRequester, properties);
  }
}
