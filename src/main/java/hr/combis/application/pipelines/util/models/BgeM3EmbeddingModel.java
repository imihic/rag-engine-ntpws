package hr.combis.application.pipelines.util.models;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;

public class BgeM3EmbeddingModel {

    private static EmbeddingModel instance;

    public static EmbeddingModel getInstance() {
        if (instance == null) {
            String modelPath = "src/main/resources/models/bge-m3-quantized.onnx";
            String tokenizerPath = "src/main/resources/models/bge-m3-tokenizer.json"; // Adjust if tokenizer file is elsewhere
            PoolingMode poolingMode = PoolingMode.CLS; // Adjust if necessary

            instance = new OnnxEmbeddingModel(modelPath, tokenizerPath, poolingMode);
        }
        return instance;
    }
}
