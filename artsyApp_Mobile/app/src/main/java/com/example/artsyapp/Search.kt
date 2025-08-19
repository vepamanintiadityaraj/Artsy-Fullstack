package com.example.artsyapp

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward


import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.artsyapp.ui.theme.LocalArtsyColors
import com.example.myapplication.R
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path




data class ArtistResponse(val _embedded: Embedded)
data class Embedded(val results: List<Artist>)
data class Artist(
    val id: String,
    val title: String,
    val _links: Links
)

data class Links(
    val thumbnail: Thumbnail,
    val self: SelfLink
)

data class SelfLink(val href: String)
data class Thumbnail(val href: String)


interface ApiService {
    @GET("/api/search/{artistName}")
    suspend fun searchArtists(@Path("artistName") artistName: String): ArtistResponse

    @GET("/api/end/{artistId}")
    suspend fun getArtistDetails(@Path("artistId") artistId: String): ArtistDetail

    @POST("/api/addFav/")
    suspend fun addFavorite(@Body payload: AddFavRequest): Response<AddFavResponse>

    @POST("/api/remFav/")
    suspend fun removeFavorite(@Body payload: Map<String, String>): Response<Void>
}


object DelApiClientSearch  {
    private const val BASE_URL = "https://adiartsytwt2.wl.r.appspot.com/"

    fun create(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(HttpClientProvider.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}


data class ArtistDetail(
    val id: String,
    val name: String?,
    val birthday: String?,
    val deathday: String?,
    val nationality: String?
)


data class FavoriteArtist(
    val id: String,
    val name: String,
    val birthday: String,
    val deathday: String,
    val nationality: String,
    val image: String
)

data class AddFavRequest(
    val artist: FavoriteArtist
)

data class AddFavResponse(
    val message: String,
    val email: String,
    val favorites: Map<String, FavoriteItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    var query by rememberSaveable { mutableStateOf("") }
    var results by rememberSaveable { mutableStateOf<List<Artist>>(emptyList()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var showNoResults by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(query) {
        focusRequester.requestFocus()
        keyboardController?.show()

        showNoResults = false
        if (query.length >= 3) {

            try {
                val response = DelApiClientSearch.create().searchArtists(query)
                results = response._embedded.results
                error = null
            } catch (e: Exception) {
                results = emptyList()
            }


            delay(1000)
            if (results.isEmpty()) showNoResults = true
        } else {
            results = emptyList()
            error = null
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp),
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
                title = {
                    SearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        onClear = { query = "" }, navController = navController,
                        focusRequester = focusRequester
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LocalArtsyColors.current.topBarColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (error != null) {
                Text(text = error!!, color = Color.Red, fontSize = 16.sp)
            }

            LazyColumn {
                items(results) { artist ->
                    ArtistItem(
                        artist = artist,
                        snackbarHostState = snackbarHostState,
                        onClick = {
                            val nameEncoded = Uri.encode(artist.title)
                            val href = artist._links.self.href
                            val idEncoded = href.takeIf { it.contains("/") }?.substringAfterLast("/") ?: run {
                                Toast.makeText(context, "Invalid artist ID", Toast.LENGTH_SHORT).show()
                                return@ArtistItem
                            }
                            Log.d("Artist Details in search.kt", idEncoded)
                            navController.navigate("artistDetails/$nameEncoded/$idEncoded")
                        }
                    )
                }
            }

            if (showNoResults) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                        .background(
                            color = LocalArtsyColors.current.cardColor,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Results found.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = LocalArtsyColors.current.textColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit, navController: NavHostController, focusRequester: FocusRequester) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalArtsyColors.current.topBarColor)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = LocalArtsyColors.current.textColor
        )

        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = query,
            onValueChange = { input ->
                if (input.contains("\n")) {
                    focusManager.clearFocus()
                    onQueryChange(input.replace("\n", ""))
                } else {
                    onQueryChange(input)
                }
            },
            modifier = Modifier.weight(1f)
                .focusRequester(focusRequester),
            singleLine = true,
            textStyle = TextStyle(fontSize = 18.sp, color = LocalArtsyColors.current.textColor),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onQueryChange(query)
                focusManager.clearFocus()
            }),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search artists...",
                            color = LocalArtsyColors.current.textColor.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            }
        )

        IconButton(
            onClick = {
                onClear()
                navController.popBackStack()
            }
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = LocalArtsyColors.current.textColor)
        }
    }
}
@Composable
fun ArtistItem(
    artist: Artist,
    onClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val user = UserSession.currentUser

    val artistId = artist._links.self.href.substringAfterLast("/")

    val isFavoriteInitial = remember {
        UserSession.currentUser?.favorites?.containsKey(artistId) == true
    }
    var isFavorite by remember { mutableStateOf(isFavoriteInitial) }

    val placeholderImage = R.drawable.artsy_app
    val imageUrl = artist._links.thumbnail.href
    val isMissingImage = imageUrl.contains("/assets/shared/missing_image.png")

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            if (isMissingImage) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White)
                        .clip(RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = placeholderImage),
                        contentDescription = "Placeholder Image",
                        modifier = Modifier
                            .height(180.dp)
                            .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    )
                }
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Artist Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            if (user != null) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            try {



                                if (!isFavorite) {
                                    val artistDetail = DelApiClientSearch.create().getArtistDetails(artistId)

                                    val favArtist = FavoriteArtist(
                                        id          = artistDetail.id,
                                        name        = artistDetail.name.orEmpty(),
                                        birthday    = artistDetail.birthday.orEmpty(),
                                        deathday    = artistDetail.deathday.orEmpty(),
                                        nationality = artistDetail.nationality.orEmpty(),
                                        image       = imageUrl
                                    )
                                    val payload = AddFavRequest(artist = favArtist)
                                    Log.d("AddFav", "Sending payload: $payload")


                                    val response = DelApiClientSearch.create().addFavorite(payload)
                                    Log.d("current user body","${response.body()}")
                                    if (response.isSuccessful) {
                                        isFavorite = true

                                        UserSession.currentUser = UserSession.currentUser?.copy(
                                            favorites = response.body()?.favorites as MutableMap<String, FavoriteItem>?
                                        )

                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Added to favorites")
                                        }
                                    } else {
                                        Log.e("AddFav", "API failed")

                                    }
                                } else {
                                    val response = DelApiClientSearch.create().removeFavorite(mapOf("artistId" to artistId))
                                    if (response.isSuccessful) {
                                        isFavorite = false
                                        (UserSession.currentUser?.favorites as? MutableMap<String, Any?>)?.remove(artistId)
                                        Log.d("RemoveFav", "Removed $artistId from UserSession")
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Deleted from favorites")
                                        }
                                    } else {
                                        Log.e("RemoveFav", "API failed: ${response.code()}")
                                        Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("FavError", "Exception: ${e.localizedMessage}", e)

                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(32.dp)
                        .background(
                            LocalArtsyColors.current.cardColor,
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Toggle Favorite",
                        tint = Color.Black
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomStart)
                    .background(LocalArtsyColors.current.cardColor)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = artist.title,
                    color = LocalArtsyColors.current.textColor,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "View artist",
                    tint = LocalArtsyColors.current.textColor
                )
            }
        }
    }
}
