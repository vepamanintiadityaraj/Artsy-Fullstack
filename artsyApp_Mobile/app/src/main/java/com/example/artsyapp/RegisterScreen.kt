package com.example.artsyapp

import android.util.Log
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
import androidx.compose.ui.platform.LocalView

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat

import androidx.core.view.WindowInsetsCompat.Type
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import androidx.navigation.NavHostController
import com.example.artsyapp.ui.theme.LocalArtsyColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController : NavHostController,
    onLoginClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val view = LocalView.current
    val density = LocalDensity.current
    var bottomPadding by remember { mutableStateOf(0.dp) }
    val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

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


    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    var nameTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    var registerError by remember { mutableStateOf<String?>(null) }

    var showEmailFormatError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

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
                title = { Text("Register", fontSize = 22.sp) },
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
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .padding(bottom = imePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Log.d("Raju",bottomPadding.toString())
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Enter full name") },
                isError = nameTouched && name.isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused) nameTouched = true
                    }
            )
            if (nameTouched && name.isBlank()) {
                Text(
                    text = "Full name cannot be empty",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 4.dp, bottom = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }



            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    showEmailFormatError = false
                    registerError = null
                },
                label = { Text("Enter email") },
                isError = emailTouched && (email.isBlank() || showEmailFormatError || registerError != null),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused) emailTouched = true
                    }
            )
            if (emailTouched && email.isBlank()) {
                Text(
                    text = "Email cannot be empty",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 4.dp, bottom = 8.dp)
                )
            } else if (showEmailFormatError && !isValidEmail(email)) {
                Text(
                    text = "Invalid email format",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 4.dp, bottom = 8.dp)
                )
            } else if (registerError != null) {
                Text(
                    text = registerError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 4.dp, bottom = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }


            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordTouched && password.isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused) passwordTouched = true
                    }
            )
            if (passwordTouched && password.isBlank()) {
                Text(
                    text = "Password cannot be empty",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 4.dp, bottom = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            val coroutineScope = rememberCoroutineScope()


            Button(
                onClick = {
                    nameTouched = true
                    emailTouched = true
                    passwordTouched = true

                    showEmailFormatError = !isValidEmail(email)

                    if (name.isNotBlank() && email.isNotBlank() && isValidEmail(email) && password.isNotBlank()) {
                        coroutineScope.launch {
                            loading = true
                            try {
                                val response = RegisterApiClient.create().registerUser(
                                    RegisterRequest(fullName = name, email = email, password = password)
                                )
                                UserSession.currentUser = response.user
                                registerError = null
                                Log.d("register data","${response.user}")
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Registered successfully")
                                }
                                delay(1000)

                                navController.navigate("favorites") {
                                    popUpTo("register") { inclusive = true }
                                }
                            } catch (e: Exception) {
                                if (e is HttpException && e.code() == 400) {
                                    registerError = "Email already exists."
                                } else {
                                    registerError = "Registration failed. Please try again."
                                }
                            }
                            finally {
                                loading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (loading) Color.Gray else LocalArtsyColors.current.dialogButtonColor
                ),
                shape = RoundedCornerShape(50),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color(0xFF2F3640),
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Register", color = LocalArtsyColors.current.dialogTextColor)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account?")
                TextButton(onClick = onLoginClick) {
                    Text("Login", color = LocalArtsyColors.current.dialogButtonColor)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}


data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String,
    val user: User
)



interface RegisterApiService {
    @POST("/api/reg/")
    suspend fun registerUser(@Body user: RegisterRequest): RegisterResponse
}

object RegisterApiClient {
    private const val BASE_URL = "https://adiartsytwt2.wl.r.appspot.com/"

    fun create(): RegisterApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(HttpClientProvider.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegisterApiService::class.java)
    }
}