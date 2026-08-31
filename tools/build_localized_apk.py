#!/usr/bin/env python3
"""Build a Russian-localized APK from the official upstream 2.1.60 release.

Upstream does not publish the Java sources used for its 2.1.x APKs. This tool
keeps the upstream hook implementation and applies locale-gated translation
only at Android presentation sinks plus normal Android resources.
"""

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import tempfile
import urllib.request
from pathlib import Path
from typing import Dict, Iterable, Tuple
from xml.etree import ElementTree


UPSTREAM_VERSION_CODE = 234
UPSTREAM_VERSION_NAME = "2.1.60"
UPSTREAM_APK_URL = (
    "https://github.com/yijun01/com.install.appinstall.xl/releases/download/"
    "234-2.1.60/com.install.appinstall.xl_2.1.60.234.apk"
)
UPSTREAM_APK_SHA256 = "cc3197e4e63e8e3c74b05ee54b629539eb59a5b2d9937e8ee486b536fd334720"
APKTOOL_URL = (
    "https://github.com/iBotPeaches/Apktool/releases/download/"
    "v3.0.3/apktool_3.0.3.jar"
)
APKTOOL_SHA256 = "dbf930b076c6b9be08d57c449cacefc3bdd6b71ebd59b3066fc0e1f5b14f9423"
TRANSLATOR_CLASS = "Lcom/install/appinstall/xl/ru/RuStrings;"
PROTECTED_STRINGS = {"退出", "关闭", "伪造App"}
EXPECTED_UPSTREAM_DISPLAY_SINKS = 18

# Exact presentation-only APIs used by the obfuscated upstream proxy classes.
# Functional String APIs (Intent, PackageManager, Xposed, JSON, reflection,
# networking, files and shell commands) are deliberately absent.
DISPLAY_SINKS = {
    "Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V": (1, "charsequence"),
    "Landroid/widget/CheckBox;->setText(Ljava/lang/CharSequence;)V": (1, "charsequence"),
    "Landroid/widget/Button;->setText(Ljava/lang/CharSequence;)V": (1, "charsequence"),
    "Landroid/widget/RadioButton;->setText(Ljava/lang/CharSequence;)V": (1, "charsequence"),
    "Landroid/widget/EditText;->setText(Ljava/lang/CharSequence;)V": (1, "charsequence"),
    "Landroid/widget/EditText;->setHint(Ljava/lang/CharSequence;)V": (1, "charsequence"),
    "Landroid/app/AlertDialog;->setTitle(Ljava/lang/CharSequence;)V": (1, "charsequence"),
    "Landroid/app/AlertDialog;->setMessage(Ljava/lang/CharSequence;)V": (1, "charsequence"),
    "Landroid/app/AlertDialog$Builder;->setTitle(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;": (1, "charsequence"),
    "Landroid/app/AlertDialog$Builder;->setMessage(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;": (1, "charsequence"),
    "Landroid/app/AlertDialog$Builder;->setPositiveButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;": (1, "charsequence"),
    "Landroid/app/AlertDialog$Builder;->setNegativeButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;": (1, "charsequence"),
    "Landroid/app/AlertDialog$Builder;->setNeutralButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;": (1, "charsequence"),
    "Landroid/app/AlertDialog$Builder;->setItems([Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;": (1, "array"),
    "Landroid/app/AlertDialog$Builder;->setSingleChoiceItems([Ljava/lang/CharSequence;ILandroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;": (1, "array"),
    "Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;": (1, "charsequence"),
    "Landroid/text/Html;->fromHtml(Ljava/lang/String;)Landroid/text/Spanned;": (0, "string"),
    "Landroid/text/Html;->fromHtml(Ljava/lang/String;I)Landroid/text/Spanned;": (0, "string"),
}


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def download_verified(url: str, destination: Path, expected_sha256: str) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    if destination.exists() and sha256_file(destination) == expected_sha256:
        return

    last_actual = None
    for _ in range(3):
        request = urllib.request.Request(
            url, headers={"User-Agent": "Install-Protection-RU-builder"}
        )
        temporary = None
        try:
            with tempfile.NamedTemporaryFile(
                dir=destination.parent,
                prefix=destination.name + ".",
                suffix=".part",
                delete=False,
            ) as output:
                temporary = Path(output.name)
                with urllib.request.urlopen(request, timeout=60) as response:
                    shutil.copyfileobj(response, output)

            actual = sha256_file(temporary)
            if actual == expected_sha256:
                temporary.replace(destination)
                return
            last_actual = actual
        finally:
            if temporary is not None:
                temporary.unlink(missing_ok=True)

    raise RuntimeError(
        "SHA256 mismatch for {}: expected {}, got {}".format(
            url, expected_sha256, last_actual
        )
    )


