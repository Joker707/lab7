# Цели

Получить практические навыки разработки сервисов (started и bound) и Broadcast Receivers.

## Задача 1 - Started сервис для скачивания изображения

Для решения данной задачи возьмем код с корутинами, скачивающий картинку из интернета, разработанный
в лабораторной работе №6.

Ниже приведены листинги Активити и Сервиса по скачиванию файла из интернета,
который запускается при нажатии на кнопку:

__Листинг 1.1 - MainActivity.kt__

    const val keyURL = "URL"
    
    class MainActivity : AppCompatActivity() {
        private val url = "https://singulartm.com/wp-content/uploads/2020/10/Registro-marca-de-la-UE.jpg"
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
    
            button_start.setOnClickListener {
                startService(Intent(this, Service1::class.java).putExtra(keyURL, url))
            }
        }
    }

__Листинг 1.2 - Service1.kt__

    const val keyPATH = "PATH"
    
    class Service1 : Service() {
    
        private var mIcon11: Bitmap? = null
        private var job: Job? = null
    
        override fun onBind(p0: Intent?): IBinder? {
            return null
        }
    
        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            val url = intent?.getStringExtra(keyURL)
    
            if (url != null) {
                job = CoroutineScope(Dispatchers.IO).launch {
                    Log.i("Testing", "Service is running on " +
                            Thread.currentThread().name + " and it's  not UI")
                    val path = getPathAfterDownload(url)
                    Log.i("Testing","Image located in $path")
    
                    sendBroadcast(Intent("IMAGE_DOWNLOADED").putExtra(keyPATH, path))
                }
            }
            stopSelf()
    
            return START_NOT_STICKY
        }
    
        private fun getPathAfterDownload(url : String): String? {
            val name = "file${Random.nextInt()}"
            try {
                val input: InputStream = URL(url).openStream()
                mIcon11 = BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                Log.e("Error", e.message.toString())
                e.printStackTrace()
            }
            openFileOutput(name, MODE_PRIVATE).use {
                mIcon11?.compress(Bitmap.CompressFormat.JPEG, 75, it)
            }
            return File(filesDir, name).absolutePath
        }
    
        override fun onCreate() {
            super.onCreate()
        }
    
        override fun onDestroy() {
            super.onDestroy()
            job?.cancel()
        }
        
    }

__Листинг 1.3 - Описание сервиса через Манифест__

    <service
        android:name=".Service1"
        android:enabled="true"
        android:exported="true">
    </service>

По сути нужно просто скачать картинку из интернета и сохранить её на девайсе без отображения на экране,
поэтому мы через лог будет мониторить, что у нас появляется путь и что-то вообще происходит после нажатия на кнопку :)

__Вкратце расскажу про работу кода:__

- Запиливаем ссылку на изображение в переменную
- В методе `onCreate` мы запускаем сервис при нажатии на кнопку с помощью метода `startService`
- В него мы передаем интент, в который в свою очередь мы запихиваем наш URL в поле Extra

__Касательно самого Сервиса:__

- Зануляем метод `onBind`, потому что у нас здесь обычный started Service
- В `onStartCommand` мы используем корутину для скачивания картинки: из переданного в этот метод интента
мы достаем URL по ключу и передаем его в качестве параметра в функцию `getPathAfterDownload`
- Эту функцию мы большую часть копируем из прошлой лабораторной работы, добавив только пару моментов:
Мы для каждого файла делаем "уникальное" имя через Рандом и потом используем его в качестве параметра
для сохранения картинки во внутреннее хранилище с помощью `Bitmap.compress` в `openFileOutput`
- Из функции `getPathAfterDownload` мы возвращаем путь к скачанному файлу через `File(filesDir, name).absolutePath`
(с помощью filesDir мы как раз и получаем путь к внутреннему хранилищу, куда мы загружали файл через `openFileOutput`)
- Возвращаем из `onStartCommand` мы `START_NOT_STICKY`, то есть сервис не будет перезапущен после того, как его убет система


## Задача 2 - Broadcast Receiver

В данном задании нам нужно было добавить Ресивер для того, чтобы принимать бродкаст от Сервиса и
Активити с одним текстовым полем для того, чтобы после того, как Ресивер принял бродкаст, положить 
в текстовое поло путь к загруженному файлу.

Ниже представлены листинги Активити и Ресивера:
 
__Листинг 2.1 - ActivityForReceive.kt__

    class ActivityForReceive : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val path = intent.getStringExtra(keyPATH)
    
    
            val filter = IntentFilter()
            filter.addAction("IMAGE_DOWNLOADED")
            registerReceiver(Receiver(), filter)
            if (path != null) {
                receivers_url.text = "Our image's path is $path"
            }
            setContentView(R.layout.receive_activity)
        }
    }

__Листинг 2.2 - Receiver.kt__

    class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
    
            val path = intent?.getStringExtra(keyPATH)
    
            context?.startActivity(Intent(context, ActivityForReceive::class.java).putExtra(keyPATH, path))
        }
    }

По сути, здесь все достаточно просто:

