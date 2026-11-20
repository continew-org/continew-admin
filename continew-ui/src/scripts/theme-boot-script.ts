/**
 * Boot script that reads user preference values from cookies or localStorage
 * based on the configured persistence mode.
 *
 * The script is inlined into index.html at build time so the data attributes and
 * CSS variables are applied before the first paint, which keeps the theme and
 * layout from flickering on reload. Fonts are not part of it: `--app-font-sans`
 * lives in globals.css and is bundled with the stylesheet.
 */
import { PREFERENCE_REGISTRY } from '../lib/preferences/preferences-config'

export function buildThemeBootScript(): string {
  const registry = JSON.stringify(PREFERENCE_REGISTRY)

  return `
    (function () {
      try {
        var root = document.documentElement;
        var REGISTRY = ${registry};

        function readCookie(name) {
          var match = document.cookie.split("; ").find(function(c) {
            return c.startsWith(name + "=");
          });
          return match ? decodeURIComponent(match.split("=")[1]) : null;
        }

        function readLocal(name) {
          try {
            return window.localStorage.getItem(name);
          } catch (e) {
            return null;
          }
        }

        function readPreference(key, definition) {
          var storageKey = definition.persistence === "localStorage" ? "pref_" + key : key;
          var raw = null;

          if (definition.persistence === "localStorage") {
            raw = readLocal(storageKey);
          } else if (definition.persistence === "client-cookie") {
            raw = readCookie(storageKey);
          }

          if (raw == null || raw === "") return definition.defaultValue;

          if (definition.type === "enum") {
            return definition.values.indexOf(raw) >= 0 ? raw : definition.defaultValue;
          }

          if (definition.type === "number") {
            var value = Number(String(raw).replace(/[^0-9.+-]/g, ""));
            if (!isFinite(value)) return definition.defaultValue;
            value = Math.round(value);
            if (value < definition.min) value = definition.min;
            if (value > definition.max) value = definition.max;
            return value;
          }

          return raw;
        }

        var preferences = {};

        Object.keys(REGISTRY).forEach(function(key) {
          var definition = REGISTRY[key];
          var value = readPreference(key, definition);

          preferences[key] = value;

          if (definition.attribute) {
            root.setAttribute(definition.attribute, String(value));
          }

          if (definition.cssVar) {
            var names = Array.isArray(definition.cssVar) ? definition.cssVar : [definition.cssVar];
            var cssValue = definition.type === "number" && definition.unit ? value + definition.unit : String(value);
            names.forEach(function(name) {
              root.style.setProperty(name, cssValue);
            });
          }
        });

        var mode = preferences.theme_mode;
        var resolvedMode =
          mode === "system" && window.matchMedia
            ? (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light")
            : mode === "dark"
              ? "dark"
              : "light";

        root.classList.toggle("dark", resolvedMode === "dark");
        root.style.colorScheme = resolvedMode;
      } catch (e) {
        console.warn("ThemeBootScript error:", e);
      }
    })();
  `
}
