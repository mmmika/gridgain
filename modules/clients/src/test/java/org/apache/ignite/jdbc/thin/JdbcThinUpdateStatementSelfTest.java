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

package org.apache.ignite.jdbc.thin;

import java.sql.SQLException;
import java.util.Arrays;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.internal.util.typedef.F;
import org.junit.Test;

/**
 *
 */
public class JdbcThinUpdateStatementSelfTest extends JdbcThinAbstractUpdateStatementSelfTest {
    /**
     * @throws SQLException If failed.
     */
    @Test
    public void testExecute() throws SQLException {
        conn.createStatement().execute("update Person set firstName = 'Jack' where " +
            "cast(substring(_key, 2, 1) as int) % 2 = 0");

        assertEquals(Arrays.asList(F.asList("John"), F.asList("Jack"), F.asList("Mike")),
            jcache(0).query(new SqlFieldsQuery("select firstName from Person order by _key")).getAll());
    }

    /**
     * @throws SQLException If failed.
     */
    @Test
    public void testExecuteUpdate() throws SQLException {
        conn.createStatement().executeUpdate("update Person set firstName = 'Jack' where " +
                "cast(substring(_key, 2, 1) as int) % 2 = 0");

        assertEquals(Arrays.asList(F.asList("John"), F.asList("Jack"), F.asList("Mike")),
                jcache(0).query(new SqlFieldsQuery("select firstName from Person order by _key")).getAll());
    }
}
