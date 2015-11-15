**Talk** - a mobile app for deaf and hard of hearing people.

About this project
------------------
Our goal is to make it easier for individuals who are deaf or have hearing loss to communicate with the people around them: relatives, friends, acquaintances, and strangers.

The [Talk](https://mobile.yandex.ru/apps/android/talk#main) mobile app converts speech to text and can also produce spoken audio of written text. It uses [Yandex speech technologies](https://tech.yandex.ru/speechkit/) for speech recognition and synthesis. The app also includes a set of ready-to-use phrases, a history of conversations, and a large font feature.

The app for the Android platform can be downloaded from [Google Play](https://play.google.com/store/apps/details?id=ru.yandex.subtitles). 

This repository contains the Android Studio project.

How to get started
------------------
If you want to create an Android app based on Yandex.Talk:

1. Clone the project's repositories: `git clone https://github.com/mobile-subtitles-android.git`

2. Get free API keys for the [SpeechKit Mobile SDK](https://tech.yandex.com/speechkit/mobilesdk/) and the [SpeechKit Cloud API](https://tech.yandex.com/speechkit/cloud/). Please note that there are limits on the number of requests.

3. Open the project in Android Studio.

4. Open the SpeechKitInitializer.java file and replace the value of the API_KEY constant with the key for the Mobile SDK that was issued to you in the [Developer's Dashboard](https://developer.tech.yandex.ru/). Likewise, open the SpeechKitTtsCloudApi.java file and replace the value of the API_KEY constant with the key that you received for SpeechKit Cloud. The app mainly uses the Mobile SDK, but it uses the Cloud API for voicing previously saved texts without connecting to the internet.

5. To track user activity in the app, activate YandexMetricaEventTracker or implement the EventTracker interface. For more information, see the description of the SubtitlesApplication class.

If you want to contribute to the development of Yandex.Talk, submit a pull request on the master branch with a detailed explanation of changes.

What you need
-------------
* Android Studio version 1.2 and later
* Android SDK, API level 16—23
* Android Support Repository version 24
* Android Support Library version 23.1
* Build Tools version 21.1.2

How it works
------------
The app is built on interaction with [services](http://developer.android.com/intl/ru/guide/components/services.html), using the flow Activity > Service > ContentProvider. User actions are sent from Activity > Fragment to the service and processed by the service.

The service also provides asynchronous writes to the database. Loading from the database is controlled by the Loader. Various implementations of the Loader receive notifications when data is changed. A number of events are broadcast from the service. The "content" classes are responsible for interaction with the database and ContentProvider. 

The following classes are used for services:

PhrasesService — a service responsible for handling the following events: adding, editing, and deleting the pre-defined phrases. It also manages speech sample updates.

MessagingService - a service for managing messaging and recognition events.

What to read
------------
* You can find more information about the Yandex.Talk app in the Wiki.
* Project documentation (generated JavaDoc) is in mobile-subtitles-android/doc. The reference doc contains descriptions of the main classes and methods.
* For information about working with ContentProvider, see the [official documentation](http://developer.android.com/guide/topics/providers/content-provider-basics.html).

Licensing
---------
The license for the source code of the Yandex.Talk app is in the LICENSE file in the repository.

Licensing agreements: [Yandex SpeechKit Mobile SDK](https://legal.yandex.ru/speechkit/) and [Yandex SpeechKit Cloud API](https://legal.yandex.ru/speechkit_cloud/)

Contacts
--------
If you want to ask a question, report an error, or suggest a new idea, send a message to ((Issues)).

--------------------

**Разговор** - мобильное приложение для глухих и слабослышащих людей.

Что это за проект
-----------------
Мы пытаемся решить проблему общения глухих и слабослышащих людей с окружающими: родственниками, друзьями, знакомыми и незнакомыми людьми.

Мобильное приложение [Разговор](https://mobile.yandex.ru/apps/android/talk#main) переводит речь в текст и, наоборот, может озвучить написанное. Для распознавания и синтеза речи используются  [речевые технологии Яндекса](https://tech.yandex.ru/speechkit/). В приложении также есть набор готовых реплик и история диалогов, возможность использовать увеличенный размер шрифта. 

Готовое приложение для платформы Android можно скачать в [Google Play](https://play.google.com/store/apps/details?id=ru.yandex.subtitles).

В этом репозитории находится Android Studio проект.

Как начать
----------
Если вы хотите создать Android-приложение на основе Яндекс.Разговора:

1. Клонируйте репозиторий проекта: `git clone https://github.com/mobile-subtitles-android.git`

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

Лицензионное соглашение [Yandex SpeechKit Mobile SDK](https://legal.yandex.ru/speechkit/) и [Yandex SpeechKit Cloud API](https://legal.yandex.ru/speechkit_cloud/).

Контакты
--------
Если вы хотите задать вопрос, сообщить об ошибке или предложить новую идею, напишите, пожалуйста, в ((Issues)).