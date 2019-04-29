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

package org.apache.ignite.examples.ml.tutorial;

import java.io.FileNotFoundException;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.ml.dataset.feature.extractor.Vectorizer;
import org.apache.ignite.ml.dataset.feature.extractor.impl.DummyVectorizer;
import org.apache.ignite.ml.math.primitives.vector.Vector;
import org.apache.ignite.ml.pipeline.Pipeline;
import org.apache.ignite.ml.pipeline.PipelineMdl;
import org.apache.ignite.ml.preprocessing.encoding.EncoderTrainer;
import org.apache.ignite.ml.preprocessing.encoding.EncoderType;
import org.apache.ignite.ml.preprocessing.imputing.ImputerTrainer;
import org.apache.ignite.ml.preprocessing.minmaxscaling.MinMaxScalerTrainer;
import org.apache.ignite.ml.preprocessing.normalization.NormalizationTrainer;
import org.apache.ignite.ml.selection.scoring.evaluator.Evaluator;
import org.apache.ignite.ml.selection.scoring.metric.classification.Accuracy;
import org.apache.ignite.ml.tree.DecisionTreeClassificationTrainer;

/**
 * {@link MinMaxScalerTrainer} and {@link NormalizationTrainer} are used in this example due to different values
 * distribution in columns and rows.
 * <p>
 * Code in this example launches Ignite grid and fills the cache with test data (based on Titanic passengers data).</p>
 * <p>
 * After that it defines preprocessors that extract features from an upstream data and perform other desired changes
 * over the extracted data, including the scaling.</p>
 * <p>
 * Then, it trains the model based on the processed data using decision tree classification.</p>
 * <p>
 * Finally, this example uses {@link Evaluator} functionality to compute metrics from predictions.</p>
 */
public class Step_5_Scaling_with_Pipeline {
    /** Run example. */
    public static void main(String[] args) {
        System.out.println();
        System.out.println(">>> Tutorial step 5 (scaling) via Pipeline example started.");

        try (Ignite ignite = Ignition.start("examples/config/example-ignite.xml")) {
            try {
                IgniteCache<Integer, Vector> dataCache = TitanicUtils.readPassengers(ignite);

                // Extracts "pclass", "sibsp", "parch", "sex", "embarked", "age", "fare".
                final Vectorizer<Integer, Vector, Integer, Double> vectorizer
                    = new DummyVectorizer<Integer>(0, 3, 4, 5, 6, 8, 10).labeled(1);

                PipelineMdl<Integer, Vector> mdl = new Pipeline<Integer, Vector, Integer, Double>()
                    .addVectorizer(vectorizer)
                    .addPreprocessingTrainer(new EncoderTrainer<Integer, Vector>()
                        .withEncoderType(EncoderType.STRING_ENCODER)
                        .withEncodedFeature(1)
                        .withEncodedFeature(6))
                    .addPreprocessingTrainer(new ImputerTrainer<Integer, Vector>())
                    .addPreprocessingTrainer(new MinMaxScalerTrainer<Integer, Vector>())
                    .addPreprocessingTrainer(new NormalizationTrainer<Integer, Vector>()
                        .withP(1))
                    .addTrainer(new DecisionTreeClassificationTrainer(5, 0))
                    .fit(ignite, dataCache);

                System.out.println("\n>>> Trained model: " + mdl);

                double accuracy = Evaluator.evaluate(
                    dataCache,
                    mdl,
                    mdl.getPreprocessor(),
                    new Accuracy<>()
                );

                System.out.println("\n>>> Accuracy " + accuracy);
                System.out.println("\n>>> Test Error " + (1 - accuracy));

                System.out.println(">>> Tutorial step 5 (scaling) via Pipeline example completed.");
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
