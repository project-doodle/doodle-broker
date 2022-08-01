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

import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingFrameHeaderCodec.flags;
import static doodle.rsocket.broker.core.routing.codec.RSocketRoutingFrameHeaderCodec.frameType;

import doodle.rsocket.broker.core.routing.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Objects;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RSocketRoutingFrameDecoder extends AbstractDecoder<RSocketRoutingFrame> {

  public RSocketRoutingFrameDecoder() {
    super(RSocketRoutingMimeTypes.ROUTING_FRAME_MIME_TYPE);
  }

  @Override
  public Flux<RSocketRoutingFrame> decode(
      Publisher<DataBuffer> inputStream,
      ResolvableType elementType,
      MimeType mimeType,
      Map<String, Object> hints) {
    return Flux.from(inputStream)
        .flatMap(
            dataBuffer -> {
              RSocketRoutingFrame routingFrame = decode(dataBuffer, elementType, mimeType, hints);
              return Objects.isNull(routingFrame) ? Mono.empty() : Mono.just(routingFrame);
            });
  }

  @Override
  public RSocketRoutingFrame decode(
      DataBuffer buffer, ResolvableType targetType, MimeType mimeType, Map<String, Object> hints)
      throws DecodingException {
    ByteBuf byteBuf = asByteBuf(buffer);
    int flags = flags(byteBuf);
    RSocketRoutingFrameType frameType = frameType(byteBuf);
    switch (frameType) {
      case ROUTE_SETUP:
        return RSocketRoutingRouteSetup.from(byteBuf);
      case ROUTE_JOIN:
        return RSocketRoutingRouteJoin.from(byteBuf);
      case ROUTE_REMOVE:
        return RSocketRoutingRouteRemove.from(byteBuf);
      case BROKER_INFO:
        return RSocketRoutingBrokerInfo.from(byteBuf);
      case ADDRESS:
        return RSocketRoutingAddress.from(byteBuf, flags);
    }
    throw new IllegalArgumentException("Unknown rsocket routing frame type " + frameType);
  }

  private static ByteBuf asByteBuf(DataBuffer buffer) {
    return buffer instanceof NettyDataBuffer
        ? ((NettyDataBuffer) buffer).getNativeBuffer()
        : Unpooled.wrappedBuffer(buffer.asByteBuffer());
  }
}