- Наш Ресивер принимает бродкаст от Сервиса из 1 таска
- В методе `onReceive`, который вызывается при получении бродкаста, мы получаем путь к файлу по ключу
из интента, и далее мы запускаем нашу Активити с текстовым полем, прикрепляя в _Extra_ к интенту путь к нашему файлу
- В самой активити мы получаем наш путь по ключу из интента
- Регистрируем наш Ресивер(Андрей Николаевич советовал нам регистрировать в коде, а не в Манифесте)
Хотя закомменченный код регистрации Ресивера через Манифест тоже присутствует(решил оставить и не удалять)
- Проверяем, если не пустой путь, значит заменяем наше текстовое поле на путь к файлу



## Задача 3 - Bound Service для скачивания изображения

Здесь нам нужно было сделать два сервиса в одном приложении: Started и Bound 
- Для первого у нас будет та же кнопка для запуска Started Service, его сообщение принимать будет все тот же Ресивер
- Для второго мы сами же будем принимать резульатт


__Листинг 3.1 - MainActivity.kt (полная версия кода)__

    const val keyURL = "URL"
    
    class MainActivity : AppCompatActivity() {
        private val url = "https://singulartm.com/wp-content/uploads/2020/10/Registro-marca-de-la-UE.jpg"
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
    
            var messenger = Messenger(HandlerActivity())
            bindService(Intent(this, Messenger3::class.java), connection, Context.BIND_AUTO_CREATE)
    
            button_start.setOnClickListener {
                startService(Intent(this, Service1::class.java).putExtra(keyURL, url))
            }
            button_bind.setOnClickListener {
                Message.obtain().apply {
                    obj = url
                    replyTo = messenger
                    what = 1
                    connection.serviceMessenger?.send(this)
                }
            }
        }
    
        @SuppressLint("HandlerLeak")
        inner class HandlerActivity : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    1 -> message.text = msg.obj.toString()
                    else -> super.handleMessage(msg)
                }
    
            }
        }
    
        private val connection = object : ServiceConnection {
            var serviceMessenger : Messenger? = null
    
            override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
                serviceMessenger = Messenger(service)
            }
    
            override fun onServiceDisconnected(className: ComponentName?) {
                serviceMessenger = null
            }
        }
    
        override fun onDestroy() {
            super.onDestroy()
            unbindService(connection)
        }
    }

__Листинг 3.2 - Messenger3.kt__

    class Messenger3 : Service() {
    
        private var mIcon11: Bitmap? = null
        private var job: Job? = null
    
    
        override fun onBind(p0: Intent?): IBinder? {
            return Messenger(HandlerMessenger()).binder
        }
    
        @SuppressLint("HandlerLeak")
        inner class HandlerMessenger : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    1 -> {
                        val replyTo = msg.replyTo
                        val url = msg.obj.toString()
                        job = CoroutineScope(Dispatchers.IO).launch {
                            val message = Message.obtain(null, 1)
                            message.obj = getPathAfterDownload(url)
                            replyTo.send(message)
                        }
                    }
                    else -> super.handleMessage(msg)
                }
            }
        }
    
        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            val url = intent?.getStringExtra(keyURL)
    
            if (url != null) {
                job = CoroutineScope(Dispatchers.IO).launch {
                    Log.i("Testing", "Service is running on " +
                            Thread.currentThread().name + " which is not UI")
                    val path = getPathAfterDownload(url)
    
                    sendBroadcast(Intent("IMAGE_DOWNLOADED").putExtra(keyPATH, path))
                }
            }
            stopSelf()
    
            return START_NOT_STICKY
        }
    
        private fun getPathAfterDownload(url : String): String? {
            val name = "file${Random.nextInt(100000)}"
            try {
                val input: InputStream = URL(url).openStream()
                mIcon11 = BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                Log.e("Error", e.message.toString())
                e.printStackTrace()
            }
            openFileOutput(name, MODE_PRIVATE).use {
                mIcon11?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            return File(filesDir, name).absolutePath
        }
    
    
        override fun onDestroy() {
            super.onDestroy()
            job?.cancel()
        }
    }


__Порараз бирацца:__

- Добавляем в изначальную активити текстовое поле и ещё одну кнопку `Bind` для привязки к сервису
- По сути у нас не сильно отличается код двух классов - используются все те же методы, что уже были в предыдущих классах
- Добавлены два `Handler` для Активити и Messenger: первый создается для того, чтобы ловить ответы от сервиса, 
а второй принимает сообщение с URL картинки
- Изменили метод `onBind` на получение объекта IBinder, чтобы наш сервис был Bound, а не только Started
- Добавлен интерфейс ServiceConnection в Activity. 
Он устанавливает связь с сервисом(при нажатии кнопки `Bind` в данном случае)


# Выводы

Итого!!
Получается, мы завершили все лабораторные работы!
Последняя была не менее интересной, чем две последних!
Почему-то мне под конец реально стало интересно создавать что-то такое, в чем я вижу реальное применение...
Возможно, это меня как-то замотивирует развиваться в этой штуке дальше, как минимум на летних каникулах!
Что касательно этой лабораторной то было интереснен тот факт, что здесь все задания дополняли друг друга!!
Хоть и немного сложно было сначала вникнуть вообще в эти сервисы и в их методы...
Пришлось даже пересматривать лекцию после того, как я начал что-то делать в лабе
Ибо у меня уже какие-то практические навыки появились, и для меня теперь все эти методы - не просто набор слов, я ведь их юзал уже!
В обещем потратил на эту работу денечка три-четыре из-за сложности в восприятии теории :(