package com.example.skillexchange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.skillexchange.database.AppDatabase
import com.example.skillexchange.database.NeedPost
import com.example.skillexchange.database.SwapRequest
import com.example.skillexchange.database.User
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillExchangeApp()
        }
    }
}

// ─── COLORS ───────────────────────────────────────────────────────────────────
val NavyBlue = Color(0xFF1A237E)
val SoftBlue = Color(0xFF3949AB)
val LightBg  = Color(0xFFF0F4F8)
val CardWhite = Color(0xFFFFFFFF)
val GreenOk  = Color(0xFF2E7D32)
val OrangeWarn = Color(0xFFF57C00)
val TextGray = Color(0xFF555555)

// ─── ROOT NAV ─────────────────────────────────────────────────────────────────
@Composable
fun SkillExchangeApp() {
    var currentScreen by remember { mutableStateOf("home") }
    when (currentScreen) {
        "home"       -> HomeScreen       { currentScreen = it }
        "profile"    -> ProfileScreen    { currentScreen = it }
        "needboard"  -> NeedBoardScreen  { currentScreen = it }
        "swaps"      -> SwapScreen       { currentScreen = it }
        "trust"      -> TrustScoreScreen { currentScreen = it }
    }
}

// ─── HOME SCREEN ──────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(navigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🤝", fontSize = 56.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Skill-Exchange",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )
        Text(
            "Barter your skills, build your community",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(Modifier.height(32.dp))

        HomeButton("👤  My Skill Profile", NavyBlue) { navigate("profile") }
        Spacer(Modifier.height(14.dp))
        HomeButton("📋  Need Board", SoftBlue) { navigate("needboard") }
        Spacer(Modifier.height(14.dp))
        HomeButton("🔄  My Swaps", Color(0xFF00695C)) { navigate("swaps") }
        Spacer(Modifier.height(14.dp))
        HomeButton("⭐  Trust Score", Color(0xFF6A1B9A)) { navigate("trust") }
    }
}

@Composable
fun HomeButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

// ─── PROFILE SCREEN ───────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(navigate: (String) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var name    by remember { mutableStateOf("") }
    var skill   by remember { mutableStateOf("") }
    var phone   by remember { mutableStateOf("") }
    var score   by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }

    // Load existing profile on first open
    LaunchedEffect(Unit) {
        val user = db.userDao().getUserById(1)
        if (user != null) {
            name  = user.name
            skill = user.skill
            phone = user.phone
            score = user.trustScore
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .padding(24.dp)
    ) {
        BackButton { navigate("home") }
        Spacer(Modifier.height(8.dp))
        Text("My Skill Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
        Spacer(Modifier.height(24.dp))

        AppTextField("Your Name", "e.g. Ramu Kumar", name) { name = it }
        Spacer(Modifier.height(12.dp))
        AppTextField("Your Skill", "e.g. Expert Carpenter", skill) { skill = it }
        Spacer(Modifier.height(12.dp))
        AppTextField("Phone Number", "e.g. 9876543210", phone) { phone = it }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isBlank() || skill.isBlank()) {
                    message = "⚠️ Please fill Name and Skill"
                    return@Button
                }
                scope.launch {
                    db.userDao().insertUser(
                        User(id = 1, name = name, skill = skill, phone = phone, trustScore = score)
                    )
                    message = "✅ Profile Saved!"
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
        ) {
            Text("Save Profile", fontSize = 16.sp, color = Color.White)
        }

        Spacer(Modifier.height(16.dp))
        if (message.isNotEmpty()) {
            Text(message, color = if (message.startsWith("✅")) GreenOk else OrangeWarn, fontSize = 14.sp)
        }
        Spacer(Modifier.height(16.dp))
        InfoCard("⭐ Trust Score: $score points")
    }
}

