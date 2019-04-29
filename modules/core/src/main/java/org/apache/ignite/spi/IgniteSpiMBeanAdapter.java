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

package org.apache.ignite.spi;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;
import org.apache.ignite.internal.util.typedef.X;
import org.apache.ignite.internal.util.typedef.internal.U;

/**
 * This class provides convenient adapter for MBean implementations.
 */
public class IgniteSpiMBeanAdapter implements IgniteSpiManagementMBean {
    /** */
    protected IgniteSpiAdapter spiAdapter;

    /**
     * Constructor
     *
     * @param spiAdapter Spi implementation.
     */
    public IgniteSpiMBeanAdapter(IgniteSpiAdapter spiAdapter) {
        this.spiAdapter = spiAdapter;
    }

    /** {@inheritDoc} */
    @Override public final String getStartTimestampFormatted() {
        return DateFormat.getDateTimeInstance().format(new Date(spiAdapter.getStartTstamp()));
    }

    /** {@inheritDoc} */
    @Override public final String getUpTimeFormatted() {
        return X.timeSpan2DHMSM(getUpTime());
    }

    /** {@inheritDoc} */
    @Override public final long getStartTimestamp() {
        return spiAdapter.getStartTstamp();
    }

    /** {@inheritDoc} */
    @Override public final long getUpTime() {
        final long startTstamp = spiAdapter.getStartTstamp();

        return startTstamp == 0 ? 0 : U.currentTimeMillis() - startTstamp;
    }

    /** {@inheritDoc} */
    @Override public UUID getLocalNodeId() {
        return spiAdapter.ignite.cluster().localNode().id();
    }

    /** {@inheritDoc} */
    @Override public final String getIgniteHome() {
        return spiAdapter.ignite.configuration().getIgniteHome();
    }

    /** {@inheritDoc} */
    @Override public String getName() {
        return spiAdapter.getName();
    }
}
