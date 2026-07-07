package hr.lknezevic.brassbirmingham.persistence.replay;

import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public final class ReplayReader {

    private static final String SCHEMA_RESOURCE = "/replays/ReplaySchema.xsd";

    public ReplayDocument readFromFile(File file) {
        GameFlowLogger.entering("file={}", file.getName());
        try {
            ReplayHandler handler = new ReplayHandler();
            newValidatingParser().parse(file, handler);
            GameFlowLogger.exiting("moves={}", handler.getDocument().getMoves().size());
            return handler.getDocument();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse replay XML: " + file.getName(), e);
        }
    }

    public ReplayDocument readFromStream(InputStream inputStream) {
        try {
            ReplayHandler handler = new ReplayHandler();
            newValidatingParser().parse(inputStream, handler);
            return handler.getDocument();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse replay XML stream", e);
        }
    }

    private SAXParser newValidatingParser() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setNamespaceAware(true);

        try (InputStream schemaStream = ReplayReader.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (schemaStream != null) {
                SchemaFactory schemaFactory =
                        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new StreamSource(schemaStream));
                factory.setSchema(schema);
            }
        }
        return factory.newSAXParser();
    }

    private static final class ReplayHandler extends DefaultHandler {

        private final ReplayDocument doc = new ReplayDocument();
        private final StringBuilder textBuffer = new StringBuilder();

        private String currentEra;
        private int currentRound;
        private int currentMovePlayer;
        private String currentMoveAction;
        private boolean inParams;
        private String currentParamElement;
        private final Map<String, String> currentParams = new LinkedHashMap<>();

        public ReplayDocument getDocument() { return doc; }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            textBuffer.setLength(0);

            switch (qName) {
                case "brass-replay" -> {
                }
                case "player" -> {
                    String name = attrs.getValue("name");
                    if (name != null) doc.getPlayerNames().add(name);
                }
                case "era" -> currentEra = attrs.getValue("name");
                case "round" -> {
                    String num = attrs.getValue("number");
                    currentRound = num != null ? Integer.parseInt(num) : 0;
                }
                case "move" -> {
                    currentMovePlayer = Integer.parseInt(attrs.getValue("player"));
                    currentMoveAction = attrs.getValue("action");
                    currentParams.clear();
                }
                case "params" -> inParams = true;
                case "result" -> {
                    String winner = attrs.getValue("winner");
                    if (winner != null) doc.setWinnerIndex(Integer.parseInt(winner));
                    doc.setFinalScores(attrs.getValue("final-scores"));
                }
                default -> {
                    if (inParams) currentParamElement = qName;
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            switch (qName) {
                case "date" -> doc.setDate(textBuffer.toString().trim());
                case "map" -> doc.setMapId(textBuffer.toString().trim());
                case "deck-seed" -> {
                    String val = textBuffer.toString().trim();
                    if (!val.isEmpty()) doc.setDeckSeed(Long.parseLong(val));
                }
                case "params" -> inParams = false;
                case "move" -> {
                    ReplayMove move = new ReplayMove(
                            currentMovePlayer, currentMoveAction,
                            currentEra, currentRound,
                            Map.copyOf(currentParams)
                    );
                    doc.addMove(move);
                }
                default -> {
                    if (inParams && currentParamElement != null && currentParamElement.equals(qName)) {
                        currentParams.put(qName, textBuffer.toString().trim());
                        currentParamElement = null;
                    }
                }
            }
            textBuffer.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            textBuffer.append(ch, start, length);
        }

        @Override
        public void error(SAXParseException e) throws SAXParseException {
            throw e;
        }

        @Override
        public void warning(SAXParseException e) throws SAXParseException {
            throw e;
        }
    }
}
