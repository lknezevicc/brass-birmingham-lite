package hr.lknezevic.brassbirmingham.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized flow logger for tracing application behavior at runtime.
 * Uses a dedicated "FLOW" logger so output can be toggled independently via logback.xml.
 * <p>
 * Caller information (class + method + line) is resolved from the call stack automatically.
 */
public final class GameFlowLogger {

    private static final Logger log = LoggerFactory.getLogger("FLOW");
    private static final int CALLER_DEPTH = 3;

    private GameFlowLogger() {}

    public static void entering(String detail, Object... args) {
        if (!log.isDebugEnabled()) return;
        String caller = caller();
        String msg = format(detail, args);
        log.debug("[ENTER] {} — {}", caller, msg);
    }

    public static void entering() {
        if (!log.isDebugEnabled()) return;
        log.debug("[ENTER] {}", caller());
    }

    public static void exiting(String detail, Object... args) {
        if (!log.isDebugEnabled()) return;
        String caller = caller();
        String msg = format(detail, args);
        log.debug("[EXIT] {} — {}", caller, msg);
    }

    public static void exiting() {
        if (!log.isDebugEnabled()) return;
        log.debug("[EXIT] {}", caller());
    }

    public static void event(String what, Object... args) {
        if (!log.isDebugEnabled()) return;
        String caller = caller();
        String msg = format(what, args);
        log.debug("[EVENT] {} | {}", caller, msg);
    }

    public static void action(String actionType, String detail, Object... args) {
        if (!log.isDebugEnabled()) return;
        String caller = caller();
        String msg = format(detail, args);
        log.debug("[ACTION] {} | {} — {}", caller, actionType, msg);
    }

    public static void stateChange(String field, Object oldVal, Object newVal) {
        if (!log.isDebugEnabled()) return;
        String caller = caller();
        log.debug("[STATE] {} | {}: {} -> {}", caller, field, oldVal, newVal);
    }

    public static void network(String operation, Object... args) {
        if (!log.isDebugEnabled()) return;
        String caller = caller();
        String msg = format(operation, args);
        log.debug("[NET] {} | {}", caller, msg);
    }

    public static void error(String what, Throwable t) {
        String caller = caller();
        log.error("[ERROR] {} | {}", caller, what, t);
    }

    private static String caller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length <= CALLER_DEPTH) return "unknown";
        StackTraceElement el = stack[CALLER_DEPTH];
        String className = el.getClassName();
        int dot = className.lastIndexOf('.');
        if (dot > 0) className = className.substring(dot + 1);
        return className + "." + el.getMethodName() + "():" + el.getLineNumber();
    }

    private static String format(String template, Object... args) {
        if (args == null || args.length == 0) return template;
        StringBuilder sb = new StringBuilder();
        int argIdx = 0;
        int i = 0;
        while (i < template.length()) {
            if (i < template.length() - 1 && template.charAt(i) == '{' && template.charAt(i + 1) == '}') {
                sb.append(argIdx < args.length ? String.valueOf(args[argIdx++]) : "{}");
                i += 2;
            } else {
                sb.append(template.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
}
