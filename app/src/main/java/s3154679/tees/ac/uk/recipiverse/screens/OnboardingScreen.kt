package s3154679.tees.ac.uk.recipiverse.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import s3154679.tees.ac.uk.recipiverse.navigation.LoginScreen
import s3154679.tees.ac.uk.recipiverse.onboarding.OnboardingContentModel
import s3154679.tees.ac.uk.recipiverse.onboarding.OnboardingDisplayManager
import s3154679.tees.ac.uk.recipiverse.onboarding.OnboardingPages
import s3154679.tees.ac.uk.recipiverse.onboarding.OnboardingProgressIndicator
import s3154679.tees.ac.uk.recipiverse.onboarding.OnboardingScreenButtons

@Composable
fun OnboardingScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //onboarding pages
    val pages: List<OnboardingContentModel> = listOf(
        OnboardingContentModel.First,
        OnboardingContentModel.Second,
        OnboardingContentModel.Third

    )

    val pagerState: PagerState = rememberPagerState(initialPage = 0) {
        pages.size
    }


    //used to determine which button to show on the onboarding screen
    val buttonState: State<List<String>> = remember {
        derivedStateOf {
            when (pagerState.currentPage) {
                0 -> listOf("", "Next")
                1 -> listOf("Back", "Next")
                2 -> listOf("Back", "Start")
                else -> listOf("", "")
            }
        }
    }


    Scaffold(

        containerColor = Color(0xFF00BFA6),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        bottom = 10.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if(buttonState.value[0].isNotEmpty()){
                        OnboardingScreenButtons(
                            text = buttonState.value[0],
                            backgroundColor = Color.Transparent,
                            textColor = Color.White
                        ) {

                            scope.launch {
                                if(pagerState.currentPage > 0) {
                                    pagerState.animateScrollToPage(
                                        pagerState.currentPage - 1
                                    )
                                }
                            }

                        }
                    }

                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    OnboardingProgressIndicator(
                        pageSize = pages.size,
                        currentPage = pagerState.currentPage
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    OnboardingScreenButtons(
                        text = buttonState.value[1],
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        scope.launch {
                            if(pagerState.currentPage < pages.size - 1) {
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage + 1
                                )
                            }
                            else {

                                //set onboarding finished
                                OnboardingDisplayManager(context).setOnboardingFinished()

                                //navigate to login screen
                                navController.navigate(LoginScreen)

                            }
                        }
                    }
                }

            }
        },

        content = {
            Column(
                modifier = Modifier.padding(it)
            ) {
                HorizontalPager(
                    state = pagerState
                ) { index ->
                    OnboardingPages(onboardingContentModel = pages[index])

                }


            }

        }

    )
}