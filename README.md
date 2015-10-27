**Talk**

[Talk](https://mobile.ru/apps/android/talk#page1) is a helper for personal communication between hearing loss and hearing people. The application translates collocutor's speech into text and can play written text on the contrary. Moreover, the application has a number of features that make communication with Talk quick and easy: phrase enlarging, template phrases and conversatoin history.

If you're looking to install Talk for Android, you can find it on [Google Play](https://play.google.com/store/apps/details?id=ru.subtitles). If you're a developer wanting to contribute, read on.


How to start
------------
*  Clone repository: git clone https://github.com/mobile-subtitles-android.git.
*  Import project to **Android Studio**.
*  App is based on Yandex.SpeechKit technology and you should register your Mobile SDK API key at [Yandex.SpeechKit Mobile SDK documentation](https://tech.yandex.com/speechkit/mobilesdk/).
*  Open class [SpeechKitInitializer.java](https://github.com/mobile-subtitles-android/blob/master/app/src/main/java/ru/subtitles/service/speechkit/initializer/SpeechKitInitializer.java) and replace `API_KEY` value by registered Mobile SDK API key.
*  Also you should provide API key for Yandex.SpeechKit Cloud. Register your Yandex.SpeechKit Cloud API key at [Yandex.SpeechKit Cloud documentation](https://tech.yandex.ru/speechkit/cloud/).
*  Open class [SpeechKitTtsCloudApi.java](https://github.com/mobile-subtitles-android/blob/master/app/src/main/java/ru/subtitles/service/cache/SpeechKitTtsCloudApi.java) and replace `API_KEY` value by registered Cloud API key.
*  If you want to track user activity in application you should enable `YandexMetricaEventTracker` or provide your own `EventTracker` implementation. Please see [SubtitlesApplication.java](https://github.com/mobile-subtitles-android/blob/master/app/src/main/java/ru/subtitles/SubtitlesApplication.java) for more details.
*  Now you are ready to launch project!

How does it work
----------------
Application architecture design based on the following principle: `Activity` > `Service` > `ContentProvider`. `Service` receives and executes user actions that sent from `Activity`/`Fragment`. `Service` also executes async writing to the database when it's needed. Data loads from database by using `Loader` classes that get callbacks when data identified by a given content URI changes. Some events send from `Service` by using broadcast messages.

There are two services to work with different kinds of data:
* `PhrasesService` is a service that handles additing, editing and deleting starting phrases. Also service dispatches events to invalidate audio samples.
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

License agreement on use of Talk is available at [https://legal.ru/talk_mobile_agreement](https://legal.ru/talk_mobile_agreement).

-------------

**Разговор**

[Разговор](https://mobile.ru/apps/android/talk#page1) – помощник в личном общении между людьми с потерей слуха и слышащими. Приложение переводит сказанное собеседником в текст, и наоборот, может озвучить написанное. Кроме того, в приложении есть ряд функций, которые делают общение с помощью Разговора быстрым и удобным: увеличивание фраз, шаблонные фразы и история диалогов.

Если вы хотите установить Разговор на Android, то можете найти его на [Google Play](https://play.google.com/store/apps/details?id=ru.subtitles). Если же вы разработчик, который хочет принять участие в развитии проекта, то продолжайте чтение.


Как начать
----------
*  Клонируйте репозиторий проекта: git clone https://github.com/mobile-subtitles-android.git.
*  Импортируйте проект в **Android Studio**.
*  Приложение основано на технологии Yandex.SpeechKit и Вы должны зарегистрировать свой API ключ для работы с Mobile SDK. [Документация Yandex.SpeechKit Mobile SDK](https://tech.yandex.ru/speechkit/mobilesdk/).
*  Откройте класс [SpeechKitInitializer.java](https://github.com/mobile-subtitles-android/blob/master/app/src/main/java/ru/subtitles/service/speechkit/initializer/SpeechKitInitializer.java) и замените значение константы `API_KEY` ключом, полученным в предыдущем пункте.
*  Также Вы должны получить API ключ для работы с Yandex.SpeechKit Cloud. Получить ключ можно здесь: [Документация Yandex.SpeechKit Cloud](https://tech.yandex.ru/speechkit/cloud/).
*  Откройте класс [SpeechKitTtsCloudApi.java](https://github.com/mobile-subtitles-android/blob/master/app/src/main/java/ru/subtitles/service/cache/SpeechKitTtsCloudApi.java) и замените значение константы `API_KEY` ключом, полученным в предыдущем пункте.
*  Если Вы хотите отслеживать активность пользователей в приложении Вы должны активировать `YandexMetricaEventTracker` или предоставить свою собственную реализацию интерфейса `EventTracker`. Дополнительная информация находится в файле [SubtitlesApplication.java](https://github.com/mobile-subtitles-android/blob/master/app/src/main/java/ru/subtitles/SubtitlesApplication.java).
*  Теперь Вы готовы к запуску проекта!

Как это работает
----------------
Приложение построено в соответствии со следующим принципом: `Activity` > `Service` > `ContentProvider`. Пользовательские действия отправляются на выполнение из `Activity`/`Fragment` в `Service` и там обрабатываются. Если какие-то данные требуется сохранить в базу данных, то `Service` организует асинхронную запись в БД. Из БД данные загружаются при помощи `Loader`, реализации которого получают уведомления, когда изменяются данные, идентифицированные предоставленным content uri. Ряд событий отправляется из `Service` broadcast-сообщениями.

В приложении имеется два сервиса для работы с разными данными:
* `PhrasesService` - Service, занимающийся обработкой событий добавления, редактирования и удаления стартовых фраз. Дополнительно сервис управляет обновлением предзаписанных голосовых семплов.
* `MessagingService` - Service, занимающийся диспетчеризацией событий мессенджинга и распознавания.

За работу с базой данных и `ContentProvider`-ом отвечают классы пакета [content](https://github.com/mobile-subtitles-android/blob/master/app/src/main/java/ru/subtitles/content/). Для получения дополнительной информации по работе с `ContentProvider` рекомендуем обраться к [официальной документации](http://developer.android.com/intl/ru/guide/topics/providers/content-provider-basics.html).

Дополнительная информация
-------------------------

Также у этого проекта есть:
* [Wiki](https://github.com/mobile-subtitles-android/wiki), в которой содержится полезная информация о приложении.
* JavaDoc, который находится в mobile-subtitles-android/doc. В нем содержится описание базовых классов и методов приложения.

Если вы хотите сообщить об ошибке или предложить идею в развитии, то напишите об этом, пожалуйста, в [Issues](https://github.com/mobile-subtitles-android/issues).

Лицензия
--------

Лицензионное соглашение по использованию Разговора доступно по ссылке [https://legal.ru/talk_mobile_agreement](https://legal.ru/talk_mobile_agreement).
