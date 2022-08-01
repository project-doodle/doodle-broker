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

import doodle.rsocket.broker.core.routing.RSocketRoutingRouteId;
import java.net.URI;
import java.util.StringJoiner;

public class BrokerProperties {
  private URI uri;
  private RSocketRoutingRouteId brokerId;

  public URI getUri() {
    return uri;
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  public RSocketRoutingRouteId getBrokerId() {
    return brokerId;
  }

  public void setBrokerId(RSocketRoutingRouteId brokerId) {
    this.brokerId = brokerId;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", BrokerProperties.class.getSimpleName() + "[", "]")
        .add("uri=" + uri)
        .add("brokerId=" + brokerId)
        .toString();
  }
}
