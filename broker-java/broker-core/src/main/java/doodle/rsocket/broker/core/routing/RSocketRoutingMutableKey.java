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

import static doodle.rsocket.broker.core.routing.RSocketRoutingWellKnownKey.valueOf;

import java.util.Objects;
import java.util.StringJoiner;
import org.springframework.util.StringUtils;

public class RSocketRoutingMutableKey implements RSocketRoutingKey {

  private RSocketRoutingWellKnownKey wellKnownKey;
  private String key;

  public RSocketRoutingMutableKey() {}

  public static RSocketRoutingMutableKey of(RSocketRoutingWellKnownKey wellKnownKey) {
    RSocketRoutingMutableKey mutableKey = new RSocketRoutingMutableKey();
    mutableKey.setWellKnownKey(wellKnownKey);
    return mutableKey;
  }

  public RSocketRoutingMutableKey(String text) {
    if (StringUtils.hasLength(text)) {
      try {
        this.wellKnownKey = valueOf(text.toUpperCase());
      } catch (IllegalArgumentException ignored) {
        this.key = text;
      }
    }
  }

  @Override
  public RSocketRoutingWellKnownKey getWellKnownKey() {
    return this.wellKnownKey;
  }

  public void setWellKnownKey(RSocketRoutingWellKnownKey wellKnownKey) {
    this.wellKnownKey = wellKnownKey;
  }

  @Override
  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RSocketRoutingMutableKey that = (RSocketRoutingMutableKey) o;
    return wellKnownKey == that.wellKnownKey && Objects.equals(key, that.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(wellKnownKey, key);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RSocketRoutingMutableKey.class.getSimpleName() + "[", "]")
        .add("wellKnownKey=" + wellKnownKey)
        .add("key='" + key + "'")
        .toString();
  }
}
