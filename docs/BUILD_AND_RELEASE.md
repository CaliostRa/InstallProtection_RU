# Сборка и публикация InstallProtection_RU

Этот документ содержит технические детали сборки, подписи и публикации релизов. Основной пользовательский README находится в [`README.md`](../README.md).

## Локальная сборка

Из чистого clone проект собирается стандартным Gradle-сценарием:

```bash
./gradlew clean
./gradlew assembleDebug
./gradlew assembleRelease
```

Если переменные подписи release не заданы, `assembleRelease` создаёт unsigned APK.

Артефакты:

- `APP/installCheck/app/build/outputs/apk/debug/InstallProtection_RU-2.1.68-debug.apk`
- `APP/installCheck/app/build/outputs/apk/release/InstallProtection_RU-2.1.68-release-unsigned.apk`

## Локальная подписанная release-сборка

Подпись задаётся только внешними переменными окружения:

```bash
export ANDROID_KEYSTORE_FILE=/secure/path/install-protection-ru.jks
export ANDROID_KEY_ALIAS=install-protection-ru
export ANDROID_KEYSTORE_PASSWORD='...'
export ANDROID_KEY_PASSWORD='...'
./gradlew assembleRelease
```

Приватный ключ и пароли не должны попадать в репозиторий.

## GitHub Actions

Для release-job используются Secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_PASSWORD`

Их следует хранить в защищённом Environment `release`.

На обычных push/PR CI собирает debug и unsigned release APK. При публикации релиза workflow подписывает APK через `apksigner`, проверяет подпись, формирует SHA256 и прикладывает файлы к GitHub Release.

## Теги и versionCode

Встроенная проверка обновлений обращается к `releases/latest`, извлекает **первое число из `tag_name`** и сравнивает его с установленным Android `versionCode`.

Поэтому для релиза, который должен автоматически определяться как более новый:

1. `versionCode` APK должен быть больше предыдущего опубликованного `versionCode`.
2. Тег должен начинаться с того же числа.

Пример:

```text
versionCode 242
v242-2.1.68-ru3
```

Один только суффикс `ru1`, `ru2`, `ru3` не делает релиз новым для встроенного update-checker, если первое число тега и `versionCode` остаются прежними.

## Постоянная подпись RU-форка

Package name сохранён от upstream: `com.install.appinstall.xl`, но RU-форк подписывается собственным постоянным ключом.

Android не позволит установить RU APK как обновление поверх официального APK upstream с другой подписью. При переходе с официальной сборки рекомендуется сначала экспортировать конфигурацию, удалить upstream APK и затем установить RU APK.

Все последующие RU-релизы должны подписываться одним и тем же ключом.

Постоянный сертификат релизов `InstallProtection_RU`:

```text
SHA-256: 05:D1:8F:3F:21:F5:1A:24:6D:A1:53:C1:F7:3F:88:9B:86:DF:CD:6D:92:1A:F3:70:8C:77:68:FC:8D:8F:DA:36
Valid until: 2054-01-20
```

Перед установкой APK из стороннего источника сверяйте fingerprint сертификата и SHA256 опубликованного файла.
