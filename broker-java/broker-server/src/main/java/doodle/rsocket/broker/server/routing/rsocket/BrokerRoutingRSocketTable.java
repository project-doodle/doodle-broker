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

import doodle.rsocket.broker.core.routing.RSocketRoutingRouteId;
import doodle.rsocket.broker.core.routing.RSocketRoutingRouteJoin;
import doodle.rsocket.broker.core.routing.RSocketRoutingTags;
import doodle.rsocket.broker.server.util.IndexedMap;
import doodle.rsocket.broker.server.util.RoaringBitmapIndexedMap;
import java.util.List;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class BrokerRoutingRSocketTable implements Disposable {

  private static final Logger logger = LoggerFactory.getLogger(BrokerRoutingRSocketTable.class);

  private final IndexedMap<RSocketRoutingRouteId, RSocketRoutingRouteJoin, RSocketRoutingTags>
      indexedMap = new RoaringBitmapIndexedMap<>();

  private final Sinks.Many<RSocketRoutingRouteJoin> joinEvents =
      Sinks.many().multicast().directBestEffort(); // emit element with best effort
  private final Sinks.Many<RSocketRoutingRouteJoin> leaveEvents =
      Sinks.many().multicast().directBestEffort(); // emit element with best effort

  public RSocketRoutingRouteJoin find(RSocketRoutingRouteId routeId) {
    synchronized (indexedMap) {
      return indexedMap.get(routeId);
    }
  }

  public List<RSocketRoutingRouteJoin> find(RSocketRoutingTags tags) {
    synchronized (indexedMap) {
      return indexedMap.query(tags);
    }
  }

  public void add(RSocketRoutingRouteJoin routeJoin) {
    logger.debug("Adding RouteJoin {}", routeJoin);
    synchronized (indexedMap) {
      indexedMap.put(routeJoin.getRouteId(), routeJoin, routeJoin.getTags());
    }
    joinEvents.tryEmitNext(routeJoin);
  }

  public void remove(RSocketRoutingRouteId routeId) {
    logger.debug("Removing routeId {}", routeId);
    synchronized (indexedMap) {
      RSocketRoutingRouteJoin routeJoin = indexedMap.remove(routeId);
      if (routeJoin != null) {
        leaveEvents.tryEmitNext(routeJoin);
      }
    }
  }

  public Flux<RSocketRoutingRouteJoin> joinEvents(Predicate<RSocketRoutingRouteJoin> predicate) {
    return Flux.mergeSequential(Flux.fromIterable(indexedMap.values()), joinEvents.asFlux())
        .filter(predicate);
  }

  public Flux<RSocketRoutingRouteJoin> joinEvents(RSocketRoutingTags tags) {
    return joinEvents(containsTags(tags));
  }

  public Flux<RSocketRoutingRouteJoin> leaveEvents(RSocketRoutingTags tags) {
    return leaveEvents.asFlux().filter(containsTags(tags));
  }

  static Predicate<RSocketRoutingRouteJoin> containsTags(RSocketRoutingTags tags) {
    return event -> event.getTags().entrySet().containsAll(tags.entrySet());
  }

  @Override
  public void dispose() {
    indexedMap.clear();
  }
}
