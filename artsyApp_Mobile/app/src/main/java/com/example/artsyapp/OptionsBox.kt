package com.example.artsyapp

import android.net.Uri
import android.text.TextUtils.replace
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import com.example.myapplication.R
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBox
import com.example.artsyapp.Artist
import com.example.artsyapp.ArtistResponse
import com.example.artsyapp.Links
import com.example.artsyapp.SelfLink
import com.example.artsyapp.Thumbnail
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.artsyapp.ui.theme.LocalArtsyColors
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsBoxScreen(
    artistName: String,
    artistId: String,
    navController: NavHostController
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val user = UserSession.currentUser

    val baseTabs = listOf(
        TabItem("Details", Icons.Outlined.Info),
        TabItem("Artworks", Icons.Outlined.AccountBox)
    )
    val tabs = if (user != null) baseTabs + TabItem("Similar", Icons.Outlined.PersonSearch) else baseTabs

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
                        Text(text = snackbarData.visuals.message)
                    }
                }
            )
        },
        topBar = {
            val user = UserSession.currentUser
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            val isFavoriteInitial = remember {
                user?.favorites?.containsKey(artistId) == true
            }
            var isFavorite by remember { mutableStateOf(isFavoriteInitial) }
            TopAppBar(
                title = { Text(artistName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (user != null) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val imageUrl = " "
                                        Log.d("FavToggle", "Toggling for artist ID: $artistId")

                                        if (!isFavorite) {
                                            val artistDetail = DelApiClientSearch.create().getArtistDetails(artistId)
                                            Log.d("AddFav", "Fetched artist details: $artistDetail")


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
                                                Log.d("current user","${UserSession.currentUser}")
                                                Log.d("AddFav", "Added locally to UserSession")
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Added to favorites")
                                                }
                                            } else {
                                                Log.e("AddFav", "API failed: ${response.code()}")
                                                Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            val response = DelApiClientSearch.create().removeFavorite(mapOf("artistId" to artistId))
                                            if (response.isSuccessful) {
                                                isFavorite = false
                                                (UserSession.currentUser?.favorites as? MutableMap<String, Any?>)?.remove(artistId)
                                                Log.d("RemoveFav", "Removed $artistId from UserSession")
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Removed from favorites")
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
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Toggle Favorite",
                                tint = LocalArtsyColors.current.textColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LocalArtsyColors.current.topBarColor)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF435C8C),
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(tab.title) },
                        icon = { Icon(tab.icon, contentDescription = tab.title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ArtistDetailsContent(artistId)
                1 -> ArtistArtworksContent(artistId)
                2 -> if (user != null) SimilarArtistsTab(artistId, navController,snackbarHostState)
            }
        }
    }
}

data class TabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

data class ArtistDetailsResponse(
    val name: String,
    val birthday: String?,
    val deathday: String?,
    val nationality: String?,
    val biography: String?
)


data class ArtworkResponse(val _embedded: EmbeddedArtworks)
data class EmbeddedArtworks(val artworks: List<Artwork>)

data class Artwork(
    val id: String,
    val title: String?,
    val date: String?,
    val _links: ArtworkLinks
)

data class ArtworkLinks(val thumbnail: ArtworkThumbnail)
data class ArtworkThumbnail(val href: String)



interface ArtistApiService {
    @GET("/api/end/{id}")
    suspend fun getArtistDetails(@Path("id") artistId: String): ArtistDetailsResponse
}

