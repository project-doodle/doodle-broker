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
package doodle.rsocket.broker.client.rsocket;

import doodle.rsocket.broker.client.config.BrokerClientProperties;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketClient;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeType;

public class BrokerRSocketRequester implements RSocketRequester {

  private final RSocketRequester delegate;
  private final BrokerClientProperties properties;

  public BrokerRSocketRequester(RSocketRequester delegate, BrokerClientProperties properties) {
    this.delegate = delegate;
    this.properties = properties;
  }

  @Override
  public RSocketClient rsocketClient() {
    return this.delegate.rsocketClient();
  }

  @Override
  public RSocket rsocket() {
    return this.delegate.rsocket();
  }

  @Override
  public MimeType dataMimeType() {
    return this.delegate.dataMimeType();
  }

  @Override
  public MimeType metadataMimeType() {
    return this.delegate.metadataMimeType();
  }

  @Override
  public RequestSpec route(String route, Object... routeVars) {
    return this.delegate.route(route, routeVars);
  }

  @Override
  public RequestSpec metadata(Object metadata, MimeType mimeType) {
    return this.delegate.metadata(metadata, mimeType);
  }
}
