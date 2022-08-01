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
package doodle.rsocket.broker.server.cluster;

import doodle.rsocket.broker.server.config.BrokerServerProperties;
import java.util.Objects;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import reactor.core.publisher.Sinks;

public class BrokerClusterBootstrapListener implements ApplicationListener<ApplicationReadyEvent> {

  private final BrokerServerProperties properties;
  private final Sinks.Many<BrokerClusterNodeProperties> connectionEventPublisher;

  public BrokerClusterBootstrapListener(
      BrokerServerProperties properties,
      Sinks.Many<BrokerClusterNodeProperties> connectionEventPublisher) {
    this.properties = Objects.requireNonNull(properties);
    this.connectionEventPublisher = Objects.requireNonNull(connectionEventPublisher);
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    // auto connect to broker list in bootstrap configuration
    properties.getCluster().getNodes().forEach(connectionEventPublisher::tryEmitNext);
  }
}
