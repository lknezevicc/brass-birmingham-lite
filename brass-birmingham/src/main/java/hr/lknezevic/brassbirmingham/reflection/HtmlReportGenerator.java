package hr.lknezevic.brassbirmingham.reflection;

import hr.lknezevic.brassbirmingham.model.industry.Industry;
import hr.lknezevic.brassbirmingham.model.industry.IndustryLevel;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public final class HtmlReportGenerator {

    private static final String TD_CLOSE = "</td>";
    private static final String TR_CLOSE = "</tr>\n";

    private static final List<String> GETTER_NAMES = List.of(
            "getBuildCost", "getVictoryPoints", "getIncomeBonus",
            "getCoalRequired", "getIronRequired", "getBeerRequired",
            "getResourceCapacity", "getFlipTrigger"
    );

    public void generate(File outputFile) {
        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + parent);
        }
        try (PrintWriter pw = new PrintWriter(outputFile, StandardCharsets.UTF_8)) {
            pw.println(buildHtml());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTML report", e);
        }
    }

    public String buildHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>Brass Birmingham Lite - Industry Report</title>\n");
        html.append(inlineCss());
        html.append("</head>\n<body>\n");
        html.append("<h1>Industry Report</h1>\n");
        html.append("<p>Generated via Java Reflection API (<code>Class.getDeclaredMethods()</code>)</p>\n");

        for (IndustryType type : IndustryType.values()) {
            html.append(generateTypeSection(type));
        }

        html.append("</body>\n</html>");
        return html.toString();
    }

    private String generateTypeSection(IndustryType type) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>").append(formatTypeName(type)).append("</h2>\n");
        sb.append("<table>\n<thead><tr><th>Attribute</th>");

        for (IndustryLevel level : IndustryLevel.values()) {
            sb.append("<th>").append(level.name()).append("</th>");
        }
        sb.append("</tr></thead>\n<tbody>\n");

        sb.append("<tr><td>Class</td>");
        for (IndustryLevel level : IndustryLevel.values()) {
            Industry instance = Industry.create(type, level);
            sb.append("<td>").append(instance.getClass().getSimpleName()).append(TD_CLOSE);
        }
        sb.append(TR_CLOSE);

        for (String getterName : GETTER_NAMES) {
            sb.append("<tr><td>").append(formatMethodName(getterName)).append(TD_CLOSE);
            for (IndustryLevel level : IndustryLevel.values()) {
                Industry instance = Industry.create(type, level);
                String value = invokeGetter(instance, getterName);
                sb.append("<td>").append(value).append(TD_CLOSE);
            }
            sb.append(TR_CLOSE);
        }

        sb.append("<tr><td>Declared Methods</td>");
        for (IndustryLevel level : IndustryLevel.values()) {
            Industry instance = Industry.create(type, level);
            Method[] methods = instance.getClass().getDeclaredMethods();
            String names = String.join(", ", Arrays.stream(methods).map(Method::getName).sorted().toList());
            sb.append("<td><small>").append(names).append("</small>").append(TD_CLOSE);
        }
        sb.append(TR_CLOSE);

        sb.append("</tbody>\n</table>\n");
        return sb.toString();
    }

    private String invokeGetter(Industry instance, String methodName) {
        try {
            Method method = instance.getClass().getMethod(methodName);
            method.setAccessible(true);
            Object result = method.invoke(instance);
            return result != null ? result.toString() : "null";
        } catch (ReflectiveOperationException e) {
            return "N/A";
        }
    }

    private String formatTypeName(IndustryType type) {
        String raw = type.name().replace('_', ' ');
        return raw.substring(0, 1).toUpperCase() + raw.substring(1).toLowerCase();
    }

    private String formatMethodName(String getter) {
        String name = getter.startsWith("get") ? getter.substring(3) : getter;
        return name.replaceAll("([A-Z])", " $1").trim();
    }

    private String inlineCss() {
        return """
                <style>
                body { font-family: system-ui, sans-serif; margin: 2rem; background: #1a1a2e; color: #eee; }
                h1 { color: #e9a820; }
                h2 { color: #c9952a; border-bottom: 1px solid #333; padding-bottom: 0.3rem; }
                table { border-collapse: collapse; width: 100%; margin-bottom: 2rem; }
                th, td { border: 1px solid #444; padding: 0.5rem 0.75rem; text-align: left; }
                th { background: #2a2a4a; }
                tr:nth-child(even) { background: #222240; }
                code { background: #333; padding: 0.2rem 0.4rem; border-radius: 3px; }
                small { font-size: 0.75rem; color: #aaa; }
                </style>
                """;
    }
}
