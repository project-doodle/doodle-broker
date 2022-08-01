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

import static doodle.rsocket.broker.client.BrokerClientConstants.*;
import static doodle.rsocket.broker.core.routing.RSocketRoutingRouteId.random;

import doodle.rsocket.broker.core.routing.RSocketRoutingMutableKey;
import doodle.rsocket.broker.core.routing.RSocketRoutingRouteId;
import java.util.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.MimeType;

@ConfigurationProperties(PREFIX)
public class BrokerClientProperties {

  private RSocketRoutingRouteId routeId = random();

  private String serviceName;

  private final Map<RSocketRoutingMutableKey, String> tags = new LinkedHashMap<>();

  private MimeType dataMimeType;

  @NestedConfigurationProperty private BrokerProperties broker = new BrokerProperties();

  public RSocketRoutingRouteId getRouteId() {
    return routeId;
  }

  public void setRouteId(RSocketRoutingRouteId routeId) {
    this.routeId = routeId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public Map<RSocketRoutingMutableKey, String> getTags() {
    return tags;
  }

  public MimeType getDataMimeType() {
    return dataMimeType;
  }

  public void setDataMimeType(MimeType dataMimeType) {
    this.dataMimeType = dataMimeType;
  }

  public BrokerProperties getBroker() {
    return broker;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", BrokerClientProperties.class.getSimpleName() + "[", "]")
        .add("routeId=" + routeId)
        .add("serviceName='" + serviceName + "'")
        .add("tags=" + tags)
        .add("dataMimeType=" + dataMimeType)
        .add("broker=" + broker)
        .toString();
  }
}
