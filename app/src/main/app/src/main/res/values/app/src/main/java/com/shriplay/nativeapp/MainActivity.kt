package com.shriplay.nativeapp

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

data class Video(
    val title: String,
    val channel: String,
    val views: String,
    val age: String,
    val tag: String,
    val url: String
)

val videos = listOf(
    Video(
        "Beautiful Uttarakhand Journey",
        "ShriPlay Travel",
        "1.2M views",
        "2 days ago",
        "TRAVEL",
        "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    ),
    Video(
        "Morning Motivation - Start Your Day",
        "ShriPlay Motivation",
        "245K views",
        "5 hours ago",
        "MOTIVATION",
        "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    ),
    Video(
        "Kumaoni Culture & Pahadi Life",
        "Pahadi World",
        "89K views",
        "1 week ago",
        "CULTURE",
        "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
    ),
    Video(
        "EV Scooter Daily Ride",
        "EV India",
        "36K views",
        "3 days ago",
        "EV",
        "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ShriPlayApp() }
    }
}

@Composable
fun ShriPlayApp() {
    var currentTab by remember { mutableStateOf("Home") }
    var selectedVideo by remember { mutableStateOf<Video?>(null) }
    var searchOpen by remember { mutableStateOf(false) }
    var createDetail by remember { mutableStateOf<String?>(null) }
    var profileDetail by remember { mutableStateOf<String?>(null) }

    val hasInnerScreen = selectedVideo != null || createDetail != null || profileDetail != null || searchOpen

    BackHandler(enabled = hasInnerScreen || currentTab != "Home") {
        when {
            selectedVideo != null -> selectedVideo = null
            createDetail != null -> createDetail = null
            profileDetail != null -> profileDetail = null
            searchOpen -> searchOpen = false
            currentTab != "Home" -> currentTab = "Home"
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFE53935),
            background = Color.White,
            surface = Color.White
        )
    ) {
        Scaffold(
            topBar = {
                if (!hasInnerScreen) {
                    TopAppBar(
                        title = { Brand() },
                        actions = {
                            IconButton(onClick = { searchOpen = true }) {
                                Icon(Icons.Default.Search, "Search")
                            }
                            IconButton(onClick = { profileDetail = "Notifications" }) {
                                Icon(Icons.Default.NotificationsNone, "Notifications")
                            }
                            IconButton(onClick = { currentTab = "You" }) {
                                Box(
                                    Modifier.size(32.dp).background(Color(0xFFE53935), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("S", color = Color.White)
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (!hasInnerScreen) {
                    NavigationBar(containerColor = Color.White) {
                        NavItem("Home", Icons.Default.Home, currentTab) { currentTab = "Home" }
                        NavItem("Shorts", Icons.Default.SmartDisplay, currentTab) { currentTab = "Shorts" }

                        NavigationBarItem(
                            selected = currentTab == "Create",
                            onClick = { currentTab = "Create" },
                            icon = {
                                Box(
                                    Modifier.size(46.dp).background(Color(0xFFF0F0F0), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, "Create")
                                }
                            },
                            label = { Text("") }
                        )

                        NavItem("Subscriptions", Icons.Default.Subscriptions, currentTab) {
                            currentTab = "Subscriptions"
                        }

                        NavItem("You", Icons.Default.Person, currentTab) { currentTab = "You" }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when {
                    selectedVideo != null -> PlayerScreen(selectedVideo!!) { selectedVideo = null }
                    searchOpen -> SearchScreen(
                        onBack = { searchOpen = false },
                        onOpenVideo = { selectedVideo = it }
                    )
                    createDetail != null -> SimpleDetailScreen(createDetail!!) { createDetail = null }
                    profileDetail != null -> SimpleDetailScreen(profileDetail!!) { profileDetail = null }
                    else -> when (currentTab) {
                        "Home" -> HomeScreen { selectedVideo = it }
                        "Shorts" -> ShortsScreen()
                        "Create" -> CreateScreen { createDetail = it }
                        "Subscriptions" -> SubscriptionsScreen { selectedVideo = it }
                        "You" -> YouScreen { profileDetail = it }
                    }
                }
            }
        }
    }
}

@Composable
fun Brand() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(width = 36.dp, height = 25.dp)
                .background(Color(0xFFE53935), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = Color.White)
        }
        Spacer(Modifier.width(8.dp))
        Text("ShriPlay", fontSize = 22.sp)
    }
}

@Composable
fun RowScope.NavItem(
    label: String,
    icon: ImageVector,
    current: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = current == label,
        onClick = onClick,
        icon = { Icon(icon, label) },
        label = { Text(label, fontSize = 10.sp) }
    )
}

@Composable
fun HomeScreen(openVideo: (Video) -> Unit) {
    Column {
        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
            items(listOf("All", "Music", "Live", "Gaming", "News", "Travel", "EV")) { item ->
                AssistChip(
                    onClick = {},
                    label = { Text(item) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        LazyColumn {
            items(videos) { video ->
                VideoCard(video, openVideo)
            }
        }
    }
}

@Composable
fun VideoCard(video: Video, openVideo: (Video) -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .clickable { openVideo(video) }
            .padding(bottom = 18.dp)
    ) {
        Box(
            Modifier.fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF151515),
                            Color(0xFF6A1B9A),
                            Color(0xFFE53935)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayCircle,
                null,
                tint = Color.White,
                modifier = Modifier.size(72.dp)
            )
            Text(
                video.tag,
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            )
        }

        Row(Modifier.padding(12.dp)) {
            Box(
                Modifier.size(42.dp).background(Color(0xFFE53935), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(video.channel.take(1), color = Color.White)
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(video.title, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${video.channel} • ${video.views} • ${video.age}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Icon(Icons.Default.MoreVert, null)
        }
    }
}

@Composable
fun ShortsScreen() {
    Box(
        Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.SmartDisplay,
                null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            Text("ShriPlay Shorts", color = Color.White, fontSize = 30.sp)
            Text("Native Shorts screen", color = Color.LightGray)
        }

        Column(
            Modifier.align(Alignment.CenterEnd).padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ThumbUp, null, tint = Color.White)
            Text("24K", color = Color.White)

            Spacer(Modifier.height(22.dp))

            Icon(Icons.Default.Comment, null, tint = Color.White)
            Text("987", color = Color.White)

            Spacer(Modifier.height(22.dp))

            Icon(Icons.Default.Share, null, tint = Color.White)
            Text("Share", color = Color.White)
        }
    }
}

@Composable
fun CreateScreen(openDetail: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create on ShriPlay", fontSize = 26.sp)

        Spacer(Modifier.height(24.dp))

        ClickCard(Icons.Default.Upload, "Upload a video") { openDetail("Upload a video") }
        ClickCard(Icons.Default.SmartDisplay, "Create a Short") { openDetail("Create a Short") }
        ClickCard(Icons.Default.LiveTv, "Go Live") { openDetail("Go Live") }
        ClickCard(Icons.Default.PostAdd, "Create a post") { openDetail("Create a post") }
    }
}

@Composable
fun ClickCard(icon: ImageVector, text: String, onClick: () -> Unit) {
    ElevatedCard(
        Modifier.fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null)
            Spacer(Modifier.width(16.dp))
            Text(text, fontSize = 17.sp)
        }
    }
}

@Composable
fun SubscriptionsScreen(openVideo: (Video) -> Unit) {
    Column {
        Text("Subscriptions", fontSize = 24.sp, modifier = Modifier.padding(16.dp))

        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp)) {
            items(listOf("Travel", "Music", "EV India", "Pahadi", "News")) { name ->
                Column(
                    Modifier.width(80.dp).padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.size(54.dp).background(Color(0xFFE53935), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(name.take(1), color = Color.White)
                    }
                    Text(name, fontSize = 11.sp, maxLines = 1)
                }
            }
        }

        LazyColumn {
            items(videos.take(3)) {
                VideoCard(it, openVideo)
            }
        }
    }
}

@Composable
fun YouScreen(openDetail: (String) -> Unit) {
    LazyColumn {
        item {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(72.dp).background(Color(0xFFE53935), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", color = Color.White, fontSize = 30.sp)
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text("ShriPlay", fontSize = 24.sp)
                        Text("@shriplay", color = Color.Gray)
                        Text("128K subscribers", color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }

        item { DetailItem(Icons.Default.History, "History") { openDetail("History") } }
        item { DetailItem(Icons.Default.VideoLibrary, "Your videos") { openDetail("Your videos") } }
        item { DetailItem(Icons.Default.Download, "Downloads") { openDetail("Downloads") } }
        item { DetailItem(Icons.Default.Bookmark, "Saved videos") { openDetail("Saved videos") } }
        item { DetailItem(Icons.Default.Settings, "Settings") { openDetail("Settings") } }
    }
}

@Composable
fun DetailItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = { Icon(icon, null) },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider()
}

@Composable
fun SimpleDetailScreen(title: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            Text(title, fontSize = 20.sp)
        }

        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("$title screen is working", fontSize = 20.sp)
        }
    }
}

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenVideo: (Video) -> Unit
) {
    var query by remember { mutableStateOf("") }

    Column {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search ShriPlay") },
                singleLine = true
            )
        }

        LazyColumn {
            items(
                videos.filter {
                    query.isBlank() ||
                    it.title.contains(query, true) ||
                    it.channel.contains(query, true)
                }
            ) {
                VideoCard(it, onOpenVideo)
            }
        }
    }
}