val artistApi: ArtistApiService = Retrofit.Builder()
    .baseUrl("https://adiartsytwt2.wl.r.appspot.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(ArtistApiService::class.java)


interface ArtworkApiService {
    @GET("/api/artworks/{id}")
    suspend fun getArtworks(@Path("id") artistId: String): ArtworkResponse
}

val artworkApi: ArtworkApiService = Retrofit.Builder()
    .baseUrl("https://adiartsytwt2.wl.r.appspot.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(ArtworkApiService::class.java)


interface GeneApiService {
    @GET("/api/genes/{id}")
    suspend fun getCategories(@Path("id") artworkId: String): CategoryResponse
}

val geneApi: GeneApiService = Retrofit.Builder()
    .baseUrl("https://adiartsytwt2.wl.r.appspot.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(GeneApiService::class.java)

@Composable
fun ArtistDetailsContent(artistId: String) {
    var artistDetails by remember { mutableStateOf<ArtistDetailsResponse?>(null) }
    var loading by remember { mutableStateOf(true) }


    LaunchedEffect(artistId) {
        try {
            val response = artistApi.getArtistDetails(artistId)
            Log.d("Artist Details", "$response")
            artistDetails = response
        } catch (e: Exception) {
            Log.e("Artist Details Error", e.toString())
        } finally {
            loading = false
        }
    }

    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }else {
        artistDetails?.let { artist ->
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val birth = artist.birthday.orEmpty()
                val death = artist.deathday.orEmpty()
                val nationality = artist.nationality.orEmpty()

                val dateRange = when {
                    birth.isNotBlank() && death.isNotBlank() -> "$birth – $death"
                    birth.isNotBlank() -> "$birth –"
                    death.isNotBlank() -> death
                    else -> ""
                }

                val infoText = listOf(nationality, dateRange)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                if (infoText.isNotBlank()) {
                    Text(
                        text = infoText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Text(
                    text = artist.biography.toString().replace("-\\s+".toRegex(), "").replace("\\s*\\n+".toRegex(), "\n\n"),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    lineHeight = 22.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Justify
                )
            }
        }
    }
}


data class CategoryResponse(val _embedded: CategoryEmbedded)
data class CategoryEmbedded(val genes: List<Category>)
data class Category(
    val name: String,
    val description: String?,
    val _links: CategoryLinks
)
data class CategoryLinks(val thumbnail: CategoryThumbnail)
data class CategoryThumbnail(val href: String)

@Composable
fun ArtistArtworksContent(artistId: String) {
    var artworks by remember { mutableStateOf<List<Artwork>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(artistId) {
        try {
            val response = artworkApi.getArtworks(artistId)
            artworks = response._embedded.artworks
        } catch (e: Exception) {
            Log.e("Artworks", "Fetch error", e)
        } finally {
            loading = false
        }
    }

    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }else if (artworks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .background(color = LocalArtsyColors.current.cardColor, shape = RoundedCornerShape(24.dp))
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Artworks found.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = LocalArtsyColors.current.textColor
                )
            )
        }
    }
    else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(artworks) { art ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()

                ) {
                    Column(
                        modifier = Modifier.padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(art._links.thumbnail.href)
                                .crossfade(true)
                                .build(),
                            contentDescription = art.title,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${art.title ?: "Untitled"}, ${art.date.orEmpty()}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp),
                            textAlign = TextAlign.Center,
                            color = LocalArtsyColors.current.textColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                showDialog = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val response = geneApi.getCategories(art.id)
                                        delay(1000)
                                        categories = response._embedded.genes

                                    } catch (e: Exception) {
                                        Log.e("Genes", "Error fetching categories", e)
                                    }
                                    finally {
                                        isLoading = false;
                                    }

                                }
                            },
                            modifier = Modifier
                                .defaultMinSize(minWidth = 180.dp)
                                .padding(12.dp)
                                .height(40.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LocalArtsyColors.current.dialogButtonColor)
                        ) {
                            Text("View categories", color = LocalArtsyColors.current.dialogTextColor)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            CategoryDialog(categories = categories, onDismiss = { showDialog = false }, isLoading = isLoading)
        }
    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun CategoryDialog(categories: List<Category>, onDismiss: () -> Unit, isLoading: Boolean) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = LocalArtsyColors.current.dialogButtonColor)
            ) {
                Text("Close", color = LocalArtsyColors.current.dialogTextColor)
            }
        },
        title = { Text("Categories") },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    if (categories.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            if (pagerState.currentPage == 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + categories.size - 1)
                                }
                            } else if (pagerState.currentPage > 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 4.dp)
                            .zIndex(2f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChevronLeft,
                            contentDescription = "Previous",
                            tint = Color.Gray
                        )
                    }
                }


                    if (categories.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No categories available",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    } else {
                        HorizontalPager(
                            count = categories.size,
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 32.dp),
                            modifier = Modifier
                                .fillMaxSize()
                        ) { page ->
                            val category = categories[page]

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1F1E21) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    AsyncImage(
                                        model = category._links.thumbnail.href,
                                        contentDescription = category.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val rawDescription = category.description.orEmpty()

                                    val cleanedDescription = rawDescription
                                        .replace("-\\s+".toRegex(), "")
                                        .replace("\\s*\\n+".toRegex(), "\n\n")
                                        .trim()

                                    val annotatedText =
                                        buildAnnotatedDescription(cleanedDescription)
                                    val uriHandler = LocalUriHandler.current

                                    ClickableText(
                                        text = annotatedText,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = LocalArtsyColors.current.textColor),
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .align(Alignment.Start),
                                        onClick = { offset ->
                                            annotatedText.getStringAnnotations(
                                                tag = "URL",
                                                start = offset,
                                                end = offset
                                            )
                                                .firstOrNull()?.let { annotation ->
                                                    uriHandler.openUri(annotation.item)
                                                }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (categories.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            if (pagerState.currentPage == categories.size - 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - categories.size + 1)
                                }
                            } else if (pagerState.currentPage < categories.lastIndex) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                            .zIndex(2f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = "Next",
                            tint = Color.Gray
                        )
                    }
                }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isSystemInDarkTheme()) Color(0xFF1F1E21) else Color.White
    )
}


