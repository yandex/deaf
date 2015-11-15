**Talk**

[Talk](https://mobile.ru/apps/android/talk#page1) is a helper for personal communication between hearing loss and hearing people. The application translates collocutor's speech into text and can play written text on the contrary. Moreover, the application has a number of features that make communication with Talk quick and easy: phrase enlarging, template phrases and conversatoin history.

If you're looking to install Talk for Android, you can find it on [Google Play](https://play.google.com/store/apps/details?id=ru.subtitles). If you're a developer wanting to contribute, read on.


How to start
------------
*  Clone repository: git clone https://github.com/mobile-ubtitles-android.git.
*  Import project to **Android Studio** (version 1.2 or higher).
*  App is based on Yandex.SpeechKit technology and you should register your Mobile SDK API key at [Yandex.SpeechKit Mobile SDK documentation](https://tech.yandex.com/speechkit/mobilesdk/).
*  Open class [SpeechKitInitializer.java](https://github.com/mobile-subtitles-android/blob/master/app/src/main/java/ru/subtitles/service/speechkit/initializer/SpeechKitInitializer.java) and replace `API_KEY` value by registered Mobile SDK API key.
*  Also you should provide API key for Yandex.SpeechKit Cloud. Register your Yandex.SpeechKit Cloud API key at [Yandex.SpeechKit Cloud documentation](https://tech.yandex.ru/speechkit/cloud/).
*  Open class [SpeechKitTtsCloudApi.java](https://github.com/mobile-subtitles-android/blob/master/app/src/main/java/ru/subtitles/service/cache/SpeechKitTtsCloudApi.java) and replace `API_KEY` value by registered Cloud API key.
*  If you want to track user activity in application you should enable `YandexMetricaEventTracker` or provide your own `EventTracker` implementation. Please see [SubtitlesApplication.java](https://github.com/mobile-subtitles-android/blob/master/app/src/main/java/ru/subtitles/SubtitlesApplication.java) for more details.
*  Now you are ready to launch project!

Please notice, project requires the following tools have been already installed: Android SDK (API 16-23), Android Support Repository rev. 24, Android Support Library rev. 23.1, Build Tools rev. 21.1.2.

How does it work
----------------
Application architecture design based on the following principle: `Activity` > `Service` > `ContentProvider`. `Service` receives and executes user actions that sent from `Activity`/`Fragment`. `Service` also executes async writing to the database when it's needed. Data loads from database by using `Loader` classes that get callbacks when data identified by a given content URI changes. Some events send from `Service` by using broadcast messages.

There are two services to work with different kinds of data:
* `PhrasesService` is a service that handles adding, editing and deleting starting phrases. Also service dispatches events to invalidate audio samples.
* `MessagingService` is a service that dispatches messaging and recognition events.

Please see package [content](https://github.com/mobile-subtitles-android/blob/master/app/src/main/java/ru/subtitles/content/) for more details about working with data in the app. If you want to get more details about `ContentProvider` please refer to [official documentation](http://developer.android.com/intl/ru/guide/topics/providers/content-provider-basics.html).

More
----

Looking for more? Please check the following pages:
* [Wiki](https://github.com/mobile-subtitles-android/wiki) - there you can find a helpful information about the app.
* JavaDoc, which is located at mobile-subtitles-android/doc - there you can find a generated documentation for base application classes.

If you found an issue or want to improve app, please create a new issue [here](https://github.com/mobile-subtitles-android/issues).


License
-------

License agreement on use of Yandex SpeechKit Mobile SDK API is available at [https://legal.yandex.ru/speechkit/](https://legal.yandex.ru/speechkit/).

License agreement on use of Yandex SpeechKit Cloud is available at [https://legal.yandex.ru/speechkit_cloud/](https://legal.yandex.ru/speechkit_cloud/).

-------------

**Яндекс.Разговор** - мобильное приложение для глухих и слабослышащих людей.

Что это за проект
-----------------
Мы пытаемся решить проблему общения глухих и слабослышащих людей с окружающими: родственниками, друзьями, знакомыми и незнакомыми людьми.

Мобильное приложение [Разговор](https://mobile.yandex.ru/apps/android/talk#main) переводит речь в текст и, наоборот, может озвучить написанное. Для распознавания и синтеза речи используются  [речевые технологии Яндекса](https://tech.yandex.ru/speechkit/). В приложении также есть набор готовых реплик и история диалогов, возможность использовать увеличенный размер шрифта. 

Готовое приложение для платформы Android можно скачать в [Google Play](https://play.google.com/store/apps/details?id=ru.yandex.subtitles).

В этом репозитории находится Android Studio проект.

Как начать
----------
Если вы хотите создать Android-приложение на основе Яндекс.Разговора:

1. Клонируйте репозиторий проекта:

<[git clone https://github.com/mobile-subtitles-android.git.]>

2. Получите бесплатные API-ключи для [SpeechKit Mobile SDK](https://tech.yandex.ru/speechkit/mobilesdk/) и [SpeechKit Cloud API](https://tech.yandex.ru/speechkit/cloud/). Обратите, пожалуйста, внимание, что существуют ограничения на количество обращений.

3. Откройте проект в Android Studio.

4. Откройте файл SpeechKitInitializer.java и замените значение константы API_KEY ключом для Mobile SDK, который вы получили в [Кабинете разработчика](https://developer.tech.yandex.ru/). Также откройте файл SpeechKitTtsCloudApi.java и замените значение константы API_KEY ключом, полученным для SpeechKit Cloud. В основном приложение использует Mobile SDK, но Cloud API используется для того, чтобы обеспечить озвучивание ранее сохраненных фраз без подключения к интернету.

5. Чтобы отслеживать активность пользователей в приложении, активируйте YandexMetricaEventTracker или реализуйте интерфейс EventTracker. Дополнительная информация есть в описании класса SubtitlesApplication.

Если вы хотите внести свой вклад в развитие приложения Яндекс.Разговор, отправляйте pull request в ветку master, с детальным описанием изменений.

Что необходимо
--------------
* Android Studio версии 1.2 и выше,
* Android SDK, уровень API 16—23,
* Android Support Repository версии 24,
* Android Support Library версии 23.1,
* Build Tools версии 21.1.2.

Как это устроено
----------------
Приложение построено на работе со [службами](http://developer.android.com/intl/ru/guide/components/services.html), в соответствии со схемой: Activity > Service > ContentProvider. Действия пользователя отправляются из Activity > Fragment в службу и обрабатываются службой.

Служба также обеспечивает асинхронную запись в базу данных. Выгрузка из базы данных контролируется Loader, различные реализации которого получают уведомления, когда изменяются данные. Ряд событий отправляется из службы broadcast-сообщениями. За работу с базой данных и поставщиком контента (ContentProvider) отвечают классы пакета content. 

Для служб используются классы:

PhrasesService — служба, отвечающая за обработку следующих событий: добавление, редактирование и удаление стартовых фраз. Также управляет обновлением образцов речи.

MessagingService - служба для управления событиями мессенджинга и распознавания.

Что читать
----------
* Дополнительную информацию о приложении Разговор можно найти в Wiki.
* Документация проекта (сгенерированная JavaDoc) находится в mobile-subtitles-android/doc. Справочник содержит описание основных классов и методов.
* Информацию о работе с поставщиком контента ContentProvider можно найти в [официальной документации](http://developer.android.com/intl/ru/guide/topics/providers/content-provider-basics.html ). 

Лицензия
--------
Лицензия на исходный код приложения Разговор находится в файле LICENSE в репозитории.

Лицензионное соглашение [Яндекс SpeechKit Mobile SDK](https://legal.yandex.ru/speechkit/) и [SpeechKit Cloud API](https://legal.yandex.ru/speechkit_cloud/).
