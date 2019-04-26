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

namespace Apache.Ignite.Core.Transactions 
{
    using System;
    using System.Runtime.Serialization;
    using Apache.Ignite.Core.Common;

    /// <summary>
    /// Exception thrown whenever Ignite transactions time out.  
    /// </summary>
    [Serializable]
    public class TransactionTimeoutException : IgniteException
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="TransactionTimeoutException"/> class.
        /// </summary>
        public TransactionTimeoutException()
        {
            // No-op.
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="TransactionTimeoutException"/> class.
        /// </summary>
        /// <param name="message">The message that describes the error.</param>
        public TransactionTimeoutException(string message)
            : base(message)
        {
            // No-op.
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="TransactionTimeoutException"/> class.
        /// </summary>
        /// <param name="info">Serialization information.</param>
        /// <param name="ctx">Streaming context.</param>
        protected TransactionTimeoutException(SerializationInfo info, StreamingContext ctx)
            : base(info, ctx)
        {
            // No-op.
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="TransactionTimeoutException"/> class.
        /// </summary>
        /// <param name="message">The message.</param>
        /// <param name="cause">The cause.</param>
        public TransactionTimeoutException(string message, Exception cause) : base(message, cause)
        {
            // No-op.
        }
    }
}
