# Аудит русской локализации

## Целевая сборка

- upstream tag: `234-2.1.60`;
- upstream commit: `3e930ddc1bc601d88255cd9f214a4b9a35e97327`;
- official APK SHA256: `cc3197e4e63e8e3c74b05ee54b629539eb59a5b2d9937e8ee486b536fd334720`;
- package: `com.install.appinstall.xl`;
- Xposed entry: `com.install.appinstall.xl.HookInit`;
- LSPatch provider: `com.install.appinstall.xl.HookInit$HookProvider`.

## Что локализуется

`localization/ru-strings.json` содержит 1208 вручную проверенных upstream-строк и два контекстных сокращения для overlay. Перевод применяется только непосредственно перед Android presentation API: `TextView`/`Button`/`EditText`, `AlertDialog`, `Toast`, `Html.fromHtml()` и массивы пунктов диалога. Проверка `Locale.getDefault().getLanguage().equals("ru")` выполняется внутри `RuStrings`; для других языков возвращается оригинал.

Android resources дополняются `res/values-ru/strings.xml` для app label, статуса Xposed и описания модуля. Default `res/values/strings.xml` остаётся оригинальным.

## Сознательно оставленный китайский текст

1. Ключи JSON-словаря — это оригинальные строки сопоставления. Они находятся внутри APK, но заменяются только при передаче пользовательского текста в presentation API.
2. Default и `values-zh` resources — оригинальный язык для всех locale, кроме русской.
3. `退出` и `关闭` — hook-match шаблоны кнопок целевого китайского приложения. Их перевод изменил бы package-detection/exit behavior.
4. `伪造App` — значение, подставляемое целевому приложению как fake app name. Оно не является интерфейсом модуля и сохранено ради совместимости.
5. JSON bodies, synthetic `dumpsys`, `packages.xml`, shell output и прочие данные, возвращаемые целевому приложению, не локализуются.
6. Имя оригинального автора `永恒之蓝（小淋）` сохранено как copyright/attribution.
7. Комментарии и исторические снимки в `Opensource-1.3/` и старом `APP/installCheck` не являются кодом целевой APK 2.1.60 и оставлены для upstream history.

## Hook-логика

Алгоритмы PackageManager, VPN/proxy, `/proc/net`, NetworkInterface, SSL, anti-Xposed, SELinux, ADB/Developer Options, overlay и LSPatch не переписываются. `obfuse.NPStringFog`, `HookInit`, `PkgMgr`, `VpnStatusFaker`, `AntiDetection`, `Selinuxhook` и `ShareHook` остаются байт-в-байт исходными.

Патч изменяет только 14 обфусцированных proxy-классов, которые непосредственно вызывают 18 presentation sinks, и добавляет новый locale-gated `RuStrings`. Функциональные String sinks — `Intent`, JSON, reflection, Xposed, команды, пути, package ID и сетевые API — не инструментируются.

## Layout и плавающее окно

В APK есть только пустой `activity_main.xml` с `match_parent`; основной интерфейс и overlay создаются программно. Жёстких XML-ширин для исправления нет, параметры окон не изменялись. Для полного составного текста добавлены контекстные сокращения: «Защита: есть [перехват]» и «Защита: нет [перехват]».

## Ограничения проверки

- Инструментальная проверка на реальном Android/LSPosed/LSPatch устройстве в CI отсутствует.
- Основной интерфейс программный, XML layout пустой; размеры проверяются косвенно короткими overlay-переводами и отсутствием изменения layout params.
- Новые upstream-релизы нельзя подменять без повторного аудита DEX, entry points и строкового каталога.
