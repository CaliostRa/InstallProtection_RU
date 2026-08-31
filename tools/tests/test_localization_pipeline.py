"""Regression tests for the presentation-only localization pipeline."""

import importlib.util
import io
import json
import re
import tempfile
import unittest
from pathlib import Path
from unittest import mock
from xml.etree import ElementTree


ROOT = Path(__file__).resolve().parents[2]
BUILDER_PATH = ROOT / "tools" / "build_localized_apk.py"


def load_builder():
    spec = importlib.util.spec_from_file_location("build_localized_apk", BUILDER_PATH)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class FakeResponse(io.BytesIO):
    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, tb):
        self.close()
        return False


class LocalizationPipelineTest(unittest.TestCase):
    def translations(self):
        return json.loads(
            (ROOT / "localization/ru-strings.json").read_text(encoding="utf-8")
        )

    def test_target_is_official_upstream_2160_release(self):
        builder = load_builder()
        self.assertEqual(234, builder.UPSTREAM_VERSION_CODE)
        self.assertEqual("2.1.60", builder.UPSTREAM_VERSION_NAME)
        self.assertEqual(18, builder.EXPECTED_UPSTREAM_DISPLAY_SINKS)
        self.assertEqual(
            "cc3197e4e63e8e3c74b05ee54b629539eb59a5b2d9937e8ee486b536fd334720",
            builder.UPSTREAM_APK_SHA256,
        )

    def test_required_russian_translations_are_present(self):
        required = {
            "安装防护模块": "Защита установки",
            "应用安装防护模块": "Модуль защиты приложений",
            "模块未激活": "Модуль не активирован",
            "模块已激活": "Модуль активирован",
            "核心拦截功能": "Основные функции перехвата",
            "基础拦截": "Базовый перехват",
            "文件保护": "Защита файлов",
            "反射防御": "Защита от обходных проверок",
            "网络拦截": "Сетевой перехват",
            "数据防护": "Подмена данных",
            "应用包管理": "Управление пакетами",
            "包名列表": "Список пакетов",
            "当前状态": "Текущий режим",
            "已安装": "Установлено",
            "未安装": "Не установлено",
            "捕获应用总累计": "Всего обнаружено пакетов",
            "切换为已安装": "Считать установленными",
            "切换为未安装": "Считать неустановленными",
            "更多配置设置": "Дополнительные настройки",
            "启动拦截设置": "Перехват запуска",
            "权限防护设置": "Защита разрешений",
            "拦截退出设置": "Блокировка принудительного выхода",
            "返回键设置": "Настройки кнопки «Назад»",
            "伪装强制模式": "Подмена SELinux Enforcing",
            "伪装分享设置": "Подмена функции «Поделиться»",
            "网络代理设置": "Настройки VPN и прокси",
            "痕迹检测设置": "Скрытие следов модификаций",
            "配置文件导出": "Экспорт конфигурации",
            "配置文件导入": "Импорт конфигурации",
            "查看日志记录": "Просмотр журналов",
            "检查模块版本更新": "Проверить обновления модуля",
            "网络代理设置(Beta)": "VPN и прокси (Beta)",
            "VPN接口隐藏": "Скрытие VPN-интерфейса",
            "深度接口隐藏": "Расширенное скрытие сетевых интерфейсов",
            "代理检测隐藏": "Скрытие проверки прокси",
            "代理环境隐藏": "Скрытие proxy-окружения",
            "抓包应用隐藏": "Скрытие приложений анализа трафика",
            "网络文件隐藏": "Скрытие сетевых системных файлов",
            "网络检测监听": "Мониторинг сетевых проверок",
            "SSL信任链绕过": "Обход цепочки доверия SSL",
            "SSL证书绑定绕过": "Обход certificate pinning",
            "SSL证书信息隐藏": "Скрытие информации сертификата",
            "SSL证书特征替换": "Подмена признаков сертификата",
            "处理列表": "Список обработки",
            "保存设置": "Сохранить",
            "取消": "Отмена",
            "痕迹检测设置(Beta)": "Скрытие следов (Beta)",
            "框架隐藏": "Скрытие Xposed/LSPatch",
            "文件隐藏": "Скрытие файлов",
            "服务隐藏": "Скрытие сервисов",
            "信息隐藏": "Скрытие системной информации",
            "命令隐藏": "Скрытие результатов команд",
            "堆栈隐藏": "Очистка stack trace",
            "Root隐藏": "Скрытие Root",
            "ADB调试隐藏": "Скрытие ADB",
            "开发者隐藏": "Скрытие параметров разработчика",
            "实时日志": "Журнал в реальном времени",
            "全部": "Все",
            "应用查询": "Проверки приложений",
            "痕迹隐藏": "Скрытие следов",
            "网络数据": "Сетевые данные",
            "应用响应": "Реакции приложения",
            "数据展开": "Развернуть данные",
            "顶部": "В начало",
            "底部": "В конец",
            "返回应用": "Вернуться в приложение",
            "记录中": "Запись",
            "管理配置/悬浮窗设置": "Настройки конфигурации и плавающего окна",
            "隐藏悬浮窗(长期)": "Скрыть плавающее окно надолго",
            "临时隐藏": "Скрыть временно",
            "清理配置": "Очистить конфигурацию",
            "状态切换成功": "Режим успешно изменён",
            "切换成功": "Переключение выполнено",
            "是否立即刷新应用使状态生效？": (
                "Перезапустить приложение сейчас, чтобы применить изменения?"
            ),
            "稍后": "Позже",
            "立即刷新": "Перезапустить сейчас",
            "伪造安装(": "Защита (",
            "伪造安装(已安装)[拦截]": "Защита: есть [перехват]",
            "伪造安装(未安装)[拦截]": "Защита: нет [перехват]",
            "[拦截]": " [перехват]",
            " ◎ 启动拦截设置": "◎ Перехват запуска",
            " ◎ 拦截退出设置": "◎ Блокировка выхода",
            " ◎ 权限防护设置": "◎ Защита разрешений",
            " ◎ 痕迹检测设置": "◎ Скрытие следов",
            " ◎ 网络代理设置": "◎ VPN и прокси",
            " ◎ 返回键设置": "◎ Кнопка «Назад»",
        }
        translations = self.translations()
        for source, expected in required.items():
            self.assertEqual(expected, translations.get(source), source)

    def test_hook_match_strings_are_not_translated(self):
        for protected in ("退出", "关闭", "伪造App"):
            self.assertNotIn(protected, self.translations())

    def test_translation_values_are_complete(self):
        placeholder = re.compile(r"[A-Z]*PH\d+[A-Z]*")
        han = re.compile(r"[\u3400-\u9fff]")
        broken = {
            source: translated
            for source, translated in self.translations().items()
            if not source
            or not translated
            or placeholder.search(translated)
            or han.search(translated)
        }
        self.assertEqual({}, broken)

    def test_every_translation_was_explicitly_reviewed(self):
        reviewed = json.loads(
            (ROOT / "localization/ru-overrides.json").read_text(encoding="utf-8")
        )

        self.assertEqual(self.translations(), reviewed)

    def test_translation_values_have_no_known_machine_translation_artifacts(self):
        artifacts = re.compile(
            r"необыч|диалогов.{0,10}ящ|фонтан|арбуз|приклад|подлож|"
            r"стартал|магнет|резин|тарелк|чаров|прегрев|снонир|"
            r"синонир|пустын|микросообщ|микроблог",
            re.IGNORECASE,
        )
        broken = {
            source: translated
            for source, translated in self.translations().items()
            if artifacts.search(translated)
        }

        self.assertEqual({}, broken)

    def test_runtime_log_technical_tokens_are_preserved(self):
        tokens = (
            "getNetworkInterfaces",
            "getNetworkCapabilities",
            "getActiveNetwork",
            "/proc/net",
            "PackageManager",
            "NetworkCapabilities",
            "SELinux",
            "TRANSPORT_VPN",
            "NOT_VPN",
            "Xposed",
            "LSPatch",
            "Root",
            "ADB",
            "SSL",
            "VPN",
            "Activity",
            "Intent",
        )
        label_exceptions = {
            "SSL证书信息隐藏",
            "SSL证书特征替换",
            "SSL证书绑定绕过",
        }
        broken = []
        for source, translated in self.translations().items():
            for token in tokens:
                if (
                    source not in label_exceptions
                    and token in source
                    and token not in translated
                ):
                    broken.append((source, translated, token))
        self.assertEqual([], broken)

    def test_markup_and_android_format_arguments_are_preserved(self):
        tag_pattern = re.compile(r"<[^>]+>")
        argument_pattern = re.compile(r"%\d+\$[a-zA-Z]")
        broken = []
        for source, translated in self.translations().items():
            if tag_pattern.findall(source) != tag_pattern.findall(translated):
                broken.append((source, translated, "HTML"))
            if argument_pattern.findall(source) != argument_pattern.findall(translated):
                broken.append((source, translated, "format"))
        self.assertEqual([], broken)

    def test_package_ids_are_not_translated(self):
        package_pattern = re.compile(
            r"(?<![\w.])[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*){2,}(?![\w.])"
        )
        broken = []
        for source, translated in self.translations().items():
            for package_id in package_pattern.findall(source):
                if package_id not in translated:
                    broken.append((source, translated, package_id))
        self.assertEqual([], broken)

    def test_cyrillic_and_technical_tokens_do_not_run_together(self):
        joined = re.compile(
            r"(?:[A-Za-z0-9_)][А-Яа-яЁё]|[А-Яа-яЁё][A-Za-z0-9_(])"
        )
        broken = {
            source: translated
            for source, translated in self.translations().items()
            if joined.search(translated)
        }
        self.assertEqual({}, broken)

    def test_only_display_sink_is_instrumented(self):
        builder = load_builder()
        display_proxy = """.method public static setLabel(Landroid/widget/TextView;Ljava/lang/CharSequence;)V
    .locals 0
    invoke-virtual {p0, p1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    return-void
.end method
"""
        functional_proxy = """.method public static setTarget(Landroid/content/Intent;Ljava/lang/String;)V
    .locals 0
    invoke-virtual {p0, p1}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;
    return-void
.end method
"""
        patched, count = builder.instrument_display_sinks(display_proxy)
        untouched, unrelated_count = builder.instrument_display_sinks(functional_proxy)
        self.assertEqual(1, count)
        self.assertIn(
            "invoke-static {p1}, Lcom/install/appinstall/xl/ru/RuStrings;"
            "->translate(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;",
            patched,
        )
        self.assertIn("move-result-object p1", patched)
        self.assertEqual(0, unrelated_count)
        self.assertEqual(functional_proxy, untouched)

    def test_display_sink_instrumentation_handles_html_and_arrays(self):
        builder = load_builder()
        proxy = """    invoke-static {p0, p1}, Landroid/text/Html;->fromHtml(Ljava/lang/String;I)Landroid/text/Spanned;
    invoke-virtual {p0, p1, p2}, Landroid/app/AlertDialog$Builder;->setItems([Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;
"""
        patched, count = builder.instrument_display_sinks(proxy)
        self.assertEqual(2, count)
        self.assertIn(
            "->translateString(Ljava/lang/String;)Ljava/lang/String;", patched
        )
        self.assertIn(
            "->translateArray([Ljava/lang/CharSequence;)[Ljava/lang/CharSequence;",
            patched,
        )

    def test_smali_patch_leaves_decoder_and_hook_classes_unchanged(self):
        builder = load_builder()
        decoder = """.class public Lobfuse/NPStringFog;
.super Ljava/lang/Object;
.method public static decode(Ljava/lang/String;)Ljava/lang/String;
    .locals 1
    const-string v0, "示例"
    return-object v0
.end method
"""
        hook = """.class public Lcom/install/appinstall/xl/HookInit;
.super Ljava/lang/Object;
.method public static label()Ljava/lang/String;
    .locals 1
    const-string v0, "示例"
    return-object v0
.end method
"""
        display = """.class public Lcom/install/appinstall/xl/UiProxy;
.super Ljava/lang/Object;
.method public static setLabel(Landroid/widget/TextView;Ljava/lang/CharSequence;)V
    .locals 0
    invoke-virtual {p0, p1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    return-void
.end method
"""
        with tempfile.TemporaryDirectory() as directory:
            decoded = Path(directory)
            decoder_path = decoded / "smali_classes2/obfuse/NPStringFog.smali"
            hook_path = decoded / "smali/com/install/appinstall/xl/HookInit.smali"
            display_path = decoded / "smali/com/install/appinstall/xl/UiProxy.smali"
            decoder_path.parent.mkdir(parents=True)
            hook_path.parent.mkdir(parents=True)
            display_path.parent.mkdir(parents=True, exist_ok=True)
            decoder_path.write_text(decoder, encoding="utf-8")
            hook_path.write_text(hook, encoding="utf-8")
            display_path.write_text(display, encoding="utf-8")
            count = builder.patch_smali_tree(decoded, {"示例": "Пример"})
            self.assertEqual(1, count)
            self.assertEqual(decoder, decoder_path.read_text(encoding="utf-8"))
            self.assertEqual(hook, hook_path.read_text(encoding="utf-8"))
            self.assertNotEqual(display, display_path.read_text(encoding="utf-8"))
            self.assertTrue(
                (
                    decoded / "smali/com/install/appinstall/xl/ru/RuStrings.smali"
                ).is_file()
            )

    def test_translator_smali_is_russian_locale_gated(self):
        builder = load_builder()
        smali = builder.render_translator_smali(
            {"安装防护模块": "Защита установки"}
        )
        self.assertIn("Ljava/util/Locale;->getDefault()Ljava/util/Locale;", smali)
        self.assertIn('const-string v1, "ru"', smali)
        self.assertNotIn(".method static constructor <clinit>()V", smali)
        self.assertIn("Ljava/util/LinkedHashMap;", smali)
        self.assertIn("translateString(Ljava/lang/String;)", smali)
        self.assertIn("translate(Ljava/lang/CharSequence;)", smali)
        self.assertIn("translateArray([Ljava/lang/CharSequence;)", smali)
        self.assertIn("->containsHan(Ljava/lang/String;)Z", smali)
        self.assertIn(
            "Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;",
            smali,
        )

    def test_russian_resources_keep_app_label_and_fork_name(self):
        builder = load_builder()
        with tempfile.TemporaryDirectory() as directory:
            decoded = Path(directory)
            builder.write_russian_resources(decoded)
            content = (decoded / "res/values-ru/strings.xml").read_text(
                encoding="utf-8"
            )
        self.assertIn("Install Protection RU", content)
        self.assertIn("InstallProtection_RU", content)
        self.assertIn("yijun01", content)

    def test_russian_resource_update_preserves_upstream_entries(self):
        builder = load_builder()
        with tempfile.TemporaryDirectory() as directory:
            decoded = Path(directory)
            destination = decoded / "res/values-ru/strings.xml"
            destination.parent.mkdir(parents=True)
            destination.write_text(
                """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Старое имя</string>
    <string name="upstream_only">Сохранить меня</string>
</resources>
""",
                encoding="utf-8",
            )
            builder.write_russian_resources(decoded)
            document = ElementTree.parse(destination)
            values = {
                node.attrib["name"]: node.text for node in document.findall("string")
            }
        self.assertEqual("Install Protection RU", values["app_name"])
        self.assertEqual("Сохранить меня", values["upstream_only"])

    def test_bad_download_checksum_removes_partial_file(self):
        builder = load_builder()
        with tempfile.TemporaryDirectory() as directory:
            destination = Path(directory) / "artifact.apk"
            with mock.patch.object(
                builder.urllib.request,
                "urlopen",
                side_effect=lambda *args, **kwargs: FakeResponse(b"tampered"),
            ):
                with self.assertRaises(RuntimeError):
                    builder.download_verified(
                        "https://example.invalid/artifact.apk",
                        destination,
                        "0" * 64,
                    )
            self.assertFalse(destination.exists())
            self.assertFalse(destination.with_suffix(".apk.part").exists())

    def test_network_error_removes_unique_partial_file(self):
        builder = load_builder()
        with tempfile.TemporaryDirectory() as directory:
            destination = Path(directory) / "artifact.apk"
            with mock.patch.object(
                builder.urllib.request,
                "urlopen",
                side_effect=OSError("network unavailable"),
            ):
                with self.assertRaises(OSError):
                    builder.download_verified(
                        "https://example.invalid/artifact.apk",
                        destination,
                        "0" * 64,
                    )
            self.assertFalse(destination.exists())
            self.assertEqual([], list(Path(directory).glob("*.part")))

    def test_download_retries_until_checksum_matches(self):
        builder = load_builder()
        responses = [FakeResponse(b"bad"), FakeResponse(b"good")]
        expected = (
            "770e607624d689265ca6c44884d0807d9b054d23c473c106c72be9de08b7376c"
        )

        with tempfile.TemporaryDirectory() as directory:
            destination = Path(directory) / "artifact.apk"
            with mock.patch.object(
                builder.urllib.request, "urlopen", side_effect=responses
            ):
                builder.download_verified(
                    "https://example.invalid/artifact.apk", destination, expected
                )
            self.assertEqual(expected, builder.sha256_file(destination))

    def test_download_uses_unique_temporary_file(self):
        builder = load_builder()
        payload = b"verified"
        expected_sha256 = builder.hashlib.sha256(payload).hexdigest()
        with tempfile.TemporaryDirectory() as directory:
            destination = Path(directory) / "artifact.apk"
            stale = destination.with_suffix(".apk.part")
            stale.write_bytes(b"stale")
            with mock.patch.object(
                builder.urllib.request,
                "urlopen",
                side_effect=lambda *args, **kwargs: FakeResponse(payload),
            ):
                builder.download_verified(
                    "https://example.invalid/artifact.apk",
                    destination,
                    expected_sha256,
                )
            self.assertEqual(payload, destination.read_bytes())
            self.assertEqual(b"stale", stale.read_bytes())

    def test_upstream_layout_rejects_wrong_xposed_entry(self):
        builder = load_builder()
        with tempfile.TemporaryDirectory() as directory:
            decoded = Path(directory)
            (decoded / "assets").mkdir(parents=True)
            (decoded / "assets/xposed_init").write_text(
                "com.example.WrongEntry\n", encoding="utf-8"
            )
            (decoded / "AndroidManifest.xml").write_text(
                """<manifest package="com.install.appinstall.xl">
<application>
<provider android:name="com.install.appinstall.xl.HookInit$HookProvider"
 android:authorities="com.install.appinstall.xl.hook.provider" />
<meta-data android:name="xposedmodule" />
<meta-data android:name="xposedminversion" />
</application>
</manifest>
""",
                encoding="utf-8",
            )
            with self.assertRaises(RuntimeError):
                builder.verify_upstream_layout(decoded)

    def test_source_resource_locales_have_matching_keys(self):
        resource_root = ROOT / "APP/installCheck/app/src/main/res"

        def keys(path):
            document = ElementTree.parse(path)
            return {node.attrib["name"] for node in document.findall("string")}

        self.assertEqual(
            keys(resource_root / "values/strings.xml"),
            keys(resource_root / "values-ru/strings.xml"),
        )


if __name__ == "__main__":
    unittest.main()
