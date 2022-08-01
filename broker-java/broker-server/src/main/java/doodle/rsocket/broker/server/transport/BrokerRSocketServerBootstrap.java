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

import io.rsocket.SocketAcceptor;
import java.util.Objects;
import java.util.function.Consumer;
import org.springframework.context.SmartLifecycle;

public class BrokerRSocketServerBootstrap implements SmartLifecycle {

  private final BrokerRSocketServer rSocketServer;
  private final Consumer<BrokerRSocketServer> rSocketServerConsumer;

  public BrokerRSocketServerBootstrap(
      BrokerRSocketServerFactory serverFactory,
      SocketAcceptor socketAcceptor,
      Consumer<BrokerRSocketServer> rSocketServerConsumer) {
    this.rSocketServer = Objects.requireNonNull(serverFactory.createServer(socketAcceptor));
    this.rSocketServerConsumer = rSocketServerConsumer;
  }

  @Override
  public void start() {
    this.rSocketServer.start();
    if (Objects.nonNull(this.rSocketServerConsumer)) {
      this.rSocketServerConsumer.accept(rSocketServer);
    }
  }

  @Override
  public void stop() {
    this.rSocketServer.stop();
  }

  @Override
  public boolean isRunning() {
    return this.rSocketServer.isRunning();
  }
}
