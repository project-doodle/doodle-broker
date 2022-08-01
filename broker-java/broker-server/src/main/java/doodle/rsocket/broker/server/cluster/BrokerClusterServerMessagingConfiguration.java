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

import doodle.rsocket.broker.server.cluster.rsocket.RSocketBrokerClusterServerAcceptor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.rsocket.RSocketMessagingAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

@SpringBootConfiguration(proxyBeanMethods = false)
@AutoConfigureAfter(RSocketMessagingAutoConfiguration.class)
public class BrokerClusterServerMessagingConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public BrokerClusterServerAcceptor brokerClusterServerAcceptor(
      RSocketMessageHandler messageHandler) {
    return new RSocketBrokerClusterServerAcceptor(messageHandler);
  }
}
