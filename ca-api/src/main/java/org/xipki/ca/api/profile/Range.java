/*
 *
 * Copyright (c) 2013 - 2018 Lijun Liao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xipki.ca.api.profile;

import org.xipki.util.StringUtil;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

public class Range {

  private Integer min;

  private Integer max;

  public Range(Integer min, Integer max) {
    setRange(min, max);
  }

  public Integer getMin() {
    return min;
  }

  public Integer getMax() {
    return max;
  }

  public void setRange(Integer min, Integer max) {
    if (min == null && max == null) {
      throw new IllegalArgumentException("min and max must not be both null");
    }
    if (min != null && max != null && min > max) {
      throw new IllegalArgumentException(String.format(
          "min must not be greater than max: %d > %d", min, max));
    }
    this.min = min;
    this.max = max;
  }

  public boolean match(int val) {
    if (min != null && val < min) {
      return false;
    }
    if (max != null && val > max) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return StringUtil.concatObjects("[", (min == null ? "" : min), ",",
        (max == null ? "" : max), "]");
  }

}
