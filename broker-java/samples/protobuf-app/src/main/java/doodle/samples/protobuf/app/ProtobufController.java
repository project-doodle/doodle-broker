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
package doodle.samples.protobuf.app;

import static doodle.rsocket.broker.core.routing.RSocketRoutingMimeTypes.ROUTING_FRAME_MIME_TYPE;

import doodle.rsocket.broker.client.config.BrokerClientProperties;
import doodle.rsocket.broker.client.rsocket.BrokerRSocketRequester;
import doodle.rsocket.broker.core.routing.RSocketRoutingAddress;
import doodle.rsocket.broker.core.routing.RSocketRoutingRouteId;
import doodle.sample.SimpleRequest;
import doodle.sample.SimpleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@EnableScheduling
@Controller
@MessageMapping("protobuf")
public class ProtobufController {

  private final BrokerRSocketRequester requester;
  private final RSocketRoutingAddress address;

  @Autowired
  public ProtobufController(BrokerRSocketRequester requester, BrokerClientProperties properties) {
    this.requester = requester;
    address =
        RSocketRoutingAddress.from(RSocketRoutingRouteId.random(), properties.getRouteId())
            .with("instance-name", "protobuf-app")
            .build();
  }

  @MessageMapping("hi")
  public void onHi(String s) {
    System.out.println("hi-ack");
  }

  @MessageMapping("simple")
  public Mono<SimpleResponse> onSimpleReq(SimpleRequest request) {
    return Mono.just(SimpleResponse.newBuilder())
        .map(builder -> builder.setResponseMessage("response"))
        .map(SimpleResponse.Builder::build);
  }

  @Scheduled(initialDelay = 5000, fixedDelay = 5000)
  public void tick() {

    requester
        .route("protobuf.hi")
        .metadata(address, ROUTING_FRAME_MIME_TYPE)
        .data("dd")
        .send()
        .subscribe();

    requester
        .route("protobuf.simple")
        .metadata(address, ROUTING_FRAME_MIME_TYPE)
        .data(SimpleRequest.newBuilder().setRequestMessage("request").build())
        .retrieveMono(SimpleResponse.class)
        .subscribe(System.out::println);
  }
}
