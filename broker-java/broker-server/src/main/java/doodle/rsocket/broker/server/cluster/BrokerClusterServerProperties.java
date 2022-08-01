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

import static doodle.rsocket.broker.server.BrokerServerConstants.RSOCKET_CLUSTER_SERVER_DEFAULT_URI;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

public class BrokerClusterServerProperties {

  private boolean enabled = true;

  private URI uri = URI.create(RSOCKET_CLUSTER_SERVER_DEFAULT_URI);

  @NestedConfigurationProperty
  private final List<BrokerClusterNodeProperties> nodes = new ArrayList<>();

  public URI getUri() {
    return uri;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  public List<BrokerClusterNodeProperties> getNodes() {
    return nodes;
  }
}
