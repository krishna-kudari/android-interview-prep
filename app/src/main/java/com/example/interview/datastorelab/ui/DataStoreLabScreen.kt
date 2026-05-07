package com.example.interview.datastorelab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.interview.datastorelab.DataStoreLabViewModel
import com.example.interview.datastorelab.data.model.Address
import com.example.interview.datastorelab.data.model.ContentPrefs
import com.example.interview.datastorelab.data.model.Language
import com.example.interview.datastorelab.data.model.Theme
import com.example.interview.datastorelab.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStoreLabScreen(
    viewModel: DataStoreLabViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackMessage) {
        if (uiState.snackMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.snackMessage)
            viewModel.clearSnack()
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "⚙️ Settings" to Icons.Default.Settings,
        "👤 Profile" to Icons.Default.Person,
        "🔐 Session" to Icons.Default.Key
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("DataStore Lab", fontWeight = FontWeight.Bold)
                        Text(
                            "Preferences • Typed • Serializer • Migration",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, (label, _) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            when (selectedTab) {
                0 -> AppSettingsTab(uiState.settings, viewModel)
                1 -> UserProfileTab(uiState.userProfile, viewModel)
                2 -> SessionTab(uiState.session, viewModel)
            }
        }
    }
}

// ── Tab 1: Preferences DataStore ─────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppSettingsTab(settings: com.example.interview.datastorelab.data.model.AppSettings, vm: DataStoreLabViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(
            title = "Preferences DataStore",
            body = "Stores typed primitives under Preferences.Key<T>.\n" +
                    "Each edit {} call is one atomic disk write.\n" +
                    "IOException → emit(emptyPreferences()) → all defaults."
        )

        // ── Live state display ────────────────────────────────────────────────
        LiveDataCard(title = "Live State (Preferences DataStore)") {
            KvRow("theme", settings.theme.name)
            KvRow("language", settings.language.displayName)
            KvRow("fontSize", "${settings.fontSize}sp")
            KvRow("notifications", settings.notificationsEnabled.toString())
            KvRow("analytics", settings.analyticsEnabled.toString())
            KvRow(
                "lastOpened",
                if (settings.lastOpenedTimestamp == 0L) "never"
                else SimpleDateFormat("HH:mm:ss", LocalLocale.current.platformLocale)
                    .format(Date(settings.lastOpenedTimestamp))
            )
        }

        // ── Theme ─────────────────────────────────────────────────────────────
        SectionTitle("Theme (enum stored as String)")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Theme.entries.forEach { theme ->
                FilterChip(
                    selected = settings.theme == theme,
                    onClick = { vm.setTheme(theme) },
                    label = { Text(theme.displayName) },
                    leadingIcon = {
                        Icon(
                            if (theme == Theme.DARK) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        // ── Language ──────────────────────────────────────────────────────────
        SectionTitle("Language (enum stored as String)")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Language.entries.forEach { lang ->
                FilterChip(
                    selected = settings.language == lang,
                    onClick = { vm.setLanguage(lang) },
                    label = { Text(lang.displayName) }
                )
            }
        }

        // ── Font Size ─────────────────────────────────────────────────────────
        SectionTitle("Font Size (Int key, 10–28)")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            listOf(10, 12, 14, 16, 18, 20, 24, 28).forEach { size ->
                FilterChip(
                    selected = settings.fontSize == size,
                    onClick = { vm.setFontSize(size) },
                    label = { Text("${size}sp") }
                )
            }
        }

        // ── Boolean toggles ───────────────────────────────────────────────────
        SectionTitle("Boolean Keys")
        LabeledSwitch(
            label = "Push Notifications",
            checked = settings.notificationsEnabled,
            onCheckedChange = { vm.setNotificationsEnabled(it) }
        )
        LabeledSwitch(
            label = "Analytics",
            checked = settings.analyticsEnabled,
            onCheckedChange = { vm.setAnalyticsEnabled(it) }
        )

        // ── Atomic multi-key write ────────────────────────────────────────────
        SectionTitle("Atomic Multi-Key Write")
        InfoCard(
            title = null,
            body = "applySettingsAtomically() wraps all key mutations in a single edit {}.\n" +
                    "If the process dies mid-write, ALL keys revert — no partial state.\n" +
                    "This is impossible to achieve safely with SharedPreferences."
        )
        Button(
            onClick = { vm.applySettingsAtomically(Theme.DARK, Language.HINDI, 18) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply (Dark + Hindi + 18sp) Atomically")
        }

        // ── Clear ─────────────────────────────────────────────────────────────
        OutlinedButton(
            onClick = { vm.clearSettings() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Clear All Settings (prefs.clear())")
        }
    }
}

// ── Tab 2: Typed DataStore — UserProfile ─────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserProfileTab(profile: UserProfile, vm: DataStoreLabViewModel) {
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var bioInput by remember { mutableStateOf("") }
    var badgeInput by remember { mutableStateOf("") }
    var statKeyInput by remember { mutableStateOf("views") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(
            title = "Typed DataStore<UserProfile>",
            body = "Stores a complex @Serializable object as JSON.\n" +
                    "Custom Serializer<T> bridges InputStream/OutputStream.\n" +
                    "updateData { current.copy(...) } for partial updates.\n" +
                    "CorruptionException → ReplaceFileCorruptionHandler → UserProfile()"
        )

        // ── Live state display ────────────────────────────────────────────────
        LiveDataCard(title = "Live State (user_profile.json)") {
            KvRow("id", profile.id.ifEmpty { "(empty)" })
            KvRow("displayName", profile.displayName.ifEmpty { "(empty)" })
            KvRow("email", profile.email.ifEmpty { "(empty)" })
            KvRow("bio", profile.bio.ifEmpty { "(empty)" })
            KvRow("badges", profile.badges.joinToString(", ").ifEmpty { "(none)" })
            KvRow("stats", profile.stats.entries.joinToString(", ") { "${it.key}=${it.value}" }.ifEmpty { "(none)" })
        }

        // ── Nested object: Address ────────────────────────────────────────────
        LiveDataCard(title = "Nested Object: address") {
            KvRow("city", profile.address.city.ifEmpty { "(empty)" })
            KvRow("state", profile.address.state.ifEmpty { "(empty)" })
            KvRow("country", profile.address.country)
        }

        // ── Nested object: ContentPrefs ───────────────────────────────────────
        LiveDataCard(title = "Nested Object: contentPrefs") {
            KvRow("emailUpdates", profile.contentPrefs.emailUpdates.toString())
            KvRow("pushNotifications", profile.contentPrefs.pushNotifications.toString())
            KvRow("weeklyDigest", profile.contentPrefs.weeklyDigest.toString())
            KvRow(
                "topicWeights",
                profile.contentPrefs.topicWeights.entries
                    .joinToString(", ") { "${it.key}=${it.value}" }
                    .ifEmpty { "(none)" }
            )
        }

        // ── Partial updates ───────────────────────────────────────────────────
        SectionTitle("Partial Update: copy() fields")

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            onClick = {
                if (nameInput.isNotBlank()) {
                    vm.updateDisplayName(nameInput)
                    nameInput = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Update Display Name") }

        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            onClick = {
                if (emailInput.isNotBlank()) {
                    vm.updateEmail(emailInput)
                    emailInput = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Update Email") }

        // ── Load full sample profile ──────────────────────────────────────────
        SectionTitle("Write Complex Nested Object")
        Button(
            onClick = { vm.saveFullProfile(sampleUserProfile()) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Full Sample Profile (nested + map + list)") }

        // ── Badges (List<String>) ─────────────────────────────────────────────
        SectionTitle("Badges (List<String> field)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = badgeInput,
                onValueChange = { badgeInput = it },
                label = { Text("Badge name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    if (badgeInput.isNotBlank()) {
                        vm.addBadge(badgeInput)
                        badgeInput = ""
                    }
                }
            ) { Text("Add") }
        }
        if (profile.badges.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                profile.badges.forEach { badge ->
                    InputChip(
                        selected = false,
                        onClick = { vm.removeBadge(badge) },
                        label = { Text(badge) },
                        trailingIcon = {
                            Text("✕", modifier = Modifier.padding(start = 2.dp))
                        }
                    )
                }
            }
        }

        // ── Stats (Map<String, Int>) ──────────────────────────────────────────
        SectionTitle("Stats (Map<String, Int> field)")
        InfoCard(
            title = null,
            body = "Maps and Lists stored natively inside the @Serializable object.\n" +
                    "updateData { current.copy(stats = updatedMap) } — full map replaced atomically."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = statKeyInput,
                onValueChange = { statKeyInput = it },
                label = { Text("Stat key") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(onClick = { if (statKeyInput.isNotBlank()) vm.incrementStat(statKeyInput) }) {
                Text("+1")
            }
        }
        if (profile.stats.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                profile.stats.forEach { (k, v) ->
                    StatBadge(key = k, value = v)
                }
            }
        }

        // ── Nested object: Address ────────────────────────────────────────────
        SectionTitle("Nested Object: Update Address")
        Button(
            onClick = {
                vm.updateAddress(
                    Address(street = "123 MG Road", city = "Bengaluru", state = "Karnataka", country = "India", pinCode = "560001")
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Set Bengaluru Address") }

        // ── ContentPrefs toggles ──────────────────────────────────────────────
        SectionTitle("Nested ContentPrefs Toggles")
        LabeledSwitch(
            label = "Email Updates",
            checked = profile.contentPrefs.emailUpdates,
            onCheckedChange = {
                vm.updateContentPrefs(profile.contentPrefs.copy(emailUpdates = it))
            }
        )
        LabeledSwitch(
            label = "Weekly Digest",
            checked = profile.contentPrefs.weeklyDigest,
            onCheckedChange = {
                vm.updateContentPrefs(profile.contentPrefs.copy(weeklyDigest = it))
            }
        )

        SectionTitle("Topic Weights (Map<String, Float>)")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("tech", "sports", "politics", "food", "travel").forEach { topic ->
                val weight = profile.contentPrefs.topicWeights[topic] ?: 0f
                FilterChip(
                    selected = weight > 0f,
                    onClick = { vm.updateContentPrefs(profile.contentPrefs.copy(
                        topicWeights = profile.contentPrefs.topicWeights.toMutableMap().apply {
                            this[topic] = if (weight > 0f) 0f else 1f
                        }
                    ))},
                    label = { Text("$topic ${if (weight > 0f) "★" else ""}") }
                )
            }
        }

        OutlinedButton(
            onClick = { vm.clearProfile() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) { Text("Clear Profile (updateData { UserProfile() })") }
    }
}

// ── Tab 3: Typed DataStore — Session ─────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SessionTab(session: com.example.interview.datastorelab.data.model.SessionData, vm: DataStoreLabViewModel) {
    var userIdInput by remember { mutableStateOf("alice@example.com") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(
            title = "2nd Typed DataStore<SessionData> (session.json)",
            body = "Independent file from user_profile.json.\n" +
                    "Separate FileLock → token refresh never blocks profile read.\n" +
                    "logout() = updateData { SessionData() } — all-or-nothing clear.\n" +
                    "Extend: wrap produceFile with EncryptedFile for at-rest token encryption."
        )

        // ── Login status banner ───────────────────────────────────────────────
        LoginBanner(session)

        // ── Live state display ────────────────────────────────────────────────
        LiveDataCard(title = "Live State (session.json)") {
            KvRow("isLoggedIn", session.isLoggedIn.toString())
            KvRow("userId", session.userId.ifEmpty { "(none)" })
            KvRow(
                "authToken",
                if (session.authToken.isEmpty()) "(none)"
                else "${session.authToken.take(12)}…"
            )
            KvRow(
                "refreshToken",
                if (session.refreshToken.isEmpty()) "(none)"
                else "${session.refreshToken.take(12)}…"
            )
            KvRow(
                "expiresAt",
                if (session.expiresAtMs == 0L) "(none)"
                else SimpleDateFormat("HH:mm:ss", LocalLocale.current.platformLocale).format(Date(session.expiresAtMs))
            )
            KvRow("remainingMin", "${session.remainingSessionMinutes} min")
            KvRow("isExpired", session.isTokenExpired.toString())
            KvRow("roles", session.roles.joinToString(", ").ifEmpty { "(none)" })
            KvRow("deviceId", session.deviceId.ifEmpty { "(none)" })
        }

        // ── Login ─────────────────────────────────────────────────────────────
        if (!session.isLoggedIn) {
            SectionTitle("Login")
            OutlinedTextField(
                value = userIdInput,
                onValueChange = { userIdInput = it },
                label = { Text("User ID / email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = { vm.login(userIdInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = userIdInput.isNotBlank()
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Login (writes full SessionData atomically)")
            }
        } else {
            // ── Authenticated actions ─────────────────────────────────────────
            SectionTitle("Token Operations")
            Button(
                onClick = { vm.refreshToken() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Key, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Refresh Token (partial update via copy())")
            }

            SectionTitle("Role Management (List<String>)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ADMIN", "MODERATOR", "BETA_TESTER", "SUPPORT").forEach { role ->
                    val hasRole = role in session.roles
                    FilterChip(
                        selected = hasRole,
                        onClick = { if (!hasRole) vm.addRole(role) },
                        label = { Text(role) }
                    )
                }
            }

            InfoCard(
                title = null,
                body = "Roles are stored as List<String> inside SessionData.\n" +
                        "addRole() uses updateData { current.copy(roles = current.roles + role) }.\n" +
                        "The entire SessionData is re-serialized on each role add — this is the\n" +
                        "typed DataStore trade-off vs. a per-key approach."
            )

            SectionTitle("Logout")
            Button(
                onClick = { vm.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout (updateData { SessionData() })")
            }
        }

        // ── Encryption note ───────────────────────────────────────────────────
        InfoCard(
            title = "At-rest Encryption (real apps)",
            body = "Replace context.dataStoreFile() in DataStoreModule with:\n\n" +
                    "EncryptedFile.Builder(\n" +
                    "  context, file, masterKey,\n" +
                    "  AES256_GCM_HKDF_4KB\n" +
                    ").build()\n\n" +
                    "Pass the EncryptedFile's streams to your Serializer.\n" +
                    "Zero changes to SessionStore or SessionSerializer."
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { vm.resetEverything() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) { Text("Reset ALL DataStores") }
    }
}

// ── Reusable composables ──────────────────────────────────────────────────────

@Composable
private fun InfoCard(title: String?, body: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (title != null) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun LiveDataCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun KvRow(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            key,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(0.6f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun LabeledSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun StatBadge(key: String, value: Int) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(key, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(4.dp))
        Text(
            value.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun LoginBanner(session: com.example.interview.datastorelab.data.model.SessionData) {
    val (bg, text) = if (session.isLoggedIn && !session.isTokenExpired)
        Pair(Color(0xFF1B5E20), Color.White)
    else if (session.isLoggedIn && session.isTokenExpired)
        Pair(Color(0xFFE65100), Color.White)
    else
        Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            when {
                !session.isLoggedIn -> "Not logged in"
                session.isTokenExpired -> "⚠ Token expired — session still persisted on disk"
                else -> "✓ Logged in as ${session.userId} · ${session.remainingSessionMinutes} min remaining"
            },
            color = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Sample data ───────────────────────────────────────────────────────────────

private fun sampleUserProfile() = UserProfile(
    id = "user_${(1000..9999).random()}",
    displayName = "Priya Sharma",
    email = "priya.sharma@example.com",
    bio = "Senior Android Engineer | Kotlin enthusiast | ex-Flipkart",
    badges = listOf("Early Adopter", "Power User", "5★ Reviewer"),
    stats = mapOf(
        "posts" to 142,
        "followers" to 3820,
        "following" to 89,
        "likes" to 5643
    ),
    address = Address(
        street = "42 Koramangala 4th Block",
        city = "Bengaluru",
        state = "Karnataka",
        country = "India",
        pinCode = "560034"
    ),
    contentPrefs = ContentPrefs(
        favoriteCategories = listOf("Tech", "Finance", "Travel"),
        emailUpdates = true,
        pushNotifications = true,
        weeklyDigest = false,
        topicWeights = mapOf("tech" to 0.9f, "finance" to 0.7f, "travel" to 0.5f)
    )
)