def load_translations(path: Path) -> Dict[str, str]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise ValueError("Translation file must contain a JSON object")

    translations = {}
    for source, translated in data.items():
        if not isinstance(source, str) or not isinstance(translated, str):
            raise ValueError("Every translation key and value must be a string")
        if source in PROTECTED_STRINGS:
            raise ValueError(
                "Protected hook-match string must not be translated: {}".format(source)
            )
        if source and translated and source != translated:
            translations[source] = translated
    return translations


def smali_quote(value: str) -> str:
    return (
        value.replace("\\", "\\\\")
        .replace('"', '\\"')
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
    )


def chunks(items: Iterable[Tuple[str, str]], size: int):
    chunk = []
    for item in items:
        chunk.append(item)
        if len(chunk) == size:
            yield chunk
            chunk = []
    if chunk:
        yield chunk


def render_translator_smali(translations: Dict[str, str]) -> str:
    # Longest keys first makes fragment replacement deterministic.
    ordered = sorted(translations.items(), key=lambda item: (-len(item[0]), item[0]))
    groups = list(chunks(ordered, 120))
    lines = [
        ".class public final Lcom/install/appinstall/xl/ru/RuStrings;",
        ".super Ljava/lang/Object;",
        '.source "RuStrings.java"',
        "",
        ".field private static volatile TRANSLATIONS:Ljava/util/Map;",
        "",
        ".method private static translations()Ljava/util/Map;",
        "    .locals 1",
        "    sget-object v0, {}->TRANSLATIONS:Ljava/util/Map;".format(TRANSLATOR_CLASS),
        "    if-nez v0, :ready",
        "    new-instance v0, Ljava/util/LinkedHashMap;",
        "    invoke-direct {v0}, Ljava/util/LinkedHashMap;-><init>()V",
    ]
    for index in range(len(groups)):
        lines.append(
            "    invoke-static {{v0}}, {}->add{}(Ljava/util/Map;)V".format(
                TRANSLATOR_CLASS, index
            )
        )
    lines.extend(
        [
            "    sput-object v0, {}->TRANSLATIONS:Ljava/util/Map;".format(
                TRANSLATOR_CLASS
            ),
            "  :ready",
            "    return-object v0",
            ".end method",
            "",
            ".method private constructor <init>()V",
            "    .locals 0",
            "    invoke-direct {p0}, Ljava/lang/Object;-><init>()V",
            "    return-void",
            ".end method",
            "",
        ]
    )

    for index, group in enumerate(groups):
        lines.extend(
            [
                ".method private static add{}(Ljava/util/Map;)V".format(index),
                "    .locals 3",
            ]
        )
        for source, translated in group:
            lines.extend(
                [
                    '    const-string v1, "{}"'.format(smali_quote(source)),
                    '    const-string v2, "{}"'.format(smali_quote(translated)),
                    "    invoke-interface {p0, v1, v2}, Ljava/util/Map;"
                    "->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                ]
            )
        lines.extend(["    return-void", ".end method", ""])

    lines.extend(
        [
            ".method private static containsHan(Ljava/lang/String;)Z",
            "    .locals 4",
            "    const/4 v0, 0x0",
            "    invoke-virtual {p0}, Ljava/lang/String;->length()I",
            "    move-result v1",
            "  :scan",
            "    if-ge v0, v1, :not_found",
            "    invoke-virtual {p0, v0}, Ljava/lang/String;->charAt(I)C",
            "    move-result v2",
            "    const/16 v3, 0x3400",
            "    if-lt v2, v3, :next",
            "    const v3, 0x9fff",
            "    if-gt v2, v3, :next",
            "    const/4 v0, 0x1",
            "    return v0",
            "  :next",
            "    add-int/lit8 v0, v0, 0x1",
            "    goto :scan",
            "  :not_found",
            "    const/4 v0, 0x0",
            "    return v0",
            ".end method",
            "",
            ".method public static translateString(Ljava/lang/String;)Ljava/lang/String;",
            "    .locals 6",
            "    if-eqz p0, :original",
            "    invoke-static {}, Ljava/util/Locale;->getDefault()Ljava/util/Locale;",
            "    move-result-object v0",
            "    invoke-virtual {v0}, Ljava/util/Locale;->getLanguage()Ljava/lang/String;",
            "    move-result-object v0",
            '    const-string v1, "ru"',
            "    invoke-virtual {v1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z",
            "    move-result v2",
            "    if-eqz v2, :original",
            "    invoke-static {{}}, {}->translations()Ljava/util/Map;".format(
                TRANSLATOR_CLASS
            ),
            "    move-result-object v0",
            "    invoke-interface {v0, p0}, Ljava/util/Map;"
            "->get(Ljava/lang/Object;)Ljava/lang/Object;",
            "    move-result-object v1",
            "    check-cast v1, Ljava/lang/String;",
            "    if-eqz v1, :fragments",
            "    return-object v1",
            "  :fragments",
            "    invoke-static {{p0}}, {}->containsHan(Ljava/lang/String;)Z".format(
                TRANSLATOR_CLASS
            ),
            "    move-result v1",
            "    if-eqz v1, :original",
            "    invoke-interface {v0}, Ljava/util/Map;->entrySet()Ljava/util/Set;",
            "    move-result-object v1",
            "    invoke-interface {v1}, Ljava/util/Set;->iterator()Ljava/util/Iterator;",
            "    move-result-object v1",
            "    move-object v2, p0",
            "  :replace_loop",
            "    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z",
            "    move-result v3",
            "    if-eqz v3, :translated",
            "    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;",
            "    move-result-object v3",
            "    check-cast v3, Ljava/util/Map$Entry;",
            "    invoke-interface {v3}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;",
            "    move-result-object v4",
            "    check-cast v4, Ljava/lang/String;",
            "    invoke-interface {v3}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;",
            "    move-result-object v5",
            "    check-cast v5, Ljava/lang/String;",
            "    invoke-virtual {v2, v4}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z",
            "    move-result v3",
            "    if-eqz v3, :replace_loop",
            "    invoke-virtual {v2, v4, v5}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;",
            "    move-result-object v2",
            "    goto :replace_loop",
            "  :translated",
            "    return-object v2",
            "  :original",
            "    return-object p0",
            ".end method",
            "",
            ".method public static translate(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;",
            "    .locals 1",
            "    if-eqz p0, :char_original",
            "    instance-of v0, p0, Ljava/lang/String;",
            "    if-eqz v0, :char_original",
            "    check-cast p0, Ljava/lang/String;",
            "    invoke-static {{p0}}, {}->translateString(Ljava/lang/String;)Ljava/lang/String;".format(
                TRANSLATOR_CLASS
            ),
            "    move-result-object p0",
            "  :char_original",
            "    return-object p0",
            ".end method",
            "",
            ".method public static translateArray([Ljava/lang/CharSequence;)[Ljava/lang/CharSequence;",
            "    .locals 4",
            "    if-eqz p0, :array_original",
            "    invoke-virtual {p0}, [Ljava/lang/CharSequence;->clone()Ljava/lang/Object;",
            "    move-result-object v0",
            "    check-cast v0, [Ljava/lang/CharSequence;",
            "    array-length v1, v0",
            "    const/4 v2, 0x0",
            "  :array_loop",
            "    if-ge v2, v1, :array_done",
            "    aget-object v3, v0, v2",
            "    invoke-static {{v3}}, {}->translate(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;".format(
                TRANSLATOR_CLASS
            ),
            "    move-result-object v3",
            "    aput-object v3, v0, v2",
            "    add-int/lit8 v2, v2, 0x1",
            "    goto :array_loop",
            "  :array_done",
            "    return-object v0",
            "  :array_original",
            "    return-object p0",
            ".end method",
            "",
        ]
    )
    return "\n".join(lines)


def instrument_display_sinks(smali: str) -> Tuple[str, int]:
    lines = smali.splitlines()
    patched = []
    count = 0
    invoke_pattern = re.compile(
        r"^(\s*)invoke-(?:virtual|static)\s+\{([^}]*)\},\s+(\S+)\s*$"
    )
    translator_methods = {
        "charsequence": (
            "translate(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;"
        ),
        "string": "translateString(Ljava/lang/String;)Ljava/lang/String;",
        "array": "translateArray([Ljava/lang/CharSequence;)[Ljava/lang/CharSequence;",
    }

    for line in lines:
        match = invoke_pattern.match(line)
        sink = DISPLAY_SINKS.get(match.group(3)) if match else None
        if sink:
            indent, register_list, _ = match.groups()
            if ".." in register_list:
                raise RuntimeError(
                    "Display sink uses unsupported register range: {}".format(line)
                )
            registers = [register.strip() for register in register_list.split(",")]
            argument_index, translator_kind = sink
            if argument_index >= len(registers):
                raise RuntimeError(
                    "Malformed display sink invocation: {}".format(line)
                )
            register = registers[argument_index]
            patched.append(
                "{}invoke-static {{{}}}, {}->{}".format(
                    indent,
                    register,
                    TRANSLATOR_CLASS,
                    translator_methods[translator_kind],
                )
            )
            patched.append("{}move-result-object {}".format(indent, register))
            count += 1
        patched.append(line)

    suffix = "\n" if smali.endswith("\n") else ""
    return "\n".join(patched) + suffix, count


def patch_smali_tree(decoded: Path, translations: Dict[str, str]) -> int:
    smali_roots = sorted(path for path in decoded.glob("smali*") if path.is_dir())
    if not smali_roots:
        raise RuntimeError("Apktool output contains no smali directories")

    total = 0
    for smali_root in smali_roots:
        for smali_path in smali_root.rglob("*.smali"):
            original = smali_path.read_text(encoding="utf-8")
            patched, count = instrument_display_sinks(original)
            if count:
                smali_path.write_text(patched, encoding="utf-8")
                total += count
    if total == 0:
        raise RuntimeError("No supported Android presentation sinks were found")

    translator_path = smali_roots[0] / "com/install/appinstall/xl/ru/RuStrings.smali"
    translator_path.parent.mkdir(parents=True, exist_ok=True)
    translator_path.write_text(render_translator_smali(translations), encoding="utf-8")
    return total


def write_russian_resources(decoded: Path) -> None:
    destination = decoded / "res/values-ru/strings.xml"
    if destination.is_file():
        tree = ElementTree.parse(destination)
        resources = tree.getroot()
    else:
        resources = ElementTree.Element("resources")
        tree = ElementTree.ElementTree(resources)
    values = {
        "app_name": "Защита установки",
        "xposed_activated": "Xposed: активирован",
        "xposed_unactivated": "Xposed: не активирован",
        "xposed_xiaolin": (
            "Защита от принудительной установки сторонних приложений\n"
            "Оригинальный автор: yijun01 / 永恒之蓝(小淋)\n"
            "Основано на Install Protection 2.1.60 (234)\n"
            "InstallProtectionR — русская локализация / RU fork\n"
            "Лицензия upstream: GPL-3.0 с дополнительным уведомлением\n"
            "Upstream: github.com/yijun01/com.install.appinstall.xl\n"
            "RU source: github.com/CaliostRa/InstallProtectionR"
        ),
    }
    for name, value in values.items():
        node = resources.find("string[@name='{}']".format(name))
        if node is None:
            node = ElementTree.SubElement(resources, "string", {"name": name})
        node.text = value

    destination.parent.mkdir(parents=True, exist_ok=True)
    ElementTree.indent(resources, space="    ")
    tree.write(destination, encoding="utf-8", xml_declaration=True)


def verify_upstream_layout(decoded: Path) -> None:
    manifest = (decoded / "AndroidManifest.xml").read_text(encoding="utf-8")
    xposed_entry = (decoded / "assets/xposed_init").read_text(encoding="utf-8").strip()
    required_manifest_tokens = (
        'package="com.install.appinstall.xl"',
        'android:name="com.install.appinstall.xl.HookInit$HookProvider"',
        'android:authorities="com.install.appinstall.xl.hook.provider"',
        'android:name="xposedmodule"',
        'android:name="xposedminversion"',
    )
    missing = [token for token in required_manifest_tokens if token not in manifest]
    if missing:
        raise RuntimeError(
            "Unexpected upstream manifest; missing: {}".format(", ".join(missing))
        )
    if xposed_entry != "com.install.appinstall.xl.HookInit":
        raise RuntimeError("Unexpected Xposed entry point: {}".format(xposed_entry))


def run(command, cwd: Path) -> None:
    subprocess.run(command, cwd=str(cwd), check=True)


def build(project_root: Path, variant: str, java_command: str) -> Path:
    build_root = project_root / "build"
    cache = build_root / "cache"
    work = build_root / "work" / variant
    decoded = work / "decoded"
    output_dir = build_root / "outputs" / "apk" / variant
    output = output_dir / (
        "InstallProtectionR-{}-{}-unsigned.apk".format(
            UPSTREAM_VERSION_NAME, variant
        )
    )
    upstream_apk = cache / "com.install.appinstall.xl_2.1.60.234.apk"
    apktool_jar = cache / "apktool_3.0.3.jar"

    download_verified(UPSTREAM_APK_URL, upstream_apk, UPSTREAM_APK_SHA256)
    download_verified(APKTOOL_URL, apktool_jar, APKTOOL_SHA256)

    if work.exists():
        shutil.rmtree(work)
    output_dir.mkdir(parents=True, exist_ok=True)

    run(
        [
            java_command,
            "-jar",
            str(apktool_jar),
            "d",
            "-f",
            str(upstream_apk),
            "-o",
            str(decoded),
        ],
        project_root,
    )
    verify_upstream_layout(decoded)
    translations = load_translations(project_root / "localization/ru-strings.json")
    write_russian_resources(decoded)
    instrumented_display_sinks = patch_smali_tree(decoded, translations)
    if instrumented_display_sinks != EXPECTED_UPSTREAM_DISPLAY_SINKS:
        raise RuntimeError(
            "Unexpected presentation sink count: expected {}, found {}".format(
                EXPECTED_UPSTREAM_DISPLAY_SINKS, instrumented_display_sinks
            )
        )
    run(
        [
            java_command,
            "-jar",
            str(apktool_jar),
            "b",
            str(decoded),
            "-o",
            str(output),
        ],
        project_root,
    )

    report = {
        "artifact": str(output.relative_to(project_root)),
        "artifact_sha256": sha256_file(output),
        "instrumented_display_sinks": instrumented_display_sinks,
        "package_name": "com.install.appinstall.xl",
        "translations": len(translations),
        "upstream_apk_sha256": UPSTREAM_APK_SHA256,
        "upstream_version_code": UPSTREAM_VERSION_CODE,
        "upstream_version_name": UPSTREAM_VERSION_NAME,
        "variant": variant,
        "xposed_entry": "com.install.appinstall.xl.HookInit",
    }
    report_path = output.with_suffix(".json")
    report_path.write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print(json.dumps(report, ensure_ascii=False, indent=2))
    return output


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--variant", choices=("debug", "release"), required=True)
    parser.add_argument(
        "--project-root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
    )
    parser.add_argument("--java", default="java")
    args = parser.parse_args()
    build(args.project_root.resolve(), args.variant, args.java)


if __name__ == "__main__":
    main()
