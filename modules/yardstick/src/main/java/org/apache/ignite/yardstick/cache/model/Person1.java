/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.yardstick.cache.model;

import java.io.Serializable;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * Value used for indexed put test.
 */
public class Person1 implements Serializable {
    /** Value. */
    @QuerySqlField(index = true)
    private int val1;

    /**
     * Constructs.
     *
     * @param val Indexed value.
     */
    public Person1(int val) {
        this.val1 = val;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        return this == o || (o instanceof Person1) && val1 == ((Person1)o).val1;
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return val1;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return "Person1 [val1=" + val1 + ']';
    }
}