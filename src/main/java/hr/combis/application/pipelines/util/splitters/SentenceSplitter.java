package hr.combis.application.pipelines.util.splitters;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import opennlp.tools.sentdetect.SentenceDetectorME;

import java.util.ArrayList;
import java.util.List;

public class SentenceSplitter {

    private final SentenceDetectorME sentenceDetector;

    public SentenceSplitter(SentenceDetectorME sentenceDetector) {
        this.sentenceDetector = sentenceDetector;
    }

    public List<TextSegment> splitIntoSentences(String text, String documentId) {
        String[] sentencesArray = sentenceDetector.sentDetect(text);
        List<TextSegment> sentenceSegments = new ArrayList<>();
        for (int i = 0; i < sentencesArray.length; i++) {
            String sentenceText = sentencesArray[i];
            Metadata metadata = new Metadata()
                    .put("document_id", documentId)
                    .put("sentence_index", String.valueOf(i));
            TextSegment segment = TextSegment.from(sentenceText, metadata);
            sentenceSegments.add(segment);
        }
        return sentenceSegments;
    }
}
