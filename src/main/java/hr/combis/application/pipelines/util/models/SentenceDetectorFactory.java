package hr.combis.application.pipelines.util.models;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.FileInputStream;
import java.io.InputStream;

public class SentenceDetectorFactory {

    private static final SentenceDetectorME SENTENCE_DETECTOR = createSentenceDetector();

    private static SentenceDetectorME createSentenceDetector() {
        String modelPath = "/home/imihic/Documents/rag-engine/src/main/resources/models/hr-sent.bin";

        try (InputStream modelIn = new FileInputStream(modelPath)) {
            SentenceModel model = new SentenceModel(modelIn);
            return new SentenceDetectorME(model);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load the sentence model from path: " + modelPath, e);
        }
    }

    public static SentenceDetectorME getSentenceDetector() {
        return SENTENCE_DETECTOR;
    }
}
