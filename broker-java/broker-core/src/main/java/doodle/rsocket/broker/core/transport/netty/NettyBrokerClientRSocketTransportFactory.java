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
package doodle.rsocket.broker.core.transport.netty;

import doodle.rsocket.broker.core.transport.BrokerClientRSocketTransportFactory;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import java.net.URI;

public class NettyBrokerClientRSocketTransportFactory
    implements BrokerClientRSocketTransportFactory {

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
  public ClientTransport create(URI uri) {
    if (isTcp(uri)) {
      return TcpClientTransport.create(uri.getHost(), uri.getPort());
    } else if (isWebSocket(uri)) {
      return WebsocketClientTransport.create(uri);
    }
    throw new IllegalArgumentException("Unknown transport: " + uri);
  }
}
