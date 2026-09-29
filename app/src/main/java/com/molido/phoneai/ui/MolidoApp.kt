package com.molido.phoneai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(val text: String, val fromUser: Boolean, val time: Long = System.currentTimeMillis())

class MolidoViewModel : ViewModel() {
    var messages by mutableStateOf(
        listOf(ChatMessage("سلام! من MOLIDO PHONE AI هستم. آفلاین کار می‌کنم و اطلاعاتت را روی همین گوشی نگه می‌دارم.", false))
    )
        private set
    var memories by mutableStateOf(listOf<String>())
        private set
    var notes by mutableStateOf(listOf<String>())
        private set
    var input by mutableStateOf("")
    var language by mutableStateOf("fa")
    var dark by mutableStateOf(true)

    fun send() {
        val text = input.trim()
        if (text.isEmpty()) return
        messages = messages + ChatMessage(text, true)
        input = ""
        val reply = Brain.reply(text, language)
        messages = messages + ChatMessage(reply, false)
    }

    fun remember(text: String) {
        val clean = text.trim()
        if (clean.isNotEmpty() && !memories.contains(clean)) memories = memories + clean
    }

    fun addNote(text: String) {
        val clean = text.trim()
        if (clean.isNotEmpty()) notes = notes + clean
    }

    fun clearChat() {
        messages = listOf(ChatMessage(if (language == "fa") "گفتگو پاک شد. آماده‌ام." else "Chat cleared. Ready.", false))
    }
}

object Brain {
    fun reply(input: String, language: String): String {
        val x = input.lowercase(Locale.getDefault())
        return if (language == "fa") {
            when {
                x.contains("سلام") || x.contains("hello") -> "سلام 👋 من آماده‌ام. می‌توانی سؤال بپرسی، چیزی را به حافظه بسپاری یا یادداشت بسازی."
                x.contains("کمک") || x.contains("help") -> "دستورات ساده: «یادآوری: ...»، «یادداشت: ...»، «پاک کردن گفتگو»، یا هر سؤال متنی."
                x.startsWith("یادآوری:") -> "حتماً. این مورد برای ذخیره در حافظه آماده است؛ آن را از بخش حافظه هم می‌توانی مدیریت کنی."
                x.startsWith("یادداشت:") -> "یادداشت دریافت شد. آن را می‌توانی در بخش یادداشت‌ها ثبت کنی."
                x.contains("آفلاین") -> "هسته پایه MOLIDO بدون API و بدون VPS طراحی شده است."
                else -> "پیامت را دریافت کردم. در نسخه پایه، پاسخ‌ها محلی و بدون ارسال متن به سرور تولید می‌شوند."
            }
        } else {
            when {
                x.contains("hello") || x.contains("hi") -> "Hello 👋 I am ready. Ask a question or create a memory/note."
                x.contains("help") -> "Try: remember, note, clear chat, or ask a text question."
                x.contains("offline") -> "The base MOLIDO engine works locally without an API key or VPS."
                else -> "I received your message. In this base release, responses are generated locally without sending your text to a server."
            }
        }
    }
}

@Composable
fun MolidoApp(vm: MolidoViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = if (vm.language == "fa") listOf("دستیار", "حافظه", "یادداشت", "تنظیمات") else listOf("Assistant", "Memory", "Notes", "Settings")

    MaterialTheme(
        colorScheme = if (vm.dark) darkColorScheme() else lightColorScheme()
    ) {
        Column(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            TopAppBar(title = {
                Column {
                    Text("MOLIDO PHONE AI", fontWeight = FontWeight.Bold)
                    Text(if (vm.language == "fa") "آفلاین • خصوصی • سبک" else "Offline • Private • Lightweight",
                        style = MaterialTheme.typography.labelSmall)
                }
            })
            NavigationBar {
                tabs.forEachIndexed { i, label ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i },
                        icon = { Text(listOf("✦","🧠","📝","⚙")[i]) }, label = { Text(label) })
                }
            }
            when (tab) {
                0 -> AssistantScreen(vm)
                1 -> MemoryScreen(vm)
                2 -> NotesScreen(vm)
                3 -> SettingsScreen(vm)
            }
        }
    }
}

@Composable
fun AssistantScreen(vm: MolidoViewModel) {
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.messages) { m ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromUser) Arrangement.End else Arrangement.Start) {
                    Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 2.dp) {
                        Text(m.text, Modifier.padding(12.dp).widthIn(max = 310.dp))
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = vm.input,
                onValueChange = { vm.input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(if (vm.language == "fa") "پیام..." else "Message...") },
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = vm::send) { Text("➤") }
        }
    }
}

@Composable
fun MemoryScreen(vm: MolidoViewModel) {
    var text by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (vm.language == "fa") "حافظه محلی" else "Local Memory", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Row {
            OutlinedTextField(text, { text = it }, Modifier.weight(1f), placeholder = { Text(if(vm.language=="fa") "چیزی برای یادآوری..." else "Something to remember...") })
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.remember(text); text = "" }) { Text("+") }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(vm.memories) { item -> ListItem(headlineContent = { Text(item) }) }
        }
        if (vm.memories.isEmpty()) Text(if (vm.language=="fa") "هنوز حافظه‌ای ثبت نشده." else "No memories yet.")
    }
}

@Composable
fun NotesScreen(vm: MolidoViewModel) {
    var text by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (vm.language == "fa") "یادداشت‌ها" else "Notes", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Row {
            OutlinedTextField(text, { text = it }, Modifier.weight(1f), placeholder = { Text(if(vm.language=="fa") "یادداشت جدید..." else "New note...") })
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.addNote(text); text = "" }) { Text("+") }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(vm.notes) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item)
                        Text(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        if (vm.notes.isEmpty()) Text(if (vm.language=="fa") "هنوز یادداشتی ثبت نشده." else "No notes yet.")
    }
}

@Composable
fun SettingsScreen(vm: MolidoViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (vm.language=="fa") "تنظیمات" else "Settings", style = MaterialTheme.typography.headlineSmall)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if(vm.language=="fa") "حالت تاریک" else "Dark mode")
            Switch(checked = vm.dark, onCheckedChange = { vm.dark = it })
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if(vm.language=="fa") "زبان" else "Language")
            Button(onClick = { vm.language = if (vm.language=="fa") "en" else "fa" }) {
                Text(if(vm.language=="fa") "English" else "فارسی")
            }
        }
        OutlinedButton(onClick = vm::clearChat, Modifier.fillMaxWidth()) {
            Text(if(vm.language=="fa") "پاک کردن گفتگو" else "Clear chat")
        }
        Text(
            if(vm.language=="fa") "نسخه 1.0.0 • بدون VPS • بدون API Key • هسته محلی"
            else "Version 1.0.0 • No VPS • No API Key • Local core",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
