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

/** https://github.com/rsocket/rsocket/blob/feature/rf/Extensions/Routing-And-Forwarding.md */
public enum RSocketRoutingFrameType {
  RESERVED(0x00), // reserved
  ROUTE_SETUP(0x01), // service setup up metadata frame
  ROUTE_JOIN(0x02), // service route join metadata frame
  ROUTE_REMOVE(0x03), // service route remove metadata frame
  BROKER_INFO(0x04), // broker cluster information metadata frame
  ADDRESS(0x05); // routing forward destination metadata frame

  private static RSocketRoutingFrameType[] FRAME_TYPES;

  static {
    FRAME_TYPES = new RSocketRoutingFrameType[values().length];

    for (RSocketRoutingFrameType frameType : values()) {
      FRAME_TYPES[frameType.id] = frameType;
    }
  }

  public static RSocketRoutingFrameType from(int id) {
    return FRAME_TYPES[id];
  }

  private final int id;

  RSocketRoutingFrameType(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
