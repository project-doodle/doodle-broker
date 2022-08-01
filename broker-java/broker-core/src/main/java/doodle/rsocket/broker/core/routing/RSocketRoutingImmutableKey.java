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

import java.util.Objects;
import java.util.StringJoiner;

public class RSocketRoutingImmutableKey implements RSocketRoutingKey {

  private final RSocketRoutingWellKnownKey wellKnownKey;
  private final String key;

  public RSocketRoutingImmutableKey(RSocketRoutingWellKnownKey wellKnownKey) {
    this(wellKnownKey, null);
  }

  public RSocketRoutingImmutableKey(String key) {
    this(null, key);
  }

  public RSocketRoutingImmutableKey(RSocketRoutingWellKnownKey wellKnownKey, String key) {
    this.wellKnownKey = wellKnownKey;
    this.key = key;
  }

  @Override
  public RSocketRoutingWellKnownKey getWellKnownKey() {
    return wellKnownKey;
  }

  @Override
  public String getKey() {
    return this.key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RSocketRoutingImmutableKey that = (RSocketRoutingImmutableKey) o;
    return wellKnownKey == that.wellKnownKey && Objects.equals(key, that.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(wellKnownKey, key);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RSocketRoutingImmutableKey.class.getSimpleName() + "[", "]")
        .add("wellKnownKey=" + wellKnownKey)
        .add("key='" + key + "'")
        .toString();
  }
}
