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
import doodle.rsocket.broker.core.routing.RSocketRoutingRouteId;
import io.rsocket.RSocket;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public abstract class BrokerRoutingConnections<CONNECTION> {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected final Sinks.Many<BrokerInfoEntry<CONNECTION>> joinEvents =
      Sinks.many().multicast().directBestEffort();
  protected final Sinks.Many<RSocketRoutingBrokerInfo> leaveEvents =
      Sinks.many().multicast().directBestEffort();

  protected final Map<RSocketRoutingRouteId, BrokerInfoEntry<CONNECTION>> connections =
      new ConcurrentHashMap<>();

  public boolean contains(RSocketRoutingBrokerInfo brokerInfo) {
    return connections.containsKey(brokerInfo.getBrokerId());
  }

  public Collection<BrokerInfoEntry<CONNECTION>> entries() {
    return connections.values();
  }

  public CONNECTION get(RSocketRoutingBrokerInfo brokerInfo) {
    BrokerInfoEntry<CONNECTION> entry = connections.get(brokerInfo.getBrokerId());
    if (Objects.isNull(entry)) {
      return null;
    }
    return entry.value;
  }

  public CONNECTION put(RSocketRoutingBrokerInfo brokerInfo, CONNECTION connection) {
    logger.info("Adding {} RSocket {}", brokerInfo, connection);
    BrokerInfoEntry<CONNECTION> entry = new BrokerInfoEntry<>(connection, brokerInfo);
    BrokerInfoEntry<CONNECTION> old = connections.put(brokerInfo.getBrokerId(), entry);
    if (Objects.isNull(old)) { // new connection
      joinEvents.tryEmitNext(entry);
      registerCleanup(brokerInfo, connection);
      return null;
    }
    return old.value;
  }

  public CONNECTION remove(RSocketRoutingBrokerInfo brokerInfo) {
    BrokerInfoEntry<CONNECTION> removed = connections.remove(brokerInfo.getBrokerId());
    if (removed != null) {
      leaveEvents.tryEmitNext(removed.getBrokerInfo());
      return removed.value;
    }
    return null;
  }

  protected abstract Mono<RSocket> getRSocket(CONNECTION connection);

  protected void registerCleanup(RSocketRoutingBrokerInfo brokerInfo, CONNECTION connection) {
    getRSocket(connection)
        .flatMap(
            rSocket ->
                rSocket
                    .onClose()
                    .doFinally(
                        signal -> {
                          logger.debug("removing connection {}", brokerInfo);
                          connections.remove(brokerInfo.getBrokerId());
                          leaveEvents.tryEmitNext(brokerInfo);
                        }))
        .subscribe();
  }

  public Flux<BrokerInfoEntry<CONNECTION>> joinEvents() {
    return Flux.mergeSequential(Flux.fromIterable(connections.values()), joinEvents.asFlux());
  }

  public Flux<RSocketRoutingBrokerInfo> leaveEvents() {
    return leaveEvents.asFlux();
  }

  public static class BrokerInfoEntry<CONNECTION> {
    final CONNECTION value;
    final RSocketRoutingBrokerInfo brokerInfo;

    public BrokerInfoEntry(CONNECTION value, RSocketRoutingBrokerInfo brokerInfo) {
      this.value = value;
      this.brokerInfo = brokerInfo;
    }

    public CONNECTION getValue() {
      return this.value;
    }

    public RSocketRoutingBrokerInfo getBrokerInfo() {
      return this.brokerInfo;
    }
  }
}
