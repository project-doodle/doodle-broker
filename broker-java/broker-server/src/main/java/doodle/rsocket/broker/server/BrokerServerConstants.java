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
package doodle.rsocket.broker.server;

public final class BrokerServerConstants {
  public static final String PREFIX = "doodle.rsocket.broker.server";

  public static final String RSOCKET_PROXY_SERVER_DEFAULT_URI = "tcp://localhost:8001";

  public static final String RSOCKET_CLUSTER_SERVER_DEFAULT_URI = "tcp://localhost:9001";

  public static final String RSOCKET_SERVER_ROUTING_ROUND_ROBIN_LB_STRATEGY = "RoundRobin";

  public static final String RSOCKET_SERVER_ROUTING_DEFAULT_LB_STRATEGY =
      System.getProperty(
          PREFIX + ".routing.loadbalance-strategy", RSOCKET_SERVER_ROUTING_ROUND_ROBIN_LB_STRATEGY);

  public static final String REQUEST_CLUSTER_BROKER_INFO = "cluster.broker-info";

  public static final String REQUEST_CLUSTER_REMOTE_BROKER_INFO = "cluster.remote-broker-info";

  public static final String REQUEST_CLUSTER_ROUTE_JOIN = "cluster.route-join";

  private BrokerServerConstants() {}
}