interface SimilarApiService {
    @GET("/api/artists/{id}")
    suspend fun getSimilarArtists(@Path("id") artistId: String): SimilarArtistsResponse
}

data class SimilarArtistsResponse(
    val _embedded: SimilarEmbedded
)

data class SimilarEmbedded(
    val artists: List<SimilarArtist>
)

data class SimilarArtist(
    val id: String,
    val name: String,
    val _links: SimilarLinks
)

data class SimilarLinks(
    val thumbnail: Thumbnail1
)

data class Thumbnail1(
    val href: String
)

val similarApi: SimilarApiService = Retrofit.Builder()
    .baseUrl("https://adiartsytwt2.wl.r.appspot.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(SimilarApiService::class.java)
@Composable

fun SimilarArtistsTab(
    artistId: String,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
)  {
    var artists by remember { mutableStateOf<List<SimilarArtist>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(artistId) {
        try {
            val response = similarApi.getSimilarArtists(artistId)
            artists = response._embedded.artists
        } catch (e: Exception) {
            error = "Failed to fetch similar artists"
            Log.e("SimilarArtists", e.toString())
        } finally {
            loading = false
        }
    }

    when {
        loading -> Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...", style = MaterialTheme.typography.bodyMedium)
            }
        }
        error != null -> Text(
            error!!,
            color = Color.Red,
            modifier = Modifier.padding(16.dp)
        )
        else -> {
            if (artists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .background(color = LocalArtsyColors.current.cardColor, shape = RoundedCornerShape(24.dp))
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Similar Artists found.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(artists) { artist ->
                        SimilarArtistItem(
                            artist = artist,
                            artistId = artist.id,
                            navController = navController,
                            snackbarHostState = snackbarHostState

                        )
                    }
                }
            }
            }
    }
}
@Composable
fun SimilarArtistItem(
    artist: SimilarArtist,
    artistId: String,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val user = UserSession.currentUser
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isFavoriteInitial = remember {
        user?.favorites?.containsKey(artistId) == true
    }
    var isFavorite by remember { mutableStateOf(isFavoriteInitial) }

    val placeholderImage = R.drawable.artsy_app
    val imageUrl = artist._links.thumbnail.href
    val isMissingImage = imageUrl.contains("/assets/shared/missing_image.png")

    Card(
        onClick = {
            val nameEncoded = Uri.encode(artist.name)
            navController.navigate("artistDetails/$nameEncoded/$artistId")
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
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
                            .aspectRatio(1f)
                    )
                }
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
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

                                Log.d("FavToggle", "Toggling for artist ID: $artistId")

                                if (!isFavorite) {
                                    val artistDetail = DelApiClientSearch.create().getArtistDetails(artistId)
                                    Log.d("AddFav", "Fetched artist details: $artistDetail")
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
                                        Log.d("current user","${UserSession.currentUser}")
                                        Log.d("AddFav", "Added locally to UserSession")
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Added to favorites")
                                        }
                                    } else {
                                        Log.e("AddFav", "API failed: ${response.code()}")
                                        Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val response = DelApiClientSearch.create().removeFavorite(mapOf("artistId" to artistId))
                                    if (response.isSuccessful) {
                                        isFavorite = false
                                        (UserSession.currentUser?.favorites as? MutableMap<String, Any?>)?.remove(artistId)
                                        Log.d("RemoveFav", "Removed $artistId from UserSession")
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Removed from favorites")
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
                        .background(LocalArtsyColors.current.cardColor, shape = RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Toggle Favorite",
                        tint = LocalArtsyColors.current.textColor
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
                    text = artist.name,
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
@Composable
fun buildAnnotatedDescription(desc: String): AnnotatedString {
    val regex = Regex("""\[(.*?)]\((.*?)\)""")
    val base = "https://www.artsy.net"

    return buildAnnotatedString {
        var idx = 0
        for (m in regex.findAll(desc)) {
            val (lbl, rel) = m.destructured
            val start = m.range.first

            if (idx < start) {
                append(desc.substring(idx, start))
            }

            val url1 = base + rel
            pushStringAnnotation(tag = "URL", annotation = url1)
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(lbl)
            }
            pop()

            idx = m.range.last + 1
        }

        if (idx < desc.length) {
            append(desc.substring(idx))
        }
    }
}