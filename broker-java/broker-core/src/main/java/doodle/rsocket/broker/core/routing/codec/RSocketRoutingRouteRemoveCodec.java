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
package doodle.rsocket.broker.core.routing.codec;

import static doodle.rsocket.broker.core.routing.RSocketRoutingFrameType.ROUTE_REMOVE;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingCodecUtils.ROUTE_ID_BYTES;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingCodecUtils.decodeRouteId;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingCodecUtils.encodeRouteId;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingFrameHeaderCodec.BYTES;

import doodle.rsocket.broker.core.routing.RSocketRoutingRouteId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public final class RSocketRoutingRouteRemoveCodec {

  public static ByteBuf encode(
      ByteBufAllocator allocator,
      RSocketRoutingRouteId brokerId,
      RSocketRoutingRouteId routeId,
      long timestamp,
      int flags) {
    ByteBuf byteBuf = RSocketRoutingFrameHeaderCodec.encode(allocator, ROUTE_REMOVE, flags);
    encodeRouteId(byteBuf, brokerId);
    encodeRouteId(byteBuf, routeId);
    byteBuf.writeLong(timestamp);
    return byteBuf;
  }

  public static RSocketRoutingRouteId brokerId(ByteBuf byteBuf) {
    return decodeRouteId(byteBuf, BYTES);
  }

  public static RSocketRoutingRouteId routeId(ByteBuf byteBuf) {
    int offset = BYTES + ROUTE_ID_BYTES;
    return decodeRouteId(byteBuf, offset);
  }

  public static long timestamp(ByteBuf byteBuf) {
    int offset = BYTES + ROUTE_ID_BYTES + ROUTE_ID_BYTES;
    return byteBuf.getLong(offset);
  }

  private RSocketRoutingRouteRemoveCodec() {}
}
