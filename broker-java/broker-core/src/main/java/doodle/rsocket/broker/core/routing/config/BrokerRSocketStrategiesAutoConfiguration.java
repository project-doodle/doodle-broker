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
package doodle.rsocket.broker.core.routing.config;

import doodle.rsocket.broker.core.routing.codec.RSocketRoutingFrameDecoder;
import doodle.rsocket.broker.core.routing.codec.RSocketRoutingFrameEncoder;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketStrategies;

@SpringBootConfiguration(proxyBeanMethods = false)
@ConditionalOnClass(RSocketStrategies.class)
@AutoConfigureBefore(RSocketStrategiesAutoConfiguration.class)
public class BrokerRSocketStrategiesAutoConfiguration {

  @Bean
  public RSocketStrategiesCustomizer brokerRSocketStrategiesCustomizer() {
    return strategies ->
        strategies
            .decoder(new RSocketRoutingFrameDecoder())
            .encoder(new RSocketRoutingFrameEncoder());
  }
}
