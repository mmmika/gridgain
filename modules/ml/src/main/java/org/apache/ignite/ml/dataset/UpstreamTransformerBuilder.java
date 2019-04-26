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

package org.apache.ignite.ml.dataset;

import java.io.Serializable;
import org.apache.ignite.ml.environment.LearningEnvironment;

/**
 * Builder of {@link UpstreamTransformer}.
 */
@FunctionalInterface
public interface UpstreamTransformerBuilder extends Serializable {
    /**
     * Create {@link UpstreamTransformer} based on learning environment.
     *
     * @param env Learning environment.
     * @return Upstream transformer.
     */
    public UpstreamTransformer build(LearningEnvironment env);

    /**
     * Combunes two builders (this and other respectfully)
     * <pre>
     * env -> transformer1
     * env -> transformer2
     * </pre>
     * into
     * <pre>
     * env -> transformer2 . transformer1
     * </pre>
     *
     * @param other Builder to combine with.
     * @return Compositional builder.
     */
    public default UpstreamTransformerBuilder andThen(UpstreamTransformerBuilder other) {
        UpstreamTransformerBuilder self = this;
        return env -> {
            UpstreamTransformer transformer1 = self.build(env);
            UpstreamTransformer transformer2 = other.build(env);

            return upstream -> transformer2.transform(transformer1.transform(upstream));
        };
    }

    /**
     * Returns identity upstream transformer.
     *
     * @param <K> Type of keys in upstream.
     * @param <V> Type of values in upstream.
     * @return Identity upstream transformer.
     */
    public static <K, V> UpstreamTransformerBuilder identity() {
        return env -> upstream -> upstream;
    }
}
