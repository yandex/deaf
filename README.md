**Subtitles**

How to start
------------
*  Clone repository: git clone https://github.com/yandexmobile/mobile-yandex-subtitles-android.git.
*  Import project to **Android Studio**.
*  App is based on Yandex.SpeechKit technology and you should register your Mobile SDK API key at [Yandex.SpeechKit Mobile SDK documentation](https://tech.yandex.com/speechkit/mobilesdk/).
*  Open class [SpeechKitInitializer.java](https://github.com/yandexmobile/mobile-yandex-subtitles-android/blob/master/app/src/main/java/ru/yandex/subtitles/service/speechkit/initializer/SpeechKitInitializer.java) and replace `API_KEY` value by registered Mobile SDK API key.
*  Also you should provide API key for Yandex.SpeechKit Cloud. Register your Yandex.SpeechKit Cloud API key at [Yandex.SpeechKit Cloud documentation](https://tech.yandex.ru/speechkit/cloud/).
*  Open class [SpeechKitTtsCloudApi.java](https://github.com/yandexmobile/mobile-yandex-subtitles-android/blob/master/app/src/main/java/ru/yandex/subtitles/service/cache/SpeechKitTtsCloudApi.java) and replace `API_KEY` value by registered Cloud API key.
*  If you want to track user activity in application you should enable `YandexMetricaEventTracker` or provide your own `EventTracker` implementation. Please see [SubtitlesApplication.java](https://github.com/yandexmobile/mobile-yandex-subtitles-android/blob/master/app/src/main/java/ru/yandex/subtitles/SubtitlesApplication.java) for more details.
*  Now you are ready to launch project!

How does it work
------------
Application architecture design based on the following principle: Activity > Service > ContentProvider. Service receives and executes user actions that sent from Activity/Fragment. Service also executes async writing to the database when it's needed.

License
---------

License agreement on use of Yandex.Subtitles is available at [https://legal.yandex.ru/talk_mobile_agreement](https://legal.yandex.ru/talk_mobile_agreement).

-------------

**Разговор**

Как начать
------------
*  Клонируйте репозиторий проекта: git clone https://github.com/yandexmobile/mobile-yandex-subtitles-android.git.
*  Импортируйте проект в **Android Studio**.
*  Приложение основано на технологии Yandex.SpeechKit и Вы должны зарегистрировать свой API ключ для работы с Mobile SDK. [Документация Yandex.SpeechKit Mobile SDK](https://tech.yandex.ru/speechkit/mobilesdk/).
*  Откройте класс [SpeechKitInitializer.java](https://github.com/yandexmobile/mobile-yandex-subtitles-android/blob/master/app/src/main/java/ru/yandex/subtitles/service/speechkit/initializer/SpeechKitInitializer.java) и замените значение константы `API_KEY` ключом, полученным в предыдущем пункте.
*  Также Вы должны получить API ключ для работы с Yandex.SpeechKit Cloud. Получить ключ можно здесь: [Документация Yandex.SpeechKit Cloud](https://tech.yandex.ru/speechkit/cloud/).
*  Откройте класс [SpeechKitTtsCloudApi.java](https://github.com/yandexmobile/mobile-yandex-subtitles-android/blob/master/app/src/main/java/ru/yandex/subtitles/service/cache/SpeechKitTtsCloudApi.java) и замените значение константы `API_KEY` ключом, полученным в предыдущем пункте.
*  Если Вы хотите отслеживать активность пользователей в приложении Вы должны активировать `YandexMetricaEventTracker` или предоставить свою собственную реализацию интерфейса `EventTracker`. Дополнительная информация находится в файле [SubtitlesApplication.java](https://github.com/yandexmobile/mobile-yandex-subtitles-android/blob/master/app/src/main/java/ru/yandex/subtitles/SubtitlesApplication.java).
*  Теперь Вы готовы к запуску проекта!

Как это работает
------------
Приложение построено в соответствии со следующим принципом: `Activity` > `Service` > `ContentProvider`. Пользовательские действия отправляются на выполнение из `Activity`/`Fragment` в `Service` и там обрабатываются. Если какие-то данные требуется сохранить в базу данных, то `Service` организует асинхронную запись в БД. Из БД данные загружаются при помощи `Loader`, реализации которого подписываются на уведомления об изменении данных через `ContentResolver`. Ряд событий отправляется из `Service` broadcast-сообщениями.

В приложении имеется два сервиса для работы с разными данными:
* `PhrasesService` - Service, занимающийся обработкой событий добавления, редактирования и удаления стартовых фраз. Дополнительно сервис управляет обновлением предзаписанных голосовых семплов.
* `MessagingService` - Service, занимающийся диспетчеризацией событий мессенджинга и распознавания.

За работу с базой данных и `ContentProvider`-ом отвечают классы пакета [content](https://github.com/yandexmobile/mobile-yandex-subtitles-android/blob/master/app/src/main/java/ru/yandex/subtitles/content/).

Лицензия
---------

Лицензионное соглашение по использованию Яндекс.Разговора available at [https://legal.yandex.ru/talk_mobile_agreement](https://legal.yandex.ru/talk_mobile_agreement).
