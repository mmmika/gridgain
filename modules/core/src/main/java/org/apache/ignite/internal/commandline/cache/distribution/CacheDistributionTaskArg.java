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
package org.apache.ignite.internal.commandline.cache.distribution;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.internal.visor.VisorDataTransferObject;

/**
 * Input params for CacheDistributionTask
 */
public class CacheDistributionTaskArg extends VisorDataTransferObject {
    /** */
    private static final long serialVersionUID = 0L;

    /** Caches. */
    private Set<String> caches;

    /** Add user attribute in result. */
    private Set<String> userAttrs;

    /**
     * Default constructor.
     */
    public CacheDistributionTaskArg() {
        // No-op.
    }

    /**
     * @param caches Caches.
     * @param userAttrs Add user attribute in result.
     */
    public CacheDistributionTaskArg(Set<String> caches, Set<String> userAttrs) {
        this.caches = caches;
        this.userAttrs = userAttrs;
    }

    /**
     * @return Caches.
     */
    public Set<String> getCaches() {
        return caches;
    }

    /**
     * @return Add user attribute in result
     */
    public Set<String> getUserAttributes() {
        return userAttrs;
    }

    /**
     * @param userAttrs New add user attribute in result
     */
    public void setUserAttributes(Set<String> userAttrs) {
        this.userAttrs = userAttrs;
    }

    /** {@inheritDoc} */
    @Override protected void writeExternalData(ObjectOutput out) throws IOException {
        U.writeCollection(out, caches);
        U.writeCollection(out, userAttrs);
    }

    /** {@inheritDoc} */
    @Override protected void readExternalData(byte protoVer,
        ObjectInput in) throws IOException, ClassNotFoundException {
        caches = U.readSet(in);
        userAttrs = U.readSet(in);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(CacheDistributionTaskArg.class, this);
    }
}
