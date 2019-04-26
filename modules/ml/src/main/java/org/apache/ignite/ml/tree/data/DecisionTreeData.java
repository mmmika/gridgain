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

package org.apache.ignite.ml.tree.data;

import java.util.ArrayList;
import java.util.List;
import org.apache.ignite.ml.dataset.primitive.FeatureMatrixWithLabelsOnHeapData;
import org.apache.ignite.ml.tree.TreeFilter;

/**
 * A partition {@code data} of the containing matrix of features and vector of labels stored in heap
 * with index on features.
 */
public class DecisionTreeData extends FeatureMatrixWithLabelsOnHeapData implements AutoCloseable {
    /** Copy of vector with original labels. Auxiliary for Gradient Boosting on Trees.*/
    private double[] copiedOriginalLabels;

    /** Indexes cache. */
    private final List<TreeDataIndex> indexesCache;

    /** Build index. */
    private final boolean buildIdx;

    /**
     * Constructs a new instance of decision tree data.
     *
     * @param features Matrix with features.
     * @param labels Vector with labels.
     * @param buildIdx Build index.
     */
    public DecisionTreeData(double[][] features, double[] labels, boolean buildIdx) {
        super(features, labels);
        this.buildIdx = buildIdx;

        indexesCache = new ArrayList<>();
        if (buildIdx)
            indexesCache.add(new TreeDataIndex(features, labels));
    }

    /**
     * Filters objects and returns only data that passed filter.
     *
     * @param filter Filter.
     * @return Data passed filter.
     */
    public DecisionTreeData filter(TreeFilter filter) {
        int size = 0;

        double[][] features = getFeatures();
        double[] labels = getLabels();
        for (int i = 0; i < features.length; i++)
            if (filter.test(features[i]))
                size++;

        double[][] newFeatures = new double[size][];
        double[] newLabels = new double[size];

        int ptr = 0;

        for (int i = 0; i < features.length; i++) {
            if (filter.test(features[i])) {
                newFeatures[ptr] = features[i];
                newLabels[ptr] = labels[i];

                ptr++;
            }
        }

        return new DecisionTreeData(newFeatures, newLabels, buildIdx);
    }

    /**
     * Sorts data by specified column in ascending order.
     *
     * @param col Column.
     */
    public void sort(int col) {
        sort(col, 0, getFeatures().length - 1);
    }

    /** */
    private void sort(int col, int from, int to) {
        if (from < to) {
            double[][] features = getFeatures();
            double[] labels = getLabels();

            double pivot = features[(from + to) / 2][col];

            int i = from, j = to;

            while (i <= j) {
                while (features[i][col] < pivot)
                    i++;
                while (features[j][col] > pivot)
                    j--;

                if (i <= j) {
                    double[] tmpFeature = features[i];
                    features[i] = features[j];
                    features[j] = tmpFeature;

                    double tmpLb = labels[i];
                    labels[i] = labels[j];
                    labels[j] = tmpLb;

                    i++;
                    j--;
                }
            }

            sort(col, from, j);
            sort(col, i, to);
        }
    }

    /** */
    public double[] getCopiedOriginalLabels() {
        return copiedOriginalLabels;
    }

    /** */
    public void setCopiedOriginalLabels(double[] copiedOriginalLabels) {
        this.copiedOriginalLabels = copiedOriginalLabels;
    }

    /** {@inheritDoc} */
    @Override public void close() {
        // Do nothing, GC will clean up.
    }

    /**
     * Builds index in according to current tree depth and cached indexes in upper levels. Uses depth as key of cached
     * index and replaces cached index with same key.
     *
     * @param depth Tree Depth.
     * @param filter Filter.
     */
    public TreeDataIndex createIndexByFilter(int depth, TreeFilter filter) {
        assert depth >= 0 && depth <= indexesCache.size();

        if (depth > 0 && depth <= indexesCache.size() - 1) {
            for (int i = indexesCache.size() - 1; i >= depth; i--)
                indexesCache.remove(i);
        }

        if (depth == indexesCache.size()) {
            if (depth == 0)
                indexesCache.add(new TreeDataIndex(getFeatures(), getLabels()));
            else {
                TreeDataIndex lastIndex = indexesCache.get(depth - 1);
                indexesCache.add(lastIndex.filter(filter));
            }
        }

        return indexesCache.get(depth);
    }
}
