﻿/*
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

namespace Apache.Ignite.Core.Impl.Memory
{
    using System;
    using System.Diagnostics.CodeAnalysis;

    /// <summary>
    /// Abstract memory chunk.
    /// </summary>
    internal abstract class PlatformMemory : IPlatformMemory
    {
        /** Memory pointer. */
        private readonly long _memPtr;

        /// <summary>
        /// Constructor.
        /// </summary>
        /// <param name="memPtr">Memory pointer.</param>
        protected PlatformMemory(long memPtr)
        {
            _memPtr = memPtr;
        }

        /** <inheritdoc /> */
        [SuppressMessage("Microsoft.Reliability", "CA2000:Dispose objects before losing scope")]
        public virtual PlatformMemoryStream GetStream()
        {
            return BitConverter.IsLittleEndian ? new PlatformMemoryStream(this) : 
                new PlatformBigEndianMemoryStream(this);
        }

        /** <inheritdoc /> */
        public long Pointer
        {
            get { return _memPtr; }
        }

        /** <inheritdoc /> */
        public long Data
        {
            get { return PlatformMemoryUtils.GetData(_memPtr); }
        }

        /** <inheritdoc /> */
        public int Capacity
        {
            get { return PlatformMemoryUtils.GetCapacity(_memPtr); }
        }

        /** <inheritdoc /> */
        public int Length
        {
            get { return PlatformMemoryUtils.GetLength(_memPtr); }
            set { PlatformMemoryUtils.SetLength(_memPtr, value); }
        }

        /** <inheritdoc /> */
        public abstract void Reallocate(int cap);

        /** <inheritdoc /> */
        public abstract void Release();
    }
}