// ─── NEED BOARD SCREEN ────────────────────────────────────────────────────────
@Composable
fun NeedBoardScreen(navigate: (String) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var skillNeeded  by remember { mutableStateOf("") }
    var offerSkill   by remember { mutableStateOf("") }
    var description  by remember { mutableStateOf("") }
    var filterText   by remember { mutableStateOf("") }
    var posts        by remember { mutableStateOf(listOf<NeedPost>()) }
    var message      by remember { mutableStateOf("") }

    fun loadPosts() {
        scope.launch {
            posts = if (filterText.isBlank()) db.needPostDao().getAllOpenPosts()
            else db.needPostDao().filterBySkillNeeded(filterText)
        }
    }

    LaunchedEffect(Unit) { loadPosts() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .padding(20.dp)
    ) {
        BackButton { navigate("home") }
        Text("Need Board", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
        Spacer(Modifier.height(16.dp))

        AppTextField("Skill you need", "e.g. Roof Repair", skillNeeded) { skillNeeded = it }
        Spacer(Modifier.height(8.dp))
        AppTextField("Skill you offer", "e.g. Woodwork", offerSkill) { offerSkill = it }
        Spacer(Modifier.height(8.dp))
        AppTextField("Short description (optional)", "e.g. Leaking roof, urgent", description) { description = it }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (skillNeeded.isBlank() || offerSkill.isBlank()) {
                    message = "⚠️ Fill Skill Needed and Offer Skill"
                    return@Button
                }
                scope.launch {
                    val user = db.userDao().getUserById(1)
                    db.needPostDao().insertNeedPost(
                        NeedPost(
                            userId = 1,
                            userName = user?.name ?: "Unknown",
                            skillNeeded = skillNeeded,
                            offerSkill = offerSkill,
                            description = description
                        )
                    )
                    skillNeeded = ""; offerSkill = ""; description = ""
                    message = "✅ Need Posted!"
                    loadPosts()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
        ) { Text("Post My Need", color = Color.White, fontSize = 15.sp) }

        Spacer(Modifier.height(8.dp))
        if (message.isNotEmpty()) Text(message, color = if (message.startsWith("✅")) GreenOk else OrangeWarn)

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                label = { Text("Filter by skill needed") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { loadPosts() },
                colors = ButtonDefaults.buttonColors(containerColor = SoftBlue),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Filter") }
        }

        Spacer(Modifier.height(16.dp))
        Text("Open Needs (${posts.size})", fontWeight = FontWeight.SemiBold, color = NavyBlue)
        Spacer(Modifier.height(8.dp))

        if (posts.isEmpty()) {
            Text("No posts yet. Be the first to post!", color = TextGray, fontSize = 13.sp)
        } else {
            posts.forEach { post ->
                NeedPostCard(post)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun NeedPostCard(post: NeedPost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Post #${post.id} — ${post.userName}", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text("🔧 Needs: ${post.skillNeeded}", color = Color(0xFFB71C1C), fontSize = 13.sp)
            Text("🎁 Offers: ${post.offerSkill}", color = GreenOk, fontSize = 13.sp)
            if (post.description.isNotEmpty())
                Text("📝 ${post.description}", color = TextGray, fontSize = 12.sp)
        }
    }
}

// ─── SWAP SCREEN ──────────────────────────────────────────────────────────────
@Composable
fun SwapScreen(navigate: (String) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var receiverId by remember { mutableStateOf("") }
    var postId     by remember { mutableStateOf("") }
    var swapId     by remember { mutableStateOf("") }
    var swaps      by remember { mutableStateOf(listOf<SwapRequest>()) }
    var message    by remember { mutableStateOf("") }

    fun loadSwaps() {
        scope.launch { swaps = db.swapRequestDao().getSwapsByUser(1) }
    }

    LaunchedEffect(Unit) { loadSwaps() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .padding(20.dp)
    ) {
        BackButton { navigate("home") }
        Text("My Swaps", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
        Spacer(Modifier.height(16.dp))

        // ── Send Request ──
        InfoCard("To send a swap, enter the other person's User ID and the Post ID you saw on Need Board.")
        Spacer(Modifier.height(10.dp))
        AppTextField("Receiver's User ID", "e.g. 2", receiverId) { receiverId = it }
        Spacer(Modifier.height(8.dp))
        AppTextField("Post ID from Need Board", "e.g. 1", postId) { postId = it }
        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                val rId = receiverId.toIntOrNull()
                val pId = postId.toIntOrNull()
                if (rId == null || pId == null) { message = "⚠️ Enter valid numbers"; return@Button }
                scope.launch {
                    db.swapRequestDao().insertSwapRequest(SwapRequest(requesterId = 1, receiverId = rId, postId = pId))
                    message = "✅ Swap Request Sent! Check below for your Swap ID."
                    receiverId = ""; postId = ""
                    loadSwaps()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
        ) { Text("Send Swap Request", color = Color.White) }

        Spacer(Modifier.height(16.dp))
        Divider(color = Color(0xFFCFD8DC))
        Spacer(Modifier.height(12.dp))

        // ── Confirm Swap ──
        Text("Confirm a Swap", fontWeight = FontWeight.SemiBold, color = NavyBlue, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        InfoCard("Both people must press confirm. Trust score increases only after BOTH confirm.")
        Spacer(Modifier.height(8.dp))
        AppTextField("Swap ID to confirm", "Look below for the Swap #ID", swapId) { swapId = it }
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val sId = swapId.toIntOrNull()
                if (sId == null) { message = "⚠️ Enter a valid Swap ID"; return@Button }
                scope.launch {
                    val swap = db.swapRequestDao().getSwapById(sId)
                    if (swap == null) { message = "⚠️ Swap not found"; return@launch }

                    val updated = if (swap.requesterId == 1) swap.copy(requesterConfirmed = true)
                    else swap.copy(receiverConfirmed = true)

                    val final = if (updated.requesterConfirmed && updated.receiverConfirmed)
                        updated.copy(isCompleted = true)
                    else updated

                    db.swapRequestDao().updateSwapRequest(final)

                    if (final.isCompleted) {
                        db.userDao().getUserById(final.requesterId)?.let {
                            db.userDao().updateUser(it.copy(trustScore = it.trustScore + 1))
                        }
                        db.userDao().getUserById(final.receiverId)?.let {
                            db.userDao().updateUser(it.copy(trustScore = it.trustScore + 1))
                        }
                        db.needPostDao().closePost(final.postId)
                        message = "🎉 Swap Complete! Both trust scores increased!"
                    } else {
                        message = "✅ Your confirmation saved. Waiting for other party."
                    }
                    swapId = ""
                    loadSwaps()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenOk)
        ) { Text("Confirm This Swap ✓", color = Color.White) }

        Spacer(Modifier.height(8.dp))
        if (message.isNotEmpty()) Text(message, color = if (message.startsWith("⚠️")) OrangeWarn else GreenOk, fontSize = 13.sp)

        Spacer(Modifier.height(16.dp))
        Text("All My Swaps (${swaps.size})", fontWeight = FontWeight.SemiBold, color = NavyBlue)
        Spacer(Modifier.height(8.dp))

        if (swaps.isEmpty()) {
            Text("No swaps yet.", color = TextGray, fontSize = 13.sp)
        } else {
            swaps.forEach { swap ->
                SwapCard(swap)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SwapCard(swap: SwapRequest) {
    val bgColor = if (swap.isCompleted) Color(0xFFE8F5E9) else CardWhite
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Swap #${swap.id} — Post #${swap.postId}", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
            Text("Requester ID: ${swap.requesterId}  |  Receiver ID: ${swap.receiverId}", fontSize = 12.sp, color = TextGray)
            Text(
                if (swap.isCompleted) "✅ Completed" else "⏳ Pending confirmation",
                color = if (swap.isCompleted) GreenOk else OrangeWarn,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── TRUST SCORE SCREEN ───────────────────────────────────────────────────────
@Composable
fun TrustScoreScreen(navigate: (String) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    var name  by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }
    var score by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val user = db.userDao().getUserById(1)
        if (user != null) { name = user.name; skill = user.skill; score = user.trustScore }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton { navigate("home") }
        Spacer(Modifier.height(32.dp))
        Text("⭐", fontSize = 64.sp)
        Spacer(Modifier.height(12.dp))
        Text("$score", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
        Text("Trust Points", fontSize = 18.sp, color = TextGray)
        Spacer(Modifier.height(32.dp))
        if (name.isNotEmpty()) {
            InfoCard("Hello $name!\nYour skill: $skill\n\n1 point = 1 successful swap confirmed by both people.\nHigher score = More trusted in the community! 🌟")
        } else {
            InfoCard("No profile found. Please create your profile first.")
        }
    }
}

// ─── SHARED UI COMPONENTS ─────────────────────────────────────────────────────
@Composable
fun AppTextField(label: String, placeholder: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun InfoCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(12.dp),
            color = NavyBlue,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun BackButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text("← Back", color = SoftBlue, fontSize = 14.sp)
    }
    Spacer(Modifier.height(4.dp))
}