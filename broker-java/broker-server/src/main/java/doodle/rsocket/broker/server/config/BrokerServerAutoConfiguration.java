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
package doodle.rsocket.broker.server.config;

import doodle.rsocket.broker.server.cluster.BrokerClusterServerConfiguration;
import doodle.rsocket.broker.server.cluster.BrokerClusterServerMessagingConfiguration;
import doodle.rsocket.broker.server.proxy.BrokerProxyServerConfiguration;
import doodle.rsocket.broker.server.routing.BrokerServerRoutingConfiguration;
import doodle.rsocket.broker.server.transport.BrokerRSocketTransportConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration(proxyBeanMethods = false)
@ConditionalOnBean(BrokerServerMarkerConfiguration.Marker.class)
@EnableConfigurationProperties(BrokerServerProperties.class)
@Import({
  BrokerRSocketTransportConfiguration.class,
  BrokerServerRoutingConfiguration.class,
  BrokerProxyServerConfiguration.class,
  BrokerClusterServerMessagingConfiguration.class,
  BrokerClusterServerConfiguration.class
})
public class BrokerServerAutoConfiguration {}
