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

import java.net.URI;
import java.util.StringJoiner;

public class BrokerClusterNodeProperties {
  private URI proxy;
  private URI cluster;

  public URI getProxy() {
    return proxy;
  }

  public BrokerClusterNodeProperties setProxy(URI proxy) {
    this.proxy = proxy;
    return this;
  }

  public URI getCluster() {
    return cluster;
  }

  public BrokerClusterNodeProperties setCluster(URI cluster) {
    this.cluster = cluster;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", BrokerClusterNodeProperties.class.getSimpleName() + "[", "]")
        .add("proxy=" + proxy)
        .add("cluster=" + cluster)
        .toString();
  }
}
