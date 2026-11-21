/**
 * Boot script that reads every saved preference (cookie or localStorage) and writes it to
 * `<html>` as data attributes and CSS custom properties.
 *
 * The script is inlined into index.html at build time so the values are applied before the
 * first paint, which keeps the theme and layout from flickering on reload. Only the DOM
 * targets are serialized, so the inlined payload stays small.
 *
 * `parse` mirrors `parsePreference` in preferences-config.ts; keep the two in step.
 */
import { getPreferenceStorageKey, PREFERENCE_KEYS, PREFERENCE_REGISTRY } from '../lib/preferences/preferences-config'

export function buildThemeBootScript(): string {
  // Only preferences that reach the DOM are needed before the first paint.
  const entries = PREFERENCE_KEYS.flatMap(key => {
    const d = PREFERENCE_REGISTRY[key] as Record<string, unknown>
    if (!d.attribute && !d.cssVar) return []
    return [
      [
        getPreferenceStorageKey(key),
        d.persistence === 'client-cookie' ? 1 : 0,
        d.type,
        d.defaultValue,
        d.type === 'enum' ? d.values : d.type === 'number' ? [d.min, d.max] : d.type === 'text' ? d.maxLength : 0,
        d.attribute ?? 0,
        d.cssVar ? ([] as unknown[]).concat(d.cssVar) : 0,
        d.unit ?? ''
      ]
    ]
  })

  return `
    (function () {
      try {
        var root = document.documentElement;
        var ENTRIES = ${JSON.stringify(entries)};
        var cookies = {};
        document.cookie.split("; ").forEach(function (c) {
          var i = c.indexOf("=");
          if (i <= 0) return;
          var raw = c.slice(i + 1);
          // One malformed cookie on the domain must not abort the whole script.
          try { raw = decodeURIComponent(raw); } catch (e) {}
          cookies[c.slice(0, i)] = raw;
        });

        function readLocal(name) {
          try { return window.localStorage.getItem(name); } catch (e) { return null; }
        }

        function parse(type, raw, fallback, extra) {
          if (raw == null) return fallback;
          if (type === "enum") return extra.indexOf(raw) >= 0 ? raw : fallback;
          if (type === "boolean") return raw === "true" ? true : raw === "false" ? false : fallback;
          if (type === "number") {
            var n = Number(raw);
            if (raw === "" || !isFinite(n)) return fallback;
            return Math.min(Math.max(Math.round(n), extra[0]), extra[1]);
          }
          if (type === "text") return raw.slice(0, extra);
          return raw;
        }

        var themeMode = "light";

        ENTRIES.forEach(function (e) {
          var raw = e[1] ? cookies[e[0]] : readLocal(e[0]);
          var value = parse(e[2], raw, e[3], e[4]);
          if (e[0] === "theme_mode") themeMode = value;
          if (e[5]) root.setAttribute(e[5], String(value));
          if (e[6] && value !== "") {
            e[6].forEach(function (name) { root.style.setProperty(name, value + e[7]); });
          }
        });

        var resolvedMode =
          themeMode === "system" && window.matchMedia
            ? (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light")
            : themeMode === "dark" ? "dark" : "light";

        root.classList.toggle("dark", resolvedMode === "dark");
        root.style.colorScheme = resolvedMode;
      } catch (e) {
        console.warn("ThemeBootScript error:", e);
      }
    })();
  `
}
