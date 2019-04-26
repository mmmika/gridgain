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

package org.apache.ignite.internal.processors.cache.binary;

import java.util.Map;

/**
 * Tests for cache store with binary.
 */
public class GridCacheBinaryStoreObjectsSelfTest extends GridCacheBinaryStoreAbstractSelfTest {
    /** {@inheritDoc} */
    @Override protected boolean keepBinaryInStore() {
        return false;
    }

    /** {@inheritDoc} */
    @Override protected void populateMap(Map<Object, Object> map, int... idxs) {
        assert map != null;
        assert idxs != null;

        for (int idx : idxs)
            map.put(new Key(idx), new Value(idx));
    }

    /** {@inheritDoc} */
    @Override protected void checkMap(Map<Object, Object> map, int... idxs) {
        assert map != null;
        assert idxs != null;

        assertEquals(idxs.length, map.size());

        for (int idx : idxs) {
            Object val = map.get(new Key(idx));

            assertTrue(String.valueOf(val), val instanceof Value);

            assertEquals(idx, ((Value)val).index());
        }
    }
}
