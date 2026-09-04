#!/usr/bin/env python3
"""Report Java string literals that remain Chinese after runtime translation."""

import argparse
import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / "APP/installCheck/app/src/main/java"
CATALOG_PATH = ROOT / "localization/ru-strings.json"
ALLOWLIST_PATH = ROOT / "localization/han-allowlist.json"
STRING_LITERAL = re.compile(r'"(?:\\.|[^"\\])*"')
HAN = re.compile(r"[\u3400-\u9fff]")


def decode_java_literal(raw):
    try:
        return json.loads(raw)
    except json.JSONDecodeError:
        return None


def translate(source, catalog, ordered):
    exact = catalog.get(source)
    if exact is not None:
        return exact
    translated = source
    for original, replacement in ordered:
        if original in translated:
            translated = translated.replace(original, replacement)
    return translated


def audit():
    catalog = json.loads(CATALOG_PATH.read_text(encoding="utf-8"))
    allowlist = json.loads(ALLOWLIST_PATH.read_text(encoding="utf-8"))
    ordered = sorted(catalog.items(), key=lambda item: (-len(item[0]), item[0]))
    residual = {}
    for path in JAVA_ROOT.rglob("*.java"):
        if "/ru/" in path.as_posix():
            continue
        content = path.read_text(encoding="utf-8")
        for match in STRING_LITERAL.finditer(content):
            source = decode_java_literal(match.group())
            if source is None or not HAN.search(source):
                continue
            if source in allowlist:
                continue
            translated = translate(source, catalog, ordered)
            if not HAN.search(translated):
                continue
            item = residual.setdefault(
                source,
                {"source": source, "partial_translation": translated, "locations": []},
            )
            line = content.count("\n", 0, match.start()) + 1
            item["locations"].append(f"{path.relative_to(ROOT)}:{line}")
    return sorted(residual.values(), key=lambda item: item["locations"][0])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--json", type=Path)
    parser.add_argument("--fail-on-han", action="store_true")
    args = parser.parse_args()
    result = audit()
    if args.json:
        args.json.parent.mkdir(parents=True, exist_ok=True)
        args.json.write_text(
            json.dumps(result, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
    print(f"Residual Chinese literals: {len(result)}")
    if args.fail_on_han and result:
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
