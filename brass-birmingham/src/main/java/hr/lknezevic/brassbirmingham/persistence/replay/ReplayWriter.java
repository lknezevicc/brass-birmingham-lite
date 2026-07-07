package hr.lknezevic.brassbirmingham.persistence.replay;

import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.action.GameAction;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ReplayWriter {

    private Document document;
    private Element rootElement;
    private Element currentEraElement;
    private Element currentRoundElement;
    private String currentEra;
    private int currentRound;

    public void startGame(List<String> playerNames) {
        startGame(playerNames, 0L);
    }

    public void startGame(List<String> playerNames, long deckSeed) {
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to create DOM document", e);
        }

        rootElement = document.createElement("brass-replay");
        rootElement.setAttribute("version", "1.0");
        document.appendChild(rootElement);

        Element metadata = document.createElement("metadata");
        rootElement.appendChild(metadata);

        Element dateEl = document.createElement("date");
        dateEl.setTextContent(Instant.now().toString());
        metadata.appendChild(dateEl);

        Element playersEl = document.createElement("players");
        metadata.appendChild(playersEl);
        for (int i = 0; i < playerNames.size(); i++) {
            Element playerEl = document.createElement("player");
            playerEl.setAttribute("index", String.valueOf(i));
            playerEl.setAttribute("name", playerNames.get(i));
            playersEl.appendChild(playerEl);
        }

        Element mapEl = document.createElement("map");
        mapEl.setTextContent("lite-5-cities");
        metadata.appendChild(mapEl);

        Element seedEl = document.createElement("deck-seed");
        seedEl.setTextContent(String.valueOf(deckSeed));
        metadata.appendChild(seedEl);

        currentEra = null;
        currentRound = -1;
    }

    public void appendMove(GameState state, int playerIndex, GameAction action) {
        if (document == null) return;

        String era = state.getCurrentEra().name().toLowerCase();
        int round = state.getCurrentRound();

        ensureEraAndRound(era, round);

        Element moveEl = document.createElement("move");
        moveEl.setAttribute("player", String.valueOf(playerIndex));
        moveEl.setAttribute("action", ReplayParamEncoder.actionTypeName(action));

        Element paramsEl = document.createElement("params");
        Map<String, String> params = ReplayParamEncoder.extractParams(action);
        for (var entry : params.entrySet()) {
            Element paramEl = document.createElement(entry.getKey());
            paramEl.setTextContent(entry.getValue());
            paramsEl.appendChild(paramEl);
        }
        moveEl.appendChild(paramsEl);

        currentRoundElement.appendChild(moveEl);
    }

    public void finishGame(int winnerId, int[] finalScores) {
        if (document == null) return;

        Element resultEl = document.createElement("result");
        resultEl.setAttribute("winner", String.valueOf(winnerId));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < finalScores.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(finalScores[i]);
        }
        resultEl.setAttribute("final-scores", sb.toString());
        rootElement.appendChild(resultEl);
    }

    public void writeToFile(File file) {
        if (document == null) return;
        GameFlowLogger.event("Writing replay XML to {}", file.getPath());
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + parent);
        }
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(document), new StreamResult(file));
        } catch (Exception e) {
            throw new RuntimeException("Failed to write replay XML", e);
        }
    }

    public String writeToString() {
        if (document == null) return "";
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            StringWriter sw = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize replay XML", e);
        }
    }

    public boolean isRecording() {
        return document != null;
    }

    private void ensureEraAndRound(String era, int round) {
        if (!era.equals(currentEra)) {
            currentEraElement = document.createElement("era");
            currentEraElement.setAttribute("name", era);
            rootElement.appendChild(currentEraElement);
            currentEra = era;
            currentRound = -1;
        }
        if (round != currentRound) {
            currentRoundElement = document.createElement("round");
            currentRoundElement.setAttribute("number", String.valueOf(round));
            currentEraElement.appendChild(currentRoundElement);
            currentRound = round;
        }
    }

}
