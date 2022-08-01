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
package doodle.rsocket.broker.server.cluster.rsocket;

import doodle.rsocket.broker.server.cluster.BrokerClusterServerAcceptor;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import java.util.Objects;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import reactor.core.publisher.Mono;

public class RSocketBrokerClusterServerAcceptor implements BrokerClusterServerAcceptor {

  private final RSocketMessageHandler messageHandler;

  public RSocketBrokerClusterServerAcceptor(RSocketMessageHandler messageHandler) {
    this.messageHandler = Objects.requireNonNull(messageHandler);
  }

  @Override
  public Mono<RSocket> accept(ConnectionSetupPayload setupPayload, RSocket rSocket) {
    return this.messageHandler.responder().accept(setupPayload, rSocket);
  }
}
