/*-
 *
 *  * Copyright 2016 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package org.deeplearning4j.nn.conf.graph;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.InputPreProcessor;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.inputs.InvalidInputTypeException;
import org.deeplearning4j.nn.conf.memory.MemoryReport;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.Arrays;

/**
 * LayerVertex is a GraphVertex with a neural network Layer (and, optionally an {@link InputPreProcessor}) in it
 *
 * @author Alex Black
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class LayerVertex extends GraphVertex {

    private NeuralNetConfiguration layerConf;
    //Set outputVertex to true when Layer is an OutputLayer, OR For use in specialized situations like reinforcement learning
    // For RL situations, this Layer insn't an OutputLayer, but is the last layer in a graph, that gets its error/epsilon
    // passed in externally
    private boolean outputVertex;


    public LayerVertex(NeuralNetConfiguration layerConf) {
        this.layerConf = layerConf;
    }

    @Override
    public GraphVertex clone() {
        return new LayerVertex(layerConf.clone());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LayerVertex))
            return false;
        LayerVertex lv = (LayerVertex) o;
        if ((layerConf == null && lv.layerConf != null) || (layerConf != null && lv.layerConf == null)) {
            return false;
        }
        if (layerConf != null && !layerConf.equals(lv.layerConf))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return layerConf.hashCode();
    }

    @Override
    public int numParams(boolean backprop) {
        return layerConf.getLayer().initializer().numParams(layerConf);
    }

    @Override
    public int minVertexInputs() {
        return 1;
    }

    @Override
    public int maxVertexInputs() {
        return 1;
    }

    @Override
    public Layer instantiate(ComputationGraph graph, String name, int idx,
                             int numInputs, INDArray paramsView, boolean initializeParams) {
        //Now, we need to work out if this vertex is an output vertex or not...
        boolean isOutput = graph.getConfiguration().getNetworkOutputs().contains(name);

        org.deeplearning4j.nn.api.Layer layer =
                        layerConf.getLayer().instantiate(layerConf, null, idx, paramsView, initializeParams);

        return layer;
    }

    @Override
    public InputType[] getOutputType(int layerIndex, InputType... vertexInputs) throws InvalidInputTypeException {
        return layerConf.getLayer().getOutputType(layerIndex, vertexInputs);
    }

    @Override
    public MemoryReport getMemoryReport(InputType... inputTypes) {
        //TODO preprocessor memory
        return layerConf.getLayer().getMemoryReport(inputTypes[0]);
    }
}
