"""Regression tests for the source-based Russian localization."""

import json
import re
import subprocess
import unittest
from pathlib import Path
from xml.etree import ElementTree


ROOT = Path(__file__).resolve().parents[2]
APP = ROOT / "APP" / "installCheck" / "app"
JAVA = APP / "src" / "main" / "java"
RES = APP / "src" / "main" / "res"


class SourceLocalizationTest(unittest.TestCase):
    def test_package_and_xposed_entry_points_are_unchanged(self):
        build_gradle = (APP / "build.gradle").read_text(encoding="utf-8")
        manifest = (APP / "src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        xposed_init = (APP / "src/main/assets/xposed_init").read_text(encoding="utf-8").strip()

        self.assertIn('applicationId "com.install.appinstall.xl"', build_gradle)
        version_code = re.search(r"versionCode\s+(\d+)", build_gradle)
        version_name = re.search(r'versionName\s+"([^"]+)"', build_gradle)
        self.assertIsNotNone(version_code)
        self.assertGreaterEqual(int(version_code.group(1)), 241)
        self.assertIsNotNone(version_name)
        self.assertRegex(version_name.group(1), r"^\d+\.\d+\.\d+")
        self.assertIn('com.install.appinstall.xl.HookInit$HookProvider', manifest)
        self.assertEqual("com.install.appinstall.xl.HookInit", xposed_init)

    def test_source_catalog_matches_generated_java(self):
        subprocess.run(
            ["python3", "tools/generate_ru_catalog.py", "--check"],
            cwd=ROOT,
            check=True,
        )

    def test_presentation_widgets_use_locale_aware_classes(self):
        forbidden = re.compile(
            r"new\s+(?:TextView|Button|CheckBox|RadioButton)\s*\(|"
            r"new\s+AlertDialog\.Builder\s*\(|Html\.fromHtml\s*\("
        )
        violations = []
        for path in JAVA.rglob("*.java"):
            if "/ru/" in path.as_posix():
                continue
            for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
                if forbidden.search(line):
                    violations.append(f"{path.relative_to(ROOT)}:{number}: {line.strip()}")
        self.assertEqual([], violations)

    def test_translation_catalog_is_complete_and_reviewed(self):
        catalog = json.loads((ROOT / "localization/ru-strings.json").read_text(encoding="utf-8"))
        reviewed = json.loads((ROOT / "localization/ru-overrides.json").read_text(encoding="utf-8"))
        han = re.compile(r"[\u3400-\u9fff]")

        self.assertEqual(catalog, reviewed)
        self.assertGreaterEqual(len(catalog), 1200)
        self.assertFalse({source: value for source, value in catalog.items() if han.search(value)})
        self.assertEqual("Защита: есть [блок.]", catalog["安装防护(已安装)[拦截]"])
        self.assertEqual("Защита: нет [блок.]", catalog["安装防护(未安装)[拦截]"])

    def test_required_interface_terms_are_translated(self):
        catalog = json.loads((ROOT / "localization/ru-strings.json").read_text(encoding="utf-8"))
        required = {
            "安装防护模块": "Защита установки",
            "应用安装防护模块": "Модуль защиты приложений",
            "模块未激活": "Модуль не активирован",
            "模块已激活": "Модуль активирован",
            "核心拦截功能": "Основные функции перехвата",
            "文件保护": "Защита файлов",
            "网络拦截": "Сетевой перехват",
            "应用包管理": "Управление пакетами",
            "包名列表": "Список пакетов",
            "当前状态": "Текущий режим",
            "更多配置设置": "Дополнительные настройки",
            "启动拦截设置": "Перехват запуска",
            "权限防护设置": "Защита разрешений",
            "返回键设置": "Настройки кнопки «Назад»",
            "网络代理设置": "Настройки VPN и прокси",
            "痕迹检测设置": "Скрытие следов модификаций",
            "配置文件导出": "Экспорт конфигурации",
            "配置文件导入": "Импорт конфигурации",
            "查看日志记录": "Просмотр журналов",
            "检查模块版本更新": "Проверить обновления модуля",
            "VPN接口隐藏": "Скрытие VPN-интерфейса",
            "框架隐藏": "Скрытие Xposed/LSPatch",
            "ADB调试隐藏": "Скрытие ADB",
            "开发者隐藏": "Скрытие параметров разработчика",
            "实时日志": "Журнал в реальном времени",
            "应用查询": "Проверки приложений",
            "网络数据": "Сетевые данные",
            "应用响应": "Реакции приложения",
            "返回应用": "Вернуться в приложение",
            "管理配置/悬浮窗设置": "Настройки конфигурации и плавающего окна",
            "隐藏悬浮窗(长期)": "Скрыть плавающее окно надолго",
            "清理配置": "Очистить конфигурацию",
            "是否立即刷新应用使状态生效？": (
                "Перезапустить приложение сейчас, чтобы применить изменения?"
            ),
        }
        for source, expected in required.items():
            self.assertEqual(expected, catalog.get(source), source)

    def test_runtime_translation_has_no_unreviewed_chinese(self):
        subprocess.run(
            ["python3", "tools/audit_translations.py", "--fail-on-han"],
            cwd=ROOT,
            check=True,
        )

    def test_markup_format_arguments_and_package_ids_are_preserved(self):
        catalog = json.loads((ROOT / "localization/ru-strings.json").read_text(encoding="utf-8"))
        tag_pattern = re.compile(r"<[^>]+>")
        argument_pattern = re.compile(r"%(?:\d+\$)?[a-zA-Z]")
        package_pattern = re.compile(
            r"(?<![\w.])[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*){2,}(?![\w.])"
        )
        broken = []
        for source, translated in catalog.items():
            if tag_pattern.findall(source) != tag_pattern.findall(translated):
                broken.append((source, "HTML"))
            if argument_pattern.findall(source) != argument_pattern.findall(translated):
                broken.append((source, "format"))
            for package_id in package_pattern.findall(source):
                if package_id not in translated:
                    broken.append((source, package_id))
        self.assertEqual([], broken)

    def test_runtime_log_technical_tokens_are_preserved(self):
        catalog = json.loads((ROOT / "localization/ru-strings.json").read_text(encoding="utf-8"))
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
        exceptions = {"SSL证书信息隐藏", "SSL证书特征替换", "SSL证书绑定绕过"}
        broken = []
        for source, translated in catalog.items():
            for token in tokens:
                if source not in exceptions and token in source and token not in translated:
                    broken.append((source, token))
        self.assertEqual([], broken)

    def test_realtime_log_copy_uses_localized_text(self):
        realog = (JAVA / "com/install/appinstall/xl/util/ReaLog.java").read_text(
            encoding="utf-8"
        )
        self.assertRegex(
            realog,
            r"RuStrings\.translateString\(\s*item\.toPlainText\(position \+ 1\)\)",
        )

    def test_android_resource_locales_have_matching_keys(self):
        def keys(path):
            root = ElementTree.parse(path).getroot()
            return {node.attrib["name"] for node in root.findall("string")}

        self.assertEqual(
            keys(RES / "values/strings.xml"),
            keys(RES / "values-ru/strings.xml"),
        )


if __name__ == "__main__":
    unittest.main()
