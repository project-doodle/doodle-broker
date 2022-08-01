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

import static java.nio.charset.StandardCharsets.UTF_8;

import doodle.rsocket.broker.core.routing.RSocketRoutingRouteId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

public final class RSocketRoutingCodecUtils {

  public static final int ROUTE_ID_BYTES = 16; // MSB(8bytes), LSB(8bytes)
  private static final int UNSIGNED_BYTE_SIZE = 8;
  public static final int UNSIGNED_BYTE_MAX_SIZE = (1 << UNSIGNED_BYTE_SIZE) - 1;

  private RSocketRoutingCodecUtils() {}

  public static void encodeByteString(ByteBuf byteBuf, String str) {
    int length = ByteBufUtil.utf8Bytes(str);
    if (length > UNSIGNED_BYTE_MAX_SIZE) { // check limit size
      throw new IllegalArgumentException(
          String.format("%d is larger then  %d bits", length, UNSIGNED_BYTE_SIZE));
    }
    byteBuf.writeByte(length);
    ByteBufUtil.reserveAndWriteUtf8(byteBuf, str, length);
  }

  public static String decodeByteString(ByteBuf byteBuf, int offset) {
    int length = byteBuf.getByte(offset);
    length &= UNSIGNED_BYTE_MAX_SIZE; // limit 255
    offset += Byte.BYTES;
    return byteBuf.toString(offset, length, UTF_8);
  }

  public static int decodeByteStringLength(ByteBuf byteBuf, int offset) {
    int length = byteBuf.getByte(offset);
    length &= UNSIGNED_BYTE_MAX_SIZE; // limit 255
    return Byte.BYTES + length;
  }

  static void encodeRouteId(ByteBuf byteBuf, RSocketRoutingRouteId routeId) {
    byteBuf.writeLong(routeId.getMsb()).writeLong(routeId.getLsb());
  }

  static RSocketRoutingRouteId decodeRouteId(ByteBuf byteBuf, int offset) {
    long msb = byteBuf.getLong(offset);
    long lsb = byteBuf.getLong(offset + Long.BYTES);
    return new RSocketRoutingRouteId(msb, lsb);
  }
}
