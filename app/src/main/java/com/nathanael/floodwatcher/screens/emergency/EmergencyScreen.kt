package com.nathanael.floodwatcher.screens.emergency

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nathanael.floodwatcher.R
import com.nathanael.floodwatcher.model.EmergencyDirectory

sealed class DirectoryIcon(val value: String, @DrawableRes val icon: Int) {
    object Mobile: DirectoryIcon("mobile", R.drawable.ic_phone)
    object Telephone: DirectoryIcon("telephone", R.drawable.ic_phone_classic)
    object Email: DirectoryIcon("email", R.drawable.ic_email)
}

@Composable
fun EmergencyScreen(viewModel: EmergencyViewModel = viewModel(factory = EmergencyViewModel.Factory)) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Emergency Directories",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 40.dp, bottom = 16.dp),
        )
        DirectoryCard(
            label = "Barangay Directories",
            colors = listOf(
                Color(0xFFBFACE0),
                Color(0xFF645CAA)
            ),
            icon = R.drawable.ic_home_city,
            directories = viewModel.barangayDirectories,
        )
        DirectoryCard(
            label = "Medical Directories",
            colors = listOf(
                Color(0xFFFFE898),
                Color(0xFFF65A83)
            ),
            icon = R.drawable.ic_medical_bag,
            directories = viewModel.medicalDirectories
        )
        DirectoryCard(
            label = "Municipal Directories",
            colors = listOf(
                Color(0xFF64C9CF),
                Color(0xFF035397)
            ),
            icon = R.drawable.ic_caloocan_city,
            directories = viewModel.municipalDirectories
        )
        DirectoryCard(
            label = "NDRRMC",
            colors = listOf(
                Color(0xFF4CACBC),
                Color(0xFFA0D995)
            ),
            icon = R.drawable.ic_ndrrmc_logo,
            directories = viewModel.nationalDirectories
        )
    }
}

@Composable
fun DirectoryCard(
    label: String,
    colors: List<Color>,
    @DrawableRes icon: Int,
    directories: List<EmergencyDirectory>,
    expand: Boolean = false
) {
    var expanded by remember { mutableStateOf(expand) }

    val extraPadding by animateDpAsState(
        if (expanded) 380.dp else 150.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(extraPadding.coerceAtLeast(150.dp))
            .padding(16.dp, 12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .height(150.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = colors,
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    )
                )
        ) {
            val (title, image, button) = createRefs()

            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                alpha = 0.3f,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
                    .constrainAs(image) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
            )

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .constrainAs(title) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .height(40.dp)
                        .width(40.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.width(180.dp)
                )
            }

            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .padding(12.dp)
                    .constrainAs(button) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                if (expanded) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_up_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
        ) {
            items(directories) { directory ->
                DirectoryInfo(directory)
            }
        }
    }
}

@Composable
fun DirectoryInfo(directory: EmergencyDirectory) {
    val directoryIcon = remember {
        mutableStateOf<DirectoryIcon>(DirectoryIcon.Mobile)
    }
    
    LaunchedEffect(directory.type) {
        when (directory.type) {
            "telephone" -> directoryIcon.value = DirectoryIcon.Telephone
            "email" -> directoryIcon.value = DirectoryIcon.Email
            else -> directoryIcon.value = DirectoryIcon.Mobile
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = directoryIcon.value.icon),
            contentDescription = null
        )
        Column(modifier = Modifier.weight(1f, true)) {
            Text(
                text = directory.contact,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = directory.name,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}