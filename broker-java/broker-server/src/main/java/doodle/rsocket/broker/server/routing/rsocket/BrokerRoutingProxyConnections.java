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
package doodle.rsocket.broker.server.routing.rsocket;

import doodle.rsocket.broker.core.routing.RSocketRoutingBrokerInfo;
import io.rsocket.RSocket;
import reactor.core.publisher.Mono;

public class BrokerRoutingProxyConnections extends BrokerRoutingConnections<RSocket> {

  @Override
  public RSocket put(RSocketRoutingBrokerInfo brokerInfo, RSocket connection) {
    return super.put(brokerInfo, connection);
  }

  @Override
  protected Mono<RSocket> getRSocket(RSocket rSocket) {
    return Mono.just(rSocket);
  }
}
