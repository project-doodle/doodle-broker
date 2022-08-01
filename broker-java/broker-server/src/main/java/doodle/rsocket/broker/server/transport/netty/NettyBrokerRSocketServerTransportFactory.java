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
package doodle.rsocket.broker.server.transport.netty;

import doodle.rsocket.broker.core.transport.BrokerRSocketTransport;
import doodle.rsocket.broker.server.transport.BrokerRSocketServerFactory;
import doodle.rsocket.broker.server.transport.BrokerRSocketServerTransportFactory;
import java.net.URI;
import org.springframework.beans.factory.ObjectProvider;

public class NettyBrokerRSocketServerTransportFactory
    implements BrokerRSocketServerTransportFactory {

  private final ObjectProvider<NettyBrokerRSocketServerFactoryCustomizer> customizers;

  public NettyBrokerRSocketServerTransportFactory(
      ObjectProvider<NettyBrokerRSocketServerFactoryCustomizer> customizers) {
    this.customizers = customizers;
  }

  @Override
  public boolean supports(URI uri) {
    return isTcp(uri) || isWebSocket(uri);
  }

  private boolean isWebSocket(URI uri) {
    return uri.getScheme().equalsIgnoreCase("ws") || uri.getScheme().equalsIgnoreCase("wss");
  }

  private boolean isTcp(URI uri) {
    return uri.getScheme().equalsIgnoreCase("tcp");
  }

  @Override
  public BrokerRSocketServerFactory create(URI uri) {
    NettyBrokerRSocketServerFactory serverFactory = new NettyBrokerRSocketServerFactory();
    serverFactory.setUri(uri);
    serverFactory.setTransport(findRSocketTransport(uri));
    customizers.orderedStream().forEach((customizer) -> customizer.customize(serverFactory));
    return serverFactory;
  }

  private BrokerRSocketTransport findRSocketTransport(URI uri) {
    if (isTcp(uri)) {
      return BrokerRSocketTransport.TCP;
    } else if (isWebSocket(uri)) {
      return BrokerRSocketTransport.WEB_SOCKET;
    }
    throw new IllegalArgumentException("Unknown transport " + uri);
  }
}
