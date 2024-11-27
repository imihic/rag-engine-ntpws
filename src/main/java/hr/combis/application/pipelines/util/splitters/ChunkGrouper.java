package hr.combis.application.pipelines.util.splitters;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class ChunkGrouper {

    private final EmbeddingModel embeddingModel;
    private final int bufferSize;
    private static final Percentile PERCENTILE_CALCULATOR = new Percentile();

    public ChunkGrouper(EmbeddingModel embeddingModel, int bufferSize) {
        this.embeddingModel = embeddingModel;
        this.bufferSize = bufferSize;
    }

    public List<TextSegment> groupSentencesIntoChunks(List<TextSegment> sentenceSegments, String documentId) {
        List<String> sentences = sentenceSegments.stream()
                .map(TextSegment::text)
                .collect(Collectors.toList());

        List<String> combinedSentences = createCombinedSentences(sentences);

        List<float[]> embeddings = computeEmbeddings(combinedSentences);

        List<Double> distances = computeCosineDistances(embeddings);

        double breakpointDistanceThreshold = computeBreakpointThreshold(distances);

        List<Integer> breakpoints = identifyBreakpoints(distances, breakpointDistanceThreshold);

        return createChunkSegments(sentences, breakpoints, documentId);
    }

    private List<String> createCombinedSentences(List<String> sentences) {
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

        return combinedSentences;
    }

    private List<float[]> computeEmbeddings(List<String> combinedSentences) {
        return combinedSentences.parallelStream()
                .map(sentence -> embeddingModel.embed(sentence).content().vector())
                .collect(Collectors.toList());
    }

    private List<Double> computeCosineDistances(List<float[]> embeddings) {
        return IntStream.range(0, embeddings.size() - 1)
                .parallel()
                .mapToDouble(i -> 1.0 - cosineSimilarity(embeddings.get(i), embeddings.get(i + 1)))
                .boxed()
                .collect(Collectors.toList());
    }

    private double computeBreakpointThreshold(List<Double> distances) {
        double[] distancesArray = distances.stream().mapToDouble(Double::doubleValue).toArray();
        synchronized (PERCENTILE_CALCULATOR) {
            PERCENTILE_CALCULATOR.setData(distancesArray);
            return PERCENTILE_CALCULATOR.evaluate(95.0); // 95th percentile
        }
    }

    private List<Integer> identifyBreakpoints(List<Double> distances, double threshold) {
        List<Integer> breakpoints = new ArrayList<>();
        for (int i = 0; i < distances.size(); i++) {
            if (distances.get(i) > threshold) {
                breakpoints.add(i + 1); // Since distances[i] is between embeddings[i] and embeddings[i+1]
            }
        }
        return breakpoints;
    }

    private List<TextSegment> createChunkSegments(List<String> sentences, List<Integer> breakpoints, String documentId) {
        List<TextSegment> chunkSegments = new ArrayList<>();
        int lastBreakpoint = 0;
        int chunkIndex = 0;
        for (int breakpoint : breakpoints) {
            List<String> chunkSentences = sentences.subList(lastBreakpoint, breakpoint);
            String chunkText = String.join(" ", chunkSentences).trim();

            Metadata metadata = new Metadata()
                    .put("document_id", documentId)
                    .put("chunk_id", UUID.randomUUID().toString())
                    .put("chunk_index", String.valueOf(chunkIndex))
                    .put("chunk_start_index", String.valueOf(lastBreakpoint))
                    .put("chunk_end_index", String.valueOf(breakpoint - 1));

            TextSegment chunkSegment = TextSegment.from(chunkText, metadata);
            chunkSegments.add(chunkSegment);

            lastBreakpoint = breakpoint;
            chunkIndex++;
        }
        if (lastBreakpoint < sentences.size()) {
            List<String> chunkSentences = sentences.subList(lastBreakpoint, sentences.size());
            String chunkText = String.join(" ", chunkSentences).trim();

            Metadata metadata = new Metadata()
                    .put("document_id", documentId)
                    .put("chunk_id", UUID.randomUUID().toString())
                    .put("chunk_index", String.valueOf(chunkIndex))
                    .put("chunk_start_index", String.valueOf(lastBreakpoint))
                    .put("chunk_end_index", String.valueOf(sentences.size() - 1));

            TextSegment chunkSegment = TextSegment.from(chunkText, metadata);
            chunkSegments.add(chunkSegment);
        }
        return chunkSegments;
    }

    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            double a = vectorA[i];
            double b = vectorB[i];
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }
        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return (denominator == 0.0) ? 0.0 : dotProduct / denominator;
    }
}

