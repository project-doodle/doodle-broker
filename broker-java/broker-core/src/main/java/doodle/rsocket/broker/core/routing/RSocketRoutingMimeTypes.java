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
package doodle.rsocket.broker.core.routing;

import io.rsocket.metadata.WellKnownMimeType;
import org.springframework.util.MimeType;

public final class RSocketRoutingMimeTypes {

  public static final String ROUTING_MESSAGE_TYPE = "message";
  public static final String ROUTING_MESSAGE_SUBTYPE = "x.rsocket.routing.frame.v0";

  public static final MimeType ROUTING_FRAME_MIME_TYPE =
      new MimeType(ROUTING_MESSAGE_TYPE, ROUTING_MESSAGE_SUBTYPE);
  public static final String ROUTING_FRAME_METADATA_KEY = "routing-frame";

  public static final MimeType COMPOSITE_MIME_TYPE =
      MimeType.valueOf(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString());

  private RSocketRoutingMimeTypes() {}
}
