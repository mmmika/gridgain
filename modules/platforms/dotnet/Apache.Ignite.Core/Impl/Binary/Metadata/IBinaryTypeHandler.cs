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

namespace Apache.Ignite.Core.Impl.Binary.Metadata
{
    using System.Collections.Generic;

    /// <summary>
    /// Binary type metadata handler.
    /// </summary>
    internal interface IBinaryTypeHandler
    {
        /// <summary>
        /// Callback invoked when named field is written.
        /// </summary>
        /// <param name="fieldId">Field ID.</param>
        /// <param name="fieldName">Field name.</param>
        /// <param name="typeId">Field type ID.</param>
        void OnFieldWrite(int fieldId, string fieldName, int typeId);

        /// <summary>
        /// Callback invoked when object write is finished and it is time to collect missing metadata.
        /// </summary>
        /// <returns>Collected metadata.</returns>
        IDictionary<string, BinaryField> OnObjectWriteFinished();
    }
}
