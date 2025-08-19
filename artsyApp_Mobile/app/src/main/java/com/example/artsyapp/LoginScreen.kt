package com.example.artsyapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.navigation.NavHostController
import com.example.artsyapp.ui.theme.LocalArtsyColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val message: String, val user: User)

data class User(
    val _id: String,
    val fullName: String,
    val email: String,
    val imagelink: String,
    val favorites: MutableMap<String, FavoriteItem>? = mutableMapOf()
)

data class FavoriteItem(
    val name: String,
    val birthday: String?,
    val deathday: String?,
    val nationality: String?,
    val image: String?,
    val DateTimeAdded: String
)

object UserSession {
    var currentUser: User? by mutableStateOf(null)
}

interface AuthApiService {
    @POST("/api/login/")
    suspend fun loginUser(@Body credentials: LoginRequest): LoginResponse

    @GET("/api/me")
    suspend fun getMe(): User
}

object AuthApiClient {
    private const val BASE_URL = "https://adiartsytwt2.wl.r.appspot.com/"

    fun create(): AuthApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(HttpClientProvider.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    onRegisterClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val view = LocalView.current
    val density = LocalDensity.current
    var bottomPadding by remember { mutableStateOf(0.dp) }
    var loading by remember { mutableStateOf(false) }
    var showEmailFormatError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    DisposableEffect(view) {
        val listener = ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val bottomInset = insets.getInsets(Type.ime()).bottom
            bottomPadding = with(density) { bottomInset.toDp() }
            insets
        }
        onDispose {
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    var loginError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        containerColor = Color(0xFF1C1C1E),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(data.visuals.message)
                    }
                }
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Login", fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LocalArtsyColors.current.topBarColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .padding(bottom = bottomPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailTouched) {
                        loginError = null
                    }
                },
                label = { Text("Email") },
                isError = emailTouched && (email.isBlank() || showEmailFormatError),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (emailTouched && email.isBlank()) 4.dp else 16.dp)
                    .onFocusChanged {
                        if (it.isFocused) {
                            emailTouched = true
                        } else {
                            if (email.isNotBlank()) {
                                showEmailFormatError = !isValidEmail(email)
                            }
                        }
                    },
                singleLine = true
            )
            if (emailTouched && email.isBlank()) {
                Text(
                    "Email cannot be empty",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 12.dp)
                )
            } else if (showEmailFormatError) {
                Text(
                    "Invalid email format",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 12.dp)
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordTouched && it.isNotBlank()) loginError = null
                },
                label = { Text("Password") },
                isError = passwordTouched && password.isBlank(),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (passwordTouched && password.isBlank()) 4.dp else 24.dp)
                    .onFocusChanged {
                        if (it.isFocused) passwordTouched = true
                    },
                singleLine = true
            )
            if (passwordTouched && password.isBlank()) {
                Text(
                    "Password cannot be empty",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = {
                    emailTouched = true
                    passwordTouched = true
                    if (email.isBlank() || password.isBlank()) return@Button
                    loading = true
                    coroutineScope.launch {
                        try {
                            val response = AuthApiClient.create().loginUser(LoginRequest(email, password))
                            UserSession.currentUser = response.user
                            loginError = null
                            snackbarHostState.showSnackbar("Logged In successfully")
                            navController.popBackStack()
                        } catch (e: Exception) {
                            loginError = "Username or password is incorrect."
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (loading) Color(0xFFF0EFF2) else LocalArtsyColors.current.dialogButtonColor
                ),
                shape = RoundedCornerShape(50)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color(0xFF2F3640),
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login", color = LocalArtsyColors.current.dialogTextColor)
                }
            }

            loginError?.let {
                Text(
                    it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account yet?")
                TextButton(onClick = onRegisterClick) {
                    Text("Register", color = LocalArtsyColors.current.dialogButtonColor)
                }
            }
        }

    }
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}