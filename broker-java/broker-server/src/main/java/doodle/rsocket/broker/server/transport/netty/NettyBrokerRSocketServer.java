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
package doodle.rsocket.broker.server.transport.netty;

import doodle.rsocket.broker.server.transport.BrokerRSocketServer;
import io.rsocket.transport.netty.server.CloseableChannel;
import java.time.Duration;
import java.util.Objects;
import reactor.core.publisher.Mono;

final class NettyBrokerRSocketServer implements BrokerRSocketServer {

  private final Mono<CloseableChannel> serverStarter;
  private final Duration lifecycleTimeout;
  private CloseableChannel serverChannel;

  NettyBrokerRSocketServer(Mono<CloseableChannel> serverStarter, Duration lifecycleTimeout) {
    this.serverStarter = Objects.requireNonNull(serverStarter);
    this.lifecycleTimeout = lifecycleTimeout;
  }

  @Override
  public void start() {
    this.serverChannel = this.block(this.serverStarter, this.lifecycleTimeout);
    this.startDaemonAwaitThread(this.serverChannel);
  }

  private void startDaemonAwaitThread(CloseableChannel serverChannel) {
    Thread thread =
        new Thread(() -> serverChannel.onClose().block(), this.getClass().getSimpleName());
    thread.setContextClassLoader(getClass().getClassLoader());
    thread.setDaemon(false);
    thread.start();
  }

  @Override
  public void stop() {
    if (this.isRunning()) {
      this.serverChannel.dispose();
      this.serverChannel = null;
    }
  }

  @Override
  public boolean isRunning() {
    return Objects.nonNull(this.serverChannel) && !this.serverChannel.isDisposed();
  }

  private <T> T block(Mono<T> mono, Duration timeout) {
    return Objects.nonNull(timeout) ? mono.block(timeout) : mono.block();
  }
}
