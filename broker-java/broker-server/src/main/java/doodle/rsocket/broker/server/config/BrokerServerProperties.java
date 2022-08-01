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

import static doodle.rsocket.broker.core.routing.RSocketRoutingRouteId.random;
import static doodle.rsocket.broker.server.BrokerServerConstants.PREFIX;

import doodle.rsocket.broker.core.routing.RSocketRoutingRouteId;
import doodle.rsocket.broker.server.cluster.BrokerClusterServerProperties;
import doodle.rsocket.broker.server.proxy.BrokerProxyServerProperties;
import doodle.rsocket.broker.server.routing.BrokerServerRoutingProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(PREFIX)
public class BrokerServerProperties {

  private RSocketRoutingRouteId brokerId = random();

  @NestedConfigurationProperty
  private final BrokerProxyServerProperties proxy = new BrokerProxyServerProperties();

  @NestedConfigurationProperty
  private final BrokerServerRoutingProperties routing = new BrokerServerRoutingProperties();

  @NestedConfigurationProperty
  private final BrokerClusterServerProperties cluster = new BrokerClusterServerProperties();

  public RSocketRoutingRouteId getBrokerId() {
    return brokerId;
  }

  public BrokerServerProperties setBrokerId(RSocketRoutingRouteId brokerId) {
    this.brokerId = brokerId;
    return this;
  }

  public BrokerProxyServerProperties getProxy() {
    return proxy;
  }

  public BrokerServerRoutingProperties getRouting() {
    return routing;
  }

  public BrokerClusterServerProperties getCluster() {
    return cluster;
  }
}
