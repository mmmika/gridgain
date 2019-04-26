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

package org.apache.ignite.internal.visor.verify;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.processors.cache.verify.ContentionInfo;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.internal.visor.VisorDataTransferObject;

/**
 *
 */
public class VisorContentionJobResult extends VisorDataTransferObject {
    /** */
    private static final long serialVersionUID = 0L;

    /** Info. */
    private ContentionInfo info;

    /**
     * @param info Info.
     */
    public VisorContentionJobResult(ContentionInfo info) {
        this.info = info;
    }

    /**
     * For externalization only.
     */
    public VisorContentionJobResult() {
    }

    /**
     * @return Contention info.
     */
    public ContentionInfo info() {
        return info;
    }

    /** {@inheritDoc} */
    @Override protected void writeExternalData(ObjectOutput out) throws IOException {
        out.writeObject(info.getNode());
        U.writeCollection(out, info.getEntries());
    }

    /** {@inheritDoc} */
    @Override protected void readExternalData(byte protoVer, ObjectInput in) throws IOException, ClassNotFoundException {
        Object node = in.readObject();
        List<String> entries = U.readList(in);

        info = new ContentionInfo();
        info.setNode((ClusterNode)node);
        info.setEntries(entries);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(VisorContentionJobResult.class, this);
    }
}