@Composable
fun PlayerScreen(video: Video, onBack: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            Text("ShriPlay Player", fontSize = 18.sp)
        }

        val context = androidx.compose.ui.platform.LocalContext.current

        val player = remember(video.url) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(video.url))
                prepare()
                playWhenReady = true
            }
        }

        DisposableEffect(player) {
            onDispose { player.release() }
        }

        AndroidView(
            factory = {
                PlayerView(it).apply {
                    this.player = player
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
        )

        Column(Modifier.padding(16.dp)) {
            Text(video.title, fontSize = 20.sp)
            Text(
                "${video.views} • ${video.age}",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(12.dp))

            LazyRow {
                item { ActionChip(Icons.Default.ThumbUp, "Like") }
                item { ActionChip(Icons.Default.Share, "Share") }
                item { ActionChip(Icons.Default.Bookmark, "Save") }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).background(Color(0xFFE53935), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(video.channel.take(1), color = Color.White)
                }

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(video.channel)
                    Text("128K subscribers", color = Color.Gray, fontSize = 12.sp)
                }

                Button(onClick = {}) {
                    Text("Subscribe")
                }
            }
        }
    }
}

@Composable
fun ActionChip(icon: ImageVector, text: String) {
    AssistChip(
        onClick = {},
        leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) },
        label = { Text(text) },
        modifier = Modifier.padding(end = 8.dp)
    )
}
