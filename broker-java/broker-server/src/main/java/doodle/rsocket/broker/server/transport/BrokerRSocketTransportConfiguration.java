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
package doodle.rsocket.broker.server.transport;

import doodle.rsocket.broker.server.transport.netty.NettyBrokerRSocketServerFactoryCustomizer;
import doodle.rsocket.broker.server.transport.netty.NettyBrokerRSocketServerTransportFactory;
import io.rsocket.RSocket;
import io.rsocket.transport.netty.server.TcpServerTransport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import reactor.netty.tcp.TcpServer;

@SpringBootConfiguration(proxyBeanMethods = false)
@ConditionalOnClass({RSocket.class, TcpServerTransport.class, TcpServer.class})
public class BrokerRSocketTransportConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public NettyBrokerRSocketServerTransportFactory nettyBrokerRSocketServerTransportFactory(
      ObjectProvider<NettyBrokerRSocketServerFactoryCustomizer> customizers) {
    return new NettyBrokerRSocketServerTransportFactory(customizers);
  }
}
