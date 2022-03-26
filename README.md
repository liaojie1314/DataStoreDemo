# DataStoreDemo

## DataStore   Android Jetpack 的一部分。

Jetpack DataStore 是一种数据存储解决方案，允许您使用协议缓冲区存储键值对或类型化对象。DataStore 使用 Kotlin 协程和 Flow 以异步、一致的事务方式存储数据。

如果您当前在使用 SharedPreferences 存储数据，请考虑迁移到 DataStore。
注意：如果您需要支持大型或复杂数据集、部分更新或参照完整性，请考虑使用 Room，而不是 DataStore。DataStore 非常适合简单的小型数据集，不支持部分更新或参照完整性。

### Preferences DataStore 和 Proto DataStore

DataStore 提供两种不同的实现：Preferences DataStore 和 Proto DataStore。

+ Preferences DataStore 使用键存储和访问数据。此实现不需要预定义的架构，也不确保类型安全。
+ Proto DataStore 将数据作为自定义数据类型的实例进行存储。此实现要求您使用协议缓冲区来定义架构，但可以确保类型安全。

### 设置

如需在您的应用中使用 Jetpack DataStore，请根据您要使用的实现向 Gradle 文件添加以下内容：
类型化 Datastore
```Groovy
    // Typed DataStore (Typed API surface, such as Proto)
    dependencies {
        implementation "androidx.datastore:datastore:1.0.0"

        // optional - RxJava2 support
        implementation "androidx.datastore:datastore-rxjava2:1.0.0"

        // optional - RxJava3 support
        implementation "androidx.datastore:datastore-rxjava3:1.0.0"
    }

    // Alternatively - use the following artifact without an Android dependency.
    dependencies {
        implementation "androidx.datastore:datastore-core:1.0.0"
    }
```  

Datastore Preferences
```Groovy
    // Preferences DataStore (SharedPreferences like APIs)
    dependencies {
        implementation "androidx.datastore:datastore-preferences:1.0.0"

        // optional - RxJava2 support
        implementation "androidx.datastore:datastore-preferences-rxjava2:1.0.0"

        // optional - RxJava3 support
        implementation "androidx.datastore:datastore-preferences-rxjava3:1.0.0"
    }

    // Alternatively - use the following artifact without an Android dependency.
    dependencies {
        implementation "androidx.datastore:datastore-preferences-core:1.0.0"
    }
```  

>注意：如果您将 datastore-preferences-core 工件与 Proguard 搭配使用，就必须手动将 Proguard 规则添加到 proguard-rules.pro 文件中，以免您的字段遭到删除。您可以点击此处查找必要的规则。

### 使用 Preferences DataStore 存储键值对

Preferences DataStore 实现使用 DataStore 和 Preferences 类将简单的键值对保留在磁盘上。

**创建 Preferences DataStore**

使用由 preferencesDataStore 创建的属性委托来创建 Datastore<Preferences> 实例。在您的 Kotlin 文件顶层调用该实例一次，便可在应用的所有其余部分通过此属性访问该实例。这样可以更轻松地将 DataStore 保留为单例。此外，如果您使用的是 RxJava，请使用 RxPreferenceDataStoreBuilder。必需的 name 参数是 Preferences DataStore 的名称。

```Kotlin
// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
```

#### 从 Preferences DataStore 读取内容

由于 Preferences DataStore 不使用预定义的架构，因此您必须使用相应的键类型函数为需要存储在 DataStore<Preferences> 实例中的每个值定义一个键。例如，如需为 int 值定义一个键，请使用 intPreferencesKey()。然后，使用 DataStore.data 属性，通过 Flow 提供适当的存储值。

```Kotlin

val EXAMPLE_COUNTER = intPreferencesKey("example_counter")
val exampleCounterFlow: Flow<Int> = context.dataStore.data
  .map { preferences ->
    // No type safety.
    preferences[EXAMPLE_COUNTER] ?: 0
}
```

#### 将内容写入 Preferences DataStore

Preferences DataStore 提供了一个 edit() 函数，用于以事务方式更新 DataStore 中的数据。该函数的 transform 参数接受代码块，您可以在其中根据需要更新值。转换块中的所有代码均被视为单个事务。
```Kotlin
suspend fun incrementCounter() {
  context.dataStore.edit { settings ->
    val currentCounterValue = settings[EXAMPLE_COUNTER] ?: 0
    settings[EXAMPLE_COUNTER] = currentCounterValue + 1
  }
}
```
#### 使用 Proto DataStore 存储类型化的对象

Proto DataStore 实现使用 DataStore 和协议缓冲区将类型化的对象保留在磁盘上。
定义架构

Proto DataStore 要求在 `app/src/main/proto/` 目录的 proto 文件中保存预定义的架构。此架构用于定义您在 Proto DataStore 中保存的对象的类型。如需详细了解如何定义 proto 架构，请参阅 protobuf 语言指南。

