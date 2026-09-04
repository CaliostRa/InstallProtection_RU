# Аудит русской локализации

## Целевая сборка

- upstream release: `241-2.1.68`;
- точный upstream source commit: `5e584196f38568ccfd9a68b4d54a9a1117179010`;
- package: `com.install.appinstall.xl`;
- Xposed entry: `com.install.appinstall.xl.HookInit`;
- LSPatch provider: `com.install.appinstall.xl.HookInit$HookProvider`.

## Как устроена локализация

Статические metadata и элементы главного экрана, добавленные форком, используют одинаковый набор ключей в `res/values/strings.xml` и `res/values-ru/strings.xml`. Для русской locale имя приложения — `InstallProtection_RU`; для остальных locale остаётся исходный китайский язык.

Основной upstream UI версии 2.1.68 создаётся программно в Java. Чтобы не переписывать hook-код и не разносить тысячи точечных изменений, его исходные литералы сохранены, а перевод применяется только при выводе через locale-aware классы `RuTextView`, `RuButton`, `RuCheckBox`, `RuRadioButton`, `RuDialogBuilder`, HTML/Toast helpers. При не-русской locale helper возвращает исходную строку.

`localization/ru-strings.json` содержит 2555 проверенных пар. `tools/generate_ru_catalog.py` воспроизводимо создаёт Java-каталог, а `tools/audit_translations.py` проверяет все китайские Java-литералы активного app-модуля. Копия журнала в буфер обмена локализуется тем же presentation helper.

## Сознательно оставленный китайский текст

Полный grep по репозиторию продолжает находить Han-символы в следующих категориях:

1. Китайские ключи `localization/ru-strings.json` и сгенерированного `RuCatalog.java`. Они нужны для точного сопоставления с upstream-строками.
2. Исходные Java-литералы активного модуля. Они сохраняют оригинальный интерфейс для не-русских locale и облегчают будущий upstream sync; для русской locale их отображаемая часть покрыта каталогом. Автоматический audit сообщает `Residual Chinese literals: 0` после применения каталога.
3. `res/values/strings.xml` и `res/values-zh/strings.xml` — исходный/default язык по требованию fork-а.
4. Четыре значения из `localization/han-allowlist.json`, которые являются функциональными данными, а не интерфейсом:
   - `com.小淋.虚假APP` — служебный package ID upstream;
   - `虚假APP` — подставляемое hook-механизмом значение `appName`;
   - JSON `code=200` с `msg=检测通过`;
   - JSON `code=404` с `msg=应用未安装`.
5. Комментарии исходного Java-кода, имя автора `永恒之蓝（小淋）`, оригинальные README/LICENSE и неизменённый исторический snapshot `OpenSource-2.1.68/`. Они не являются непереведённым русским UI.

Package IDs, имена методов, API-константы, пути и технические маркеры (`PackageManager`, `getNetworkInterfaces`, `NetworkCapabilities`, `/proc/net`, `SELinux`, `TRANSPORT_VPN`, `NOT_VPN` и другие) каталог не переводит.

## Hook-логика и совместимость

Алгоритмы PackageManager, VPN/proxy, `/proc/net`, NetworkInterface, SSL, anti-Xposed, SELinux, ADB/Developer Options, package detection, overlay и LSPatch не перерабатывались. Изменения активных Java-файлов ограничены presentation sinks и ссылкой проверки обновлений на RU-форк. `applicationId`, provider и `assets/xposed_init` сохранены.

Два дублирующихся `case` в upstream `PermissionName.java`, не позволявшие исходнику компилироваться, были удалены при подготовке source build; первое исходное отображаемое значение каждого case сохранено.

## Layout и плавающее окно

Основной UI и overlay формируются программно. Для длинного русского footer строки размещены вертикально. Маленькое overlay использует короткие варианты:

- `Защита: есть [блок.]`;
- `Защита: нет [блок.]`.

## Проверки и ограничения

- `python3 -m unittest tools.tests.test_source_localization -v` проверяет catalog/resources, package/version/Xposed invariants, технические токены и presentation sinks.
- `./gradlew assembleDebug` и `./gradlew assembleRelease` компилируют активный source module; release без внешнего ключа намеренно unsigned.
- Инструментальная проверка всех экранов на реальном Android с LSPosed/LSPatch в CI отсутствует. Перед первым публичным релизом рекомендуется smoke-test на физическом устройстве: главный экран, package list, дополнительные настройки, VPN/proxy, скрытие следов, realtime logs, overlay и импорт/экспорт.
- Полный `:app:lintDebug` показывает накопленные upstream lint-проблемы в hook-коде. `lintVitalRelease` проходит; lint не отключён и ошибки не скрыты через `abortOnError false`.
