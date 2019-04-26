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

package org.apache.ignite.internal.processors.hadoop.impl.igfs;

import org.apache.ignite.igfs.IgfsIpcEndpointConfiguration;
import org.apache.ignite.igfs.IgfsIpcEndpointType;

import static org.apache.ignite.igfs.IgfsMode.PRIMARY;
import static org.apache.ignite.internal.util.ipc.shmem.IpcSharedMemoryServerEndpoint.DFLT_IPC_PORT;

/**
 * Tests Hadoop 2.x file system in primary mode.
 */
public class HadoopIgfs20FileSystemShmemPrimarySelfTest extends HadoopIgfs20FileSystemAbstractSelfTest {
    /**
     * Creates test in primary mode.
     */
    public HadoopIgfs20FileSystemShmemPrimarySelfTest() {
        super(PRIMARY);
    }

    /** {@inheritDoc} */
    @Override protected String primaryFileSystemUriPath() {
        return "igfs://igfs@/";
    }

    /** {@inheritDoc} */
    @Override protected String primaryFileSystemConfigPath() {
        return "/modules/core/src/test/config/hadoop/core-site.xml";
    }

    /** {@inheritDoc} */
    @Override protected IgfsIpcEndpointConfiguration primaryIpcEndpointConfiguration(final String igniteInstanceName) {
        IgfsIpcEndpointConfiguration cfg = new IgfsIpcEndpointConfiguration();

        cfg.setType(IgfsIpcEndpointType.SHMEM);
        cfg.setPort(DFLT_IPC_PORT + getTestIgniteInstanceIndex(igniteInstanceName));

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected String secondaryFileSystemUriPath() {
        assert false;

        return null;
    }

    /** {@inheritDoc} */
    @Override protected String secondaryFileSystemConfigPath() {
        assert false;

        return null;
    }

    /** {@inheritDoc} */
    @Override protected IgfsIpcEndpointConfiguration secondaryIpcEndpointConfiguration() {
        assert false;

        return null;
    }
}