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

namespace Apache.Ignite.Core.Impl.Compute
{
    using System;
    using System.Diagnostics;
    using System.Diagnostics.CodeAnalysis;
    using Apache.Ignite.Core.Binary;
    using Apache.Ignite.Core.Common;
    using Apache.Ignite.Core.Impl.Binary;
    using Apache.Ignite.Core.Impl.Binary.IO;
    using Apache.Ignite.Core.Impl.Compute.Closure;
    using Apache.Ignite.Core.Impl.Deployment;
    using Apache.Ignite.Core.Impl.Memory;
    using Apache.Ignite.Core.Impl.Resource;

    /// <summary>
    /// Holder for user-provided compute job.
    /// </summary>
    internal class ComputeJobHolder : IBinaryWriteAware
    {
        /** Actual job. */
        private readonly IComputeJob _job;
        
        /** Owning grid. */
        private readonly IIgniteInternal _ignite;

        /** Result (set for local jobs only). */
        private volatile ComputeJobResultImpl _jobRes;

        /// <summary>
        /// Default ctor for marshalling.
        /// </summary>
        /// <param name="reader"></param>
        public ComputeJobHolder(BinaryReader reader)
        {
            Debug.Assert(reader != null);

            _ignite = reader.Marshaller.Ignite;

            _job = reader.ReadObject<IComputeJob>();
        }

        /// <summary>
        /// Constructor.
        /// </summary>
        /// <param name="grid">Grid.</param>
        /// <param name="job">Job.</param>
        public ComputeJobHolder(IIgniteInternal grid, IComputeJob job)
        {
            Debug.Assert(grid != null);
            Debug.Assert(job != null);

            _ignite = grid;
            _job = job;
        }

        /// <summary>
        /// Executes local job.
        /// </summary>
        /// <param name="cancel">Cancel flag.</param>
        public void ExecuteLocal(bool cancel)
        {
            object res;
            bool success;

            Execute0(cancel, out res, out success);

            _jobRes = new ComputeJobResultImpl(
                success ? res : null, 
                success ? null : new IgniteException("Compute job has failed on local node, " +
                                                     "examine InnerException for details.", (Exception) res), 
                _job, 
                _ignite.GetIgnite().GetCluster().GetLocalNode().Id, 
                cancel
            );
        }

        /// <summary>
        /// Execute job serializing result to the stream.
        /// </summary>
        /// <param name="cancel">Whether the job must be cancelled.</param>
        /// <param name="stream">Stream.</param>
        public void ExecuteRemote(PlatformMemoryStream stream, bool cancel)
        {
            // 1. Execute job.
            object res;
            bool success;

            using (PeerAssemblyResolver.GetInstance(_ignite, Guid.Empty))
            {
                Execute0(cancel, out res, out success);
            }

            // 2. Try writing result to the stream.
            var writer = _ignite.Marshaller.StartMarshal(stream);

            try
            {
                // 3. Marshal results.
                BinaryUtils.WriteInvocationResult(writer, success, res);
            }
            finally
            {
                // 4. Process metadata.
                _ignite.Marshaller.FinishMarshal(writer);
            }
        }

        /// <summary>
        /// Cancel the job.
        /// </summary>
        public void Cancel()
        {
            _job.Cancel();
        }

        /// <summary>
        /// Serialize the job to the stream.
        /// </summary>
        /// <param name="stream">Stream.</param>
        /// <returns>True if successfull.</returns>
        [SuppressMessage("Microsoft.Design", "CA1031:DoNotCatchGeneralExceptionTypes",
            Justification = "User job can throw any exception")]
        internal bool Serialize(IBinaryStream stream)
        {
            BinaryWriter writer = _ignite.Marshaller.StartMarshal(stream);

            try
            {
                writer.Write(this);

                return true;
            }
            catch (Exception e)
            {
                writer.WriteString("Failed to marshal job [job=" + _job + ", errType=" + e.GetType().Name +
                    ", errMsg=" + e.Message + ']');

                return false;
            }
            finally
            {
                // 4. Process metadata.
                _ignite.Marshaller.FinishMarshal(writer);
            }
        }

        /// <summary>
        /// Job.
        /// </summary>
        internal IComputeJob Job
        {
            get { return _job; }
        }

        /// <summary>
        /// Job result.
        /// </summary>
        internal ComputeJobResultImpl JobResult
        {
            get { return _jobRes; }
        }

        /// <summary>
        /// Internal job execution routine.
        /// </summary>
        /// <param name="cancel">Cancel flag.</param>
        /// <param name="res">Result.</param>
        /// <param name="success">Success flag.</param>
        [SuppressMessage("Microsoft.Design", "CA1031:DoNotCatchGeneralExceptionTypes",
            Justification = "User job can throw any exception")]
        private void Execute0(bool cancel, out object res, out bool success)
        {
            // 1. Inject resources.
            IComputeResourceInjector injector = _job as IComputeResourceInjector;

            if (injector != null)
                injector.Inject(_ignite);
            else
                ResourceProcessor.Inject(_job, _ignite);

            // 2. Execute.
            try
            {
                if (cancel)
                    _job.Cancel();

                res = _job.Execute();

                success = true;
            }
            catch (Exception e)
            {
                res = e;

                success = false;
            }
        }

        /** <inheritDoc /> */
        public void WriteBinary(IBinaryWriter writer)
        {
            BinaryWriter writer0 = (BinaryWriter) writer.GetRawWriter();

            writer0.WriteObjectDetached(_job);
        }

        /// <summary>
        /// Create job instance.
        /// </summary>
        /// <param name="grid">Grid.</param>
        /// <param name="stream">Stream.</param>
        /// <returns></returns>
        internal static ComputeJobHolder CreateJob(Ignite grid, IBinaryStream stream)
        {
            try
            {
                return grid.Marshaller.StartUnmarshal(stream).ReadObject<ComputeJobHolder>();
            }
            catch (Exception e)
            {
                throw new IgniteException("Failed to deserialize the job [errType=" + e.GetType().Name +
                    ", errMsg=" + e.Message + ']');
            }
        }
    }
}
