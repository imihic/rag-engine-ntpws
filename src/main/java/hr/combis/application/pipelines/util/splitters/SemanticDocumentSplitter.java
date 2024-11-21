package hr.combis.application.pipelines.util.splitters;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class SemanticDocumentSplitter implements DocumentSplitter {

    private final EmbeddingModel embeddingModel;
    private final int bufferSize;
    private final SentenceDetectorME sentenceDetector;

    public SemanticDocumentSplitter(EmbeddingModel embeddingModel, int bufferSize) {
        this.embeddingModel = embeddingModel;
        this.bufferSize = bufferSize;
        this.sentenceDetector = createSentenceDetector();
    }

    @Override
    public List<TextSegment> split(Document document) {
        String text = document.text();
        String documentId = UUID.randomUUID().toString();

        // Step 1: Split into sentences
        String[] sentenceArray = sentenceDetector.sentDetect(text);
        List<String> sentences = Arrays.asList(sentenceArray);

        // Step 2: Create combined sentences with buffer
        List<String> combinedSentences = new ArrayList<>();

        for (int i = 0; i < sentences.size(); i++) {
            StringBuilder combinedSentence = new StringBuilder();

            // Add sentences before current one
            for (int j = i - bufferSize; j < i; j++) {
                if (j >= 0) {
                    combinedSentence.append(sentences.get(j)).append(" ");
                }
            }

            // Add current sentence
            combinedSentence.append(sentences.get(i));

            // Add sentences after current one
            for (int j = i + 1; j <= i + bufferSize; j++) {
                if (j < sentences.size()) {
                    combinedSentence.append(" ").append(sentences.get(j));
                }
            }

            combinedSentences.add(combinedSentence.toString());
        }

        // Step 3: Compute embeddings for combined sentences
        List<float[]> embeddings = new ArrayList<>();
        for (String combinedSentence : combinedSentences) {
            float[] embedding = embeddingModel.embed(combinedSentence).content().vector();
            embeddings.add(embedding);
        }

        // Step 4: Compute cosine distances between consecutive embeddings
        List<Double> distances = new ArrayList<>();
        for (int i = 0; i < embeddings.size() - 1; i++) {
            double similarity = cosineSimilarity(embeddings.get(i), embeddings.get(i + 1));
            double distance = 1.0 - similarity;
            distances.add(distance);
        }

        // Step 5: Compute threshold as 95th percentile of distances
        double[] distancesArray = distances.stream().mapToDouble(Double::doubleValue).toArray();
        double breakpointPercentileThreshold = 95.0;
        double breakpointDistanceThreshold = percentile(distancesArray, breakpointPercentileThreshold);

        // Step 6: Identify breakpoints where distance exceeds threshold
        List<Integer> breakpoints = new ArrayList<>();
        for (int i = 0; i < distances.size(); i++) {
            if (distances.get(i) > breakpointDistanceThreshold) {
                int breakpointIndex = i + 1; // Since distances[i] is between embeddings[i] and embeddings[i+1]
                breakpoints.add(breakpointIndex);
            }
        }

        // Step 7: Group sentences into chunks based on breakpoints
        List<String> chunks = new ArrayList<>();
        int lastBreakpoint = 0;
        for (int breakpoint : breakpoints) {
            StringBuilder chunkBuilder = new StringBuilder();
            for (int i = lastBreakpoint; i < breakpoint; i++) {
                chunkBuilder.append(sentences.get(i)).append(" ");
            }
            chunks.add(chunkBuilder.toString().trim());
            lastBreakpoint = breakpoint;
        }

        // Add the remaining sentences as the last chunk
        if (lastBreakpoint < sentences.size()) {
            StringBuilder chunkBuilder = new StringBuilder();
            for (int i = lastBreakpoint; i < sentences.size(); i++) {
                chunkBuilder.append(sentences.get(i)).append(" ");
            }
            chunks.add(chunkBuilder.toString().trim());
        }

        // Step 8: Create TextSegments for each chunk
        List<TextSegment> segments = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            Metadata metadata = new Metadata()
                    .put("document_id", documentId)
                    .put("chunk_id", UUID.randomUUID().toString())
                    .put("chunk_index", String.valueOf(i));
            TextSegment segment = TextSegment.from(chunk, metadata);
            segments.add(segment);
        }

        return segments;
    }

    private SentenceDetectorME createSentenceDetector() {
        /*
        try (InputStream modelIn = getClass().getResourceAsStream("/models/hr-sent.bin")) {
            SentenceModel model = new SentenceModel(modelIn);
            return new SentenceDetectorME(model);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load the Croatian sentence model", e);
        }
        */
        // Absolute path to the model file
        String modelPath = "/home/imihic/Documents/rag-engine/src/main/resources/models/hr-sent.bin";

        try (InputStream modelIn = new FileInputStream(modelPath)) {
            SentenceModel model = new SentenceModel(modelIn);
            return new SentenceDetectorME(model);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load the Croatian sentence model from path: " + modelPath, e);
        }
    }

    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Helper method to compute the percentile
    private static double percentile(double[] values, double percentile) {
        Arrays.sort(values);
        int index = (int) Math.ceil(percentile / 100.0 * values.length) - 1;
        index = Math.min(Math.max(index, 0), values.length - 1); // Ensure index is within bounds
        return values[index];
    }
}
