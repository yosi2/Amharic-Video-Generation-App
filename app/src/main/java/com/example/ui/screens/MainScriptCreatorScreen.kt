package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Scene
import com.example.data.model.ScriptEntity
import com.example.data.model.VideoScript
import com.example.ui.viewmodel.GenerationUiState
import com.example.ui.viewmodel.ScriptViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScriptCreatorScreen(
    viewModel: ScriptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val savedScripts by viewModel.savedScripts.collectAsState()
    val generationState by viewModel.generationState.collectAsState()
    
    var activeTab by remember { mutableStateOf(0) } // 0 = Create, 1 = Library
    var topicInput by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("TikTok") } // TikTok, YouTube Shorts
    var selectedTone by remember { mutableStateOf("Energetic") } // Energetic, Informative, Funny
    
    var selectedScriptEntity by remember { mutableStateOf<ScriptEntity?>(null) }
    
    // Suggestion topics list
    val suggestions = listOf(
        "የአክሱም ሃውልት አስገራሚ ታሪኮች" to "Axum History",
        "ምርጥ የኢትዮጵያ ቡና አዘገጃጀት ሚስጥር" to "Ethiopian Coffee",
        "የእንጀራ አነባብሮ አሰራር በቅደም ተከተል" to "Injera Cooking",
        "3 የአዲስ አበባ ከተማ ድብቅና ውብ ቦታዎች" to "Addis Hidden Places",
        "በቀላሉ Amharic ለመማር የሚረዱ ምክሮች" to "Amharic Tips",
        "የሀገራችን አትሌቶች የስኬት ሚስጥር" to "Athletes Secrets"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "አማርኛ Shorts",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Amharic Shorts: Powered by Gemini 3.5-Flash", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Hero Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Hero Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "የአማርኛ ቪዲዮ ስክሪፕት ፈጣሪ",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Generate viral Amharic scripts with AI storyboard preview",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("አዲስ ፍጠር (Create)", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("create_tab")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ቤተ-መጽሐፍት (${savedScripts.size})", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("library_tab")
                )
            }

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                if (activeTab == 0) {
                    // Create Script Screen
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Prompt / Topic Input Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "የቪዲዮው ርዕስ (Video Topic/Theme)",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = topicInput,
                                        onValueChange = { topicInput = it },
                                        placeholder = { Text("ስለ ምን መስራት ይፈልጋሉ? (e.g. የቡና ቁርስ ታሪክ)") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("topic_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        trailingIcon = {
                                            if (topicInput.isNotEmpty()) {
                                                IconButton(onClick = { topicInput = "" }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                                }
                                            }
                                        }
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Suggestions chip row
                                    Text(
                                        text = "ምሳሌዎች (Tap to try):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        suggestions.take(3).forEach { (amharic, _) ->
                                            SuggestionChip(
                                                onClick = { topicInput = amharic },
                                                label = { Text(amharic, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        suggestions.drop(3).take(3).forEach { (amharic, _) ->
                                            SuggestionChip(
                                                onClick = { topicInput = amharic },
                                                label = { Text(amharic, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Options Card (Platform & Tone)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Platform Selection
                                    Text(
                                        text = "ማህበራዊ ሚዲያ (Social Platform)",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        val platforms = listOf("TikTok", "YouTube Shorts")
                                        platforms.forEach { platform ->
                                            val isSelected = selectedPlatform == platform
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                                                     else MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { selectedPlatform = platform }
                                                    .height(54.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = null,
                                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = platform,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Tone Selection
                                    Text(
                                        text = "የድምፅ ቃና (Tone of Voice)",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val tones = listOf(
                                            "Energetic" to Icons.Default.Star,
                                            "Informative" to Icons.Default.Info,
                                            "Funny" to Icons.Default.Face
                                        )
                                        tones.forEach { (tone, icon) ->
                                            val isSelected = selectedTone == tone
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer 
                                                                     else MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { selectedTone = tone }
                                                    .height(54.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.Center,
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = when(tone) {
                                                            "Energetic" -> "ደማቅ (Energetic)"
                                                            "Informative" -> "አስተማሪ (Info)"
                                                            else -> "አስቂኝ (Funny)"
                                                        },
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Submit Button
                        item {
                            Button(
                                onClick = {
                                    if (topicInput.isBlank()) {
                                        Toast.makeText(context, "እባክዎ መጀመሪያ ርዕስ ያስገቡ!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.generateScript(topicInput, selectedPlatform, selectedTone)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("submit_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ባለሙያ ስክሪፕት ፍጠር (Generate)",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                        
                        // Empty spacer
                        item {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                } else {
                    // Library Screen
                    if (savedScripts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "ምንም የተቀመጠ ስክሪፕት የለም",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "ከላይ አዲስ ፍጠር የሚለውን በመጫን የመጀመሪያ ስክሪፕትዎን በGemini AI ያመንጩ!",
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(savedScripts) { scriptEntity ->
                                ScriptLibraryCard(
                                    scriptEntity = scriptEntity,
                                    onClick = { selectedScriptEntity = scriptEntity },
                                    onDelete = { viewModel.deleteScript(scriptEntity.id) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Handle Generation API States
    when (val state = generationState) {
        is GenerationUiState.Loading -> {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "በማዘጋጀት ላይ...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gemini is writing structured, modern, viral Amharic script...",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        is GenerationUiState.Success -> {
            // Automatically reset and open details
            LaunchedEffect(state) {
                // Find matching saved entity to open full detail with local db id if possible
                val newlySaved = savedScripts.firstOrNull { it.topic == topicInput }
                if (newlySaved != null) {
                    selectedScriptEntity = newlySaved
                } else if (savedScripts.isNotEmpty()) {
                    selectedScriptEntity = savedScripts.first()
                }
                viewModel.clearGenerationState()
                topicInput = "" // reset topic
            }
        }
        is GenerationUiState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearGenerationState() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearGenerationState() }) {
                        Text("እሺ (OK)")
                    }
                },
                title = { Text("ስህተት ተከስቷል (Error)") },
                text = { Text(state.message) }
            )
        }
        else -> { /* Idle, do nothing */ }
    }

    // Detail & Simulator View Fullscreen Dialog
    selectedScriptEntity?.let { entity ->
        ScriptDetailDialog(
            scriptEntity = entity,
            onDismiss = { selectedScriptEntity = null },
            onDelete = {
                viewModel.deleteScript(entity.id)
                selectedScriptEntity = null
            }
        )
    }
}

@Composable
fun ScriptLibraryCard(
    scriptEntity: ScriptEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val formattedDate = remember(scriptEntity.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        sdf.format(Date(scriptEntity.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("script_card_${scriptEntity.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                    text = scriptEntity.videoTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Delete button
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            
            // Subtitle Topic info
            Text(
                text = "Topic: ${scriptEntity.topic}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pills row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Platform pill
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(scriptEntity.platform, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )

                    // Tone pill
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = when (scriptEntity.tone) {
                                    "Energetic" -> "ደማቅ"
                                    "Informative" -> "አስተማሪ"
                                    else -> "አስቂኝ"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }

                // Date
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("አጥፋ (Delete)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("ተመለስ (Cancel)")
                }
            },
            title = { Text("ስክሪፕቱን ያጥፉ?") },
            text = { Text("ይህንን የአማርኛ ቪዲዮ ስክሪፕት መሰረዝ ይፈልጋሉ? ድርጊቱ አይመለስም!") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptDetailDialog(
    scriptEntity: ScriptEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val videoScript = remember(scriptEntity) {
        try {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(VideoScript::class.java)
            adapter.fromJson(scriptEntity.scriptJson)
        } catch(e: Exception) {
            null
        }
    }

    var selectedModeTab by remember { mutableStateOf(0) } // 0 = Storyboard, 1 = Video Simulator

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = scriptEntity.videoTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Topic: ${scriptEntity.topic}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (videoScript != null) {
                                    val fullText = buildString {
                                        appendLine("=== ${videoScript.videoTitle} ===")
                                        appendLine("Platform: ${scriptEntity.platform} | Tone: ${scriptEntity.tone}")
                                        appendLine("Topic: ${scriptEntity.topic}")
                                        appendLine()
                                        videoScript.scenes.forEach { scene ->
                                            appendLine("[Scene ${scene.sceneNumber} - ${scene.section}]")
                                            appendLine("Visual B-Roll: ${scene.visualDescription}")
                                            appendLine("Text Overlay: ${scene.textOverlay}")
                                            appendLine("AI Voiceover: ${scene.voiceoverText}")
                                            appendLine()
                                        }
                                    }
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Amharic Script", fullText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "ሙሉው ስክሪፕት ኮፒ ተደርጓል! (Script Copied!)", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Copy All")
                        }
                        IconButton(
                            onClick = onDelete
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            if (videoScript == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("ስክሪፕቱን መክፈት አልተቻለም (Failed to load script data)")
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    // Selection modes row
                    TabRow(
                        selectedTabIndex = selectedModeTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedModeTab == 0,
                            onClick = { selectedModeTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ባለታሪክ (Storyboard)", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                        Tab(
                            selected = selectedModeTab == 1,
                            onClick = { selectedModeTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("አጫዋች (Interactive Player)", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (selectedModeTab == 0) {
                            // Storyboard View
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(videoScript.scenes) { scene ->
                                    StoryboardSceneCard(scene = scene, context = context)
                                }
                                item {
                                    Spacer(modifier = Modifier.height(40.dp))
                                }
                            }
                        } else {
                            // TikTok Simulation Interactive Player
                            InteractiveVideoPlayer(scenes = videoScript.scenes, topic = scriptEntity.topic)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryboardSceneCard(scene: Scene, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = scene.sceneNumber.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (scene.section) {
                            "Hook" -> "መግቢያ (Hook - 0-3s)"
                            "Body_Point_1" -> "ነጥብ 1 (Point 1)"
                            "Body_Point_2" -> "ነጥብ 2 (Point 2)"
                            "Body_Point_3" -> "ነጥብ 3 (Point 3)"
                            "CTA" -> "መደምደሚያ (Call to Action)"
                            else -> scene.section
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Copy single voiceover button
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Amharic Voiceover", scene.voiceoverText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "የድምፅ ንባቡ ኮፒ ተደርጓል!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Copy voiceover text", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text overlay (Amharic)
            Text(
                text = "የስክሪን ፅሁፍ (Text Overlay):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = scene.textOverlay,
                    modifier = Modifier.padding(10.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Voiceover (Amharic)
            Text(
                text = "የድምፅ ንባብ (AI Voiceover Speech):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = scene.voiceoverText,
                    modifier = Modifier.padding(10.dp),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // B-Roll description (English stock guidance)
            Text(
                text = "የጀርባ ቪዲዮ መግለጫ (B-Roll Video Guidance):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = scene.visualDescription,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InteractiveVideoPlayer(scenes: List<Scene>, topic: String) {
    var activeIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var playProgress by remember { mutableStateOf(0f) }
    var autoPlayEnabled by remember { mutableStateOf(true) }

    val activeScene = scenes.getOrNull(activeIndex) ?: return

    // Dynamic high-quality image background mapping based on topic/content keywords
    val imageUrl = remember(activeIndex, topic) {
        val cleanTopic = topic.lowercase()
        val tag = when {
            cleanTopic.contains("ቡና") || cleanTopic.contains("coffee") -> "coffee"
            cleanTopic.contains("እንጀራ") || cleanTopic.contains("injera") || cleanTopic.contains("ምግብ") || cleanTopic.contains("cooking") -> "ethiopian-food"
            cleanTopic.contains("አዲስ አበባ") || cleanTopic.contains("addis") || cleanTopic.contains("ከተማ") -> "addis-ababa"
            cleanTopic.contains("ታሪክ") || cleanTopic.contains("axum") || cleanTopic.contains("አክሱም") || cleanTopic.contains("history") -> "ancient"
            cleanTopic.contains("አትሌት") || cleanTopic.contains("ሩጫ") || cleanTopic.contains("sport") -> "athletics"
            cleanTopic.contains("ቴክኖሎጂ") || cleanTopic.contains("tech") || cleanTopic.contains("ስልክ") -> "digital"
            else -> "ethiopia"
        }
        val randomSeed = 100 + activeIndex * 15
        "https://images.unsplash.com/photo-$randomSeed?auto=format&fit=crop&w=600&q=80&sig=$randomSeed&keywords=$tag"
    }

    // Coroutine ticking for progress simulation
    LaunchedEffect(isPlaying, activeIndex) {
        if (isPlaying) {
            playProgress = 0f
            val totalTicks = 100
            for (tick in 1..totalTicks) {
                delay(60)
                if (!isPlaying) break
                playProgress = tick / 100f
            }
            if (isPlaying && playProgress >= 1f) {
                if (autoPlayEnabled) {
                    if (activeIndex < scenes.size - 1) {
                        activeIndex++
                    } else {
                        activeIndex = 0
                        isPlaying = false
                    }
                } else {
                    isPlaying = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Vertical Mobile Mockup Frame
        Box(
            modifier = Modifier
                .width(280.dp)
                .weight(1f)
                .background(Color.Black, shape = RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .clickable { isPlaying = !isPlaying }
        ) {
            // Background Image (Stock Visual)
            AsyncImage(
                model = imageUrl,
                contentDescription = activeScene.visualDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.img_hero_banner) // fallback
            )

            // Dynamic vignette/dark overlay for rich subtitle readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Header indicators
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                // Scene progress indicators (TikTok/Instagram-style bars)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    scenes.forEachIndexed { index, _ ->
                        val barProgress = when {
                            index < activeIndex -> 1f
                            index == activeIndex -> playProgress
                            else -> 0f
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(2.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(barProgress)
                                    .background(Color.White, shape = RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Scene indicator label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scene ${activeIndex + 1}/${scenes.size} - ${activeScene.section}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    
                    if (isPlaying) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("SIMULATOR PLAY", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Big Bold Captions Overlay (Amharic)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // TikTok-style text border styling
                Text(
                    text = activeScene.textOverlay,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.65f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            // Bottom Audio / Voiceover Narration block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
            ) {
                // Audio caption
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    // Audio Wave animation (Canvas representation)
                    AudioWaveformIndicator(isAnimating = isPlaying)
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Voiceover Speech (አማርኛ):",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Voiceover text
                        AnimatedTextCaptionHighlight(
                            fullText = activeScene.voiceoverText,
                            progress = playProgress,
                            isPlaying = isPlaying
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Visual description prompt bar (what B-roll should show)
                Text(
                    text = "🎬 B-Roll: ${activeScene.visualDescription}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Play/Pause & Navigate Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (activeIndex > 0) activeIndex--
                    playProgress = 0f
                },
                enabled = activeIndex > 0
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Prev Scene")
            }

            Spacer(modifier = Modifier.width(16.dp))

            FloatingActionButton(
                onClick = { isPlaying = !isPlaying },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = {
                    if (activeIndex < scenes.size - 1) activeIndex++
                    playProgress = 0f
                },
                enabled = activeIndex < scenes.size - 1
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Next Scene")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Toggle Auto play
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Checkbox(
                checked = autoPlayEnabled,
                onCheckedChange = { autoPlayEnabled = it }
            )
            Text(
                text = "በራሱ እንዲቀያይር (Auto-Advance Scenes)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun AudioWaveformIndicator(isAnimating: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val heightScale1 = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val heightScale2 = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val heightScale3 = infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier.height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bars = listOf(heightScale1, heightScale2, heightScale3, heightScale1, heightScale2)
        bars.forEachIndexed { index, anim ->
            val scale = if (isAnimating) anim.value else 0.25f
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(scale)
                    .background(
                        color = when(index % 3) {
                            0 -> Color(0xFF4CAF50) // Emerald Green
                            1 -> Color(0xFFFFEB3B) // Golden Yellow
                            else -> Color(0xFFF44336) // Deep Red
                        },
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun AnimatedTextCaptionHighlight(
    fullText: String,
    progress: Float,
    isPlaying: Boolean
) {
    val words = remember(fullText) { fullText.split(" ") }
    val highlightedWordCount = remember(progress, words.size) {
        if (!isPlaying) 0 else (words.size * progress).toInt().coerceIn(0, words.size)
    }

    Text(
        text = fullText,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 14.sp
    )
    
    Spacer(modifier = Modifier.height(3.dp))
    LinearProgressIndicator(
        progress = { if (isPlaying) progress else 0f },
        color = Color(0xFFFFEB3B), // Yellow glow playbar
        trackColor = Color.White.copy(alpha = 0.2f),
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
    )
}
