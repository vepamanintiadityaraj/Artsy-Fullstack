package com.example.artsyapp

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.artsyapp.ui.theme.LocalArtsyColors
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class Fav(
    val name: String,
    val image: String,
    val nationality: String?,
    val birthday: String?,
    val deathday: String?,
    val DateTimeAdded: String
)

interface DelApiService {
    @POST("/api/logout/")
    suspend fun logout(): Response<Unit>

    @DELETE("/api/del/")
    suspend fun deleteAccount(): Response<Unit>
}

object DelApiClient {
    private const val BASE_URL = "https://adiartsytwt2.wl.r.appspot.com/"

    fun create(): DelApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(HttpClientProvider.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DelApiService::class.java)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavScreen(navController: NavHostController) {
    val today = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    val context = LocalContext.current
    val user = UserSession.currentUser
    val scope = rememberCoroutineScope()
    val artsyColors = LocalArtsyColors.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp),
                snackbar = { snackbarData ->
                    Snackbar(
                        containerColor = Color(0xFF1C1C1E),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(snackbarData.visuals.message)
                    }
                }
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Artist Search") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = artsyColors.topBarColor),
                actions = {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }

                    if (user != null) {
                        var menuExpanded by remember { mutableStateOf(false) }

                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(user.imagelink)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile image",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Log out") },
                                    onClick = {
                                        menuExpanded = false
                                        scope.launch {
                                            try {
                                                val response = DelApiClient.create().logout()
                                                if (response.isSuccessful) {
                                                    UserSession.currentUser = null
                                                    snackbarHostState.showSnackbar("Logged out successfully")
                                                } else {
                                                    Log.e("Fav", "API failed in fav.kt: ${response.code()}")
                                                }
                                            } catch (e: Exception) {

                                            }
                                        }
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Delete account", color = Color.Red) },
                                    onClick = {
                                        menuExpanded = false
                                        scope.launch {
                                            try {
                                                val response = DelApiClient.create().deleteAccount()
                                                if (response.isSuccessful) {
                                                    UserSession.currentUser = null
                                                    snackbarHostState.showSnackbar("Deleted user successfully")
                                                } else {
                                                    Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {

                                            }
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = { navController.navigate("login") }) {
                            Icon(Icons.Outlined.Person, contentDescription = "Login")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            DateBanner(today)
            FavBanner()
            Spacer(modifier = Modifier.height(16.dp))

            if (user == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(30.dp))
                    LoginButton(navController)
                    Spacer(modifier = Modifier.height(16.dp))
                    FooterText {
                        val intent = Intent(Intent.ACTION_VIEW, "https://www.artsy.net/".toUri())
                        context.startActivity(intent)
                    }
                }
            } else {
                if (user.favorites?.isEmpty() != false) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp)
                            .background(color = artsyColors.cardColor, shape = RoundedCornerShape(24.dp))
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No favorites",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = artsyColors.textColor
                            )
                        )
                    }
                    FooterText {
                        val intent = Intent(Intent.ACTION_VIEW, "https://www.artsy.net/".toUri())
                        context.startActivity(intent)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(user.favorites.entries.sortedByDescending { it.value.DateTimeAdded }) { entry ->
                            val fav = entry.value
                            val artistId = entry.key
                            val cardColor = if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color.White
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    ,
                                color = cardColor,
                                tonalElevation = 1.dp,
                                shape = RoundedCornerShape(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 6.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fav.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        val info = buildString {
                                            fav.nationality?.takeIf { it.isNotBlank() }?.let { append(it) }
                                            if (!fav.birthday.isNullOrBlank()) {
                                                if (isNotEmpty()) append(", ")
                                                append(fav.birthday)
                                            }
                                        }

                                        Text(
                                            text = if (info.isNotBlank()) info else "•",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }


                                    RelativeTimeText(fav.DateTimeAdded)

                                    IconButton(
                                        onClick = {
                                            val nameEncoded = Uri.encode(fav.name)
                                            navController.navigate("artistDetails/$nameEncoded/$artistId")
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Go to artist details",
                                            tint = artsyColors.textColor
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                FooterText {
                                    val intent = Intent(Intent.ACTION_VIEW, "https://www.artsy.net/".toUri())
                                    context.startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateBanner(text: String) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDark) Color(0xFF1C1C1E)
                else Color(0xFFFAFAFA)
            )
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDark) Color.White else Color.Gray,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
        )
    }
}

@Composable
fun FavBanner() {

    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECECF1))
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Favorites",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isDark) Color.Black else Color.Unspecified
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
@Composable
fun LoginButton(navController: NavHostController) {
    Button(
        onClick = { navController.navigate("login") },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = LocalArtsyColors.current.dialogButtonColor),
        modifier = Modifier
            .wrapContentWidth()
            .height(48.dp)
    ) {
        Text(
            text = "Log in to see favorites",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = LocalArtsyColors.current.dialogTextColor
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun FooterText(onClick: () -> Unit) {
    Text(
        text = "Powered by Artsy",
        style = MaterialTheme.typography.bodySmall.copy(
            color = Color.Gray,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    )
}
@Composable
fun RelativeTimeText(timestamp: String) {
    var relativeTime by remember { mutableStateOf("") }

    LaunchedEffect(timestamp) {
        while (true) {
            relativeTime = computeRelativeTime(timestamp)
            kotlinx.coroutines.delay(1000)
        }
    }

    Text(
        text = relativeTime,
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray
    )
}

fun computeRelativeTime(isoTimestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val pastDate = sdf.parse(isoTimestamp)
        val now = Date()
        val diff = now.time - (pastDate?.time ?: 0L)

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            seconds < 60 -> "$seconds second${if (seconds == 1L) "" else "s"} ago"
            minutes < 60 -> "$minutes minute${if (minutes == 1L) "" else "s"} ago"
            hours < 24 -> "$hours hour${if (hours == 1L) "" else "s"} ago"
            else -> "$days day${if (days == 1L) "" else "s"} ago"
        }
    } catch (e: Exception) {
        ""
    }
}