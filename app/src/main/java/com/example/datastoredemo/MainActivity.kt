package com.example.datastoredemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.datastoredemo.ui.theme.DataStoreDemoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DataStoreDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DataStoreScreen()
                }
            }
        }
    }
}

/*
那么DataStore 和 SharedPreferences 有什么区别呢？

SharedPreferences 初始化的时候，会将整个文件内容加载到内存当中因此会带来以下问题：

1、通过 getXXX()获取数据时，可能会导致主线程阻塞

2、SharedPreferences 不能保证类型安全。
使用相同的 key 进行操作时，我们可以put 不同类型的数据覆盖掉相同的 key。在获取数据时就会出现ClassCastException

3、SharedPreferences 加载的数据会一起留存内存中，浪费内存

官方推出Preferences DataStore 主要用来替换SharedPreferences, Preferences DataStore 解决了SharedPreferences 带来的所有问题Preferences DataStore 相比于 SharedPreferences 优点

1、DataStore 是基于 FLow 实现的，所以保证了在主线程的安全性

2、以事务方式处理更新数据，事务有四大特性(原子性、一致性、 隔离性、持久性)

Preferences DataStore 只支持'Int'，'Long' 'Boolean' ，'Float' 'String'键值对数据，适合存储简单、小型的数据，并且不支持局部更新，

如果修改了其中一个值，整个文件内容将会被重新序列化。

如果想要支持对象数据，官方提供了 Proto DataStore 方式，用于存储类的对象(typed objects)，通过 protocol buffers 将对象序列化存储在本地
 */

@Composable
fun DataStoreScreen() {
    val storeManager = StoreManager(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text(text = "Data Store Demo")})}) {
        Column {
            var str by remember {
                mutableStateOf("")
            }

            OutlinedTextField(value = str, onValueChange = { str = it })

            Button(onClick = {
                coroutineScope.launch {
                    storeManager.saveString(str)
                }
            }) {
                Text(text = "保存字符串")
            }
            val strData = storeManager.stringData.collectAsState(initial = "")
            Text(text = "当前存储的字符串为 ${strData.value}")

            Button(onClick = {
                coroutineScope.launch {
                    storeManager.saveInt(100)
                }
            }) {
                Text(text = "保存Int")
            }
            val intData = storeManager.intData.collectAsState(initial = 0)
            Text(text = "当前存储的Int为 ${intData.value}")

            Button(onClick = {
                coroutineScope.launch {
                    storeManager.saveDouble(100.0)
                }
            }) {
                Text(text = "保存Double")
            }
            val doubleData = storeManager.doubleData.collectAsState(initial = 0.0)
            Text(text = "当前存储的Double为 ${doubleData.value}")

            Button(onClick = {
                coroutineScope.launch {
                    storeManager.saveBoolean(true)
                }
            }) {
                Text(text = "保存Boolean")
            }
            val booleanData = storeManager.booleanData.collectAsState(initial = false)
            Text(text = "当前存储的Boolean为 ${booleanData.value}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DataStoreDemoTheme {
        DataStoreScreen()
    }
}