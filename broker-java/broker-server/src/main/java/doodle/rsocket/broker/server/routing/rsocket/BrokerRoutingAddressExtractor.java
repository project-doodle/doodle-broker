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

import static doodle.rsocket.broker.core.routing.RSocketRoutingMimeTypes.COMPOSITE_MIME_TYPE;
import static doodle.rsocket.broker.core.routing.RSocketRoutingMimeTypes.ROUTING_FRAME_METADATA_KEY;

import doodle.rsocket.broker.core.routing.RSocketRoutingAddress;
import io.rsocket.Payload;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.messaging.rsocket.MetadataExtractor;

public class BrokerRoutingAddressExtractor implements Function<Payload, RSocketRoutingAddress> {

  private final MetadataExtractor metadataExtractor;

  public BrokerRoutingAddressExtractor(MetadataExtractor metadataExtractor) {
    this.metadataExtractor = Objects.requireNonNull(metadataExtractor);
  }

  @Override
  public RSocketRoutingAddress apply(Payload payload) {
    Map<String, Object> payloadMetadata =
        this.metadataExtractor.extract(payload, COMPOSITE_MIME_TYPE);
    if (payloadMetadata.containsKey(ROUTING_FRAME_METADATA_KEY)) {
      return (RSocketRoutingAddress) payloadMetadata.get(ROUTING_FRAME_METADATA_KEY);
    }
    return null;
  }
}