```
syntax = "proto3";

option java_package = "com.example.application";
option java_multiple_files = true;

message Settings {
  int32 example_counter = 1;
}
```
>注意：您的存储对象的类在编译时由 proto 文件中定义的 message 生成。请务必重新构建您的项目。
#### 创建 Proto DataStore

创建 Proto DataStore 来存储类型化对象涉及两个步骤：

1.定义一个实现 Serializer<T> 的类，其中 T 是 proto 文件中定义的类型。此序列化器类会告知 DataStore 如何读取和写入您的数据类型。请务必为该序列化器添加默认值，以便在尚未创建任何文件时使用。
2.使用由 dataStore 创建的属性委托来创建 DataStore<T> 的实例，其中 T 是在 proto 文件中定义的类型。在您的 Kotlin 文件顶层调用该实例一次，便可在应用的所有其余部分通过此属性委托访问该实例。filename 参数会告知 DataStore 使用哪个文件存储数据，而 serializer 参数会告知 DataStore 第 1 步中定义的序列化器类的名称。

```Kotlin
object SettingsSerializer : Serializer<Settings> {
  override val defaultValue: Settings = Settings.getDefaultInstance()

  override suspend fun readFrom(input: InputStream): Settings {
    try {
      return Settings.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
      throw CorruptionException("Cannot read proto.", exception)
    }
  }

  override suspend fun writeTo(
    t: Settings,
    output: OutputStream) = t.writeTo(output)
}

val Context.settingsDataStore: DataStore<Settings> by dataStore(
  fileName = "settings.pb",
  serializer = SettingsSerializer
)
```
#### 从 Proto DataStore 读取内容

使用 `DataStore.data` 显示所存储对象中相应属性的 `Flow`。
```Kotlin
val exampleCounterFlow: Flow<Int> = context.settingsDataStore.data
  .map { settings ->
    // The exampleCounter property is generated from the proto schema.
    settings.exampleCounter
  }
```
#### 将内容写入 Proto DataStore

Proto DataStore 提供了一个 updateData() 函数，用于以事务方式更新存储的对象。updateData() 为您提供数据的当前状态，作为数据类型的一个实例，并在原子读-写-修改操作中以事务方式更新数据。
```Kotlin
suspend fun incrementCounter() {
  context.settingsDataStore.updateData { currentSettings ->
    currentSettings.toBuilder()
      .setExampleCounter(currentSettings.exampleCounter + 1)
      .build()
    }
}
```
#### 在同步代码中使用 DataStore
>注意：请尽可能避免在 DataStore 数据读取时阻塞线程。阻塞界面线程可能会导致 ANR 或界面卡顿，而阻塞其他线程可能会导致死锁。

DataStore 的主要优势之一是异步 API，但可能不一定始终能将周围的代码更改为异步代码。如果您使用的现有代码库采用同步磁盘 I/O，或者您的依赖项不提供异步 API，就可能出现这种情况。

Kotlin 协程提供 runBlocking() 协程构建器，以帮助消除同步与异步代码之间的差异。您可以使用 runBlocking() 从 DataStore 同步读取数据。RxJava 在 Flowable 上提供阻塞方法。以下代码会阻塞发起调用的线程，直到 DataStore 返回数据：

```Kotlin
val exampleData = runBlocking { context.dataStore.data.first() }
```

对界面线程执行同步 I/O 操作可能会导致 ANR 或界面卡顿。您可以通过从 DataStore 异步预加载数据来减少这些问题：

```Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    lifecycleScope.launch {
        context.dataStore.data.first()
        // You should also handle IOExceptions here.
    }
}
```

这样，DataStore 可以异步读取数据并将其缓存在内存中。以后使用 runBlocking() 进行同步读取的速度可能会更快，或者如果初始读取已经完成，可能也可以完全避免磁盘 I/O 操作。

## 那么DataStore 和 SharedPreferences 有什么区别呢？

SharedPreferences 初始化的时候，会将整个文件内容加载到内存当中因此会带来以下问题：

1.通过 getXXX()获取数据时，可能会导致主线程阻塞

2.SharedPreferences 不能保证类型安全。
使用相同的 key 进行操作时，我们可以put 不同类型的数据覆盖掉相同的 key。在获取数据时就会出现ClassCastException

3.SharedPreferences 加载的数据会一起留存内存中，浪费内存

## 官方推出Preferences DataStore 主要用来替换SharedPreferences, Preferences DataStore 解决了SharedPreferences 带来的所有问题Preferences DataStore 相比于 SharedPreferences 优点

1.DataStore 是基于 FLow 实现的，所以保证了在主线程的安全性

2.以事务方式处理更新数据，事务有四大特性(原子性、一致性、 隔离性、持久性)

Preferences DataStore 只支持'Int'，'Long' 'Boolean' ，'Float' 'String'键值对数据，适合存储简单、小型的数据，并且不支持局部更新，

如果修改了其中一个值，整个文件内容将会被重新序列化。

如果想要支持对象数据，官方提供了 Proto DataStore 方式，用于存储类的对象(typed objects)，通过 protocol buffers 将对象序列化存储在本地
